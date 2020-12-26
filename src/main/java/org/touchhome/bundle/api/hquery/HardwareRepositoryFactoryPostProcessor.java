package org.touchhome.bundle.api.hquery;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.Level;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.touchhome.bundle.api.hquery.api.*;
import org.touchhome.bundle.api.util.Curl;
import org.touchhome.bundle.api.util.SpringUtils;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class HardwareRepositoryFactoryPostProcessor implements BeanFactoryPostProcessor {

    private static final BlockingQueue<HardwareProcessIO> inputProcessContextJobs = new LinkedBlockingQueue<>(10);
    private static final BlockingQueue<HardwareProcessIO> errorProcessContextJobs = new LinkedBlockingQueue<>(10);
    private static final Constructor<MethodHandles.Lookup> lookupConstructor;
    private static final Map<String, ProcessCache> cache = new HashMap<>();

    static {
        try {
            lookupConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);
            lookupConstructor.setAccessible(true);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to instantiate MethodHandles.Lookup", ex);
        }
    }

    static {
        new Thread(new StreamReader(errorProcessContextJobs), "Hardware io error stream thread").start();
        new Thread(new StreamReader(inputProcessContextJobs), "Hardware io input stream thread").start();
    }

    private final String basePackages;
    private HardwareRepositoryFactoryPostHandler handler;
    HardwareRepositoryFactoryPostProcessor(String basePackages, HardwareRepositoryFactoryPostHandler handler) {
        this.basePackages = basePackages;
        this.handler = handler;
    }

    @Override
    @SneakyThrows
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        Environment env = beanFactory.getBean(Environment.class);
        HQueryExecutor hQueryExecutor = beanFactory.getBean(HQueryExecutor.class);
        List<Class<?>> classes = getClassesWithAnnotation();
        for (Class<?> aClass : classes) {
            beanFactory.registerSingleton(aClass.getSimpleName(), Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{aClass}, (proxy, method, args) -> {
                Class<?> returnType = method.getReturnType();
                List<Object> results = null;
                for (HardwareQuery hardwareQuery : method.getDeclaredAnnotationsByType(HardwareQuery.class)) {
                    if (results == null) {
                        results = new ArrayList<>();
                    }
                    results.add(handleHardwareQuery(hardwareQuery, args, method, env, aClass, hQueryExecutor));
                }
                Optional<AtomicReference<Object>> value = handleCurlQuery(method, args, env);
                if (value.isPresent()) {
                    return value.get().get();
                }
                if (results != null) {
                    if (results.isEmpty()) {
                        return null;
                    } else if (results.size() == 1) {
                        return results.iterator().next();
                    } else if (returnType.isAssignableFrom(List.class)) {
                        return results;
                    } else {
                        return null;
                    }
                }

                if (method.isDefault()) {
                    return lookupConstructor.newInstance(aClass)
                            .in(aClass)
                            .unreflectSpecial(method, aClass)
                            .bindTo(proxy)
                            .invokeWithArguments(args);
                }
                throw new RuntimeException("Unable to execute hardware method without implementation");
            }));
        }
        if (this.handler != null) {
            this.handler.accept(beanFactory);
        }
    }

    private Optional<AtomicReference<Object>> handleCurlQuery(Method method, Object[] args, Environment env) {
        CurlQuery curlQuery = method.getDeclaredAnnotation(CurlQuery.class);
        if (curlQuery != null) {
            String argCmd = replaceStringWithArgs(curlQuery.value(), args, method);
            String command = SpringUtils.replaceEnvValues(argCmd, env::getProperty);
            ProcessCache processCache;

            if ((curlQuery.cache() || curlQuery.cacheValid() > 0)
                    && cache.containsKey(command)
                    && (curlQuery.cacheValid() < 1 || (System.currentTimeMillis() - cache.get(command).executedTime) / 1000 < cache.get(command).cacheValidInSec)) {
                processCache = cache.get(command);
            } else {
                processCache = new ProcessCache(curlQuery.cacheValid());
                try {
                    Object result = Curl.getWithTimeout(command, method.getReturnType(), curlQuery.maxSecondsTimeout());
                    Function<Object, Object> mapping = curlQuery.mapping().newInstance();
                    processCache.response = mapping.apply(result);

                } catch (Exception ex) {
                    log.error("Error while execute curl command <{}>. Msg: <{}>", command, ex.getMessage());
                    processCache.errors = Collections.singletonList(TouchHomeUtils.getErrorMessage(ex));
                    if (!curlQuery.ignoreOnError()) {
                        throw new HardwareException(processCache.errors, -1);
                    } else if (!curlQuery.valueOnError().isEmpty()) {
                        return Optional.of(new AtomicReference<>(curlQuery.valueOnError()));
                    }
                    processCache.retValue = -1;

                    // to avoid NPE instantiate empty class
                    if (!method.getReturnType().isAssignableFrom(String.class)) {
                        processCache.response = TouchHomeUtils.newInstance(method.getReturnType());
                    }
                }
                if (processCache.errors == null && curlQuery.cache() || curlQuery.cacheValid() > 0) {
                    cache.put(command, processCache);
                }
            }
            return Optional.of(new AtomicReference<>(processCache.response));
        }
        return Optional.empty();
    }

    private String replaceStringWithArgs(String str, Object[] args, Method method) {
        if (args != null) {
            Annotation[][] apiParams = method.getParameterAnnotations();
            for (int i = 0; i < args.length; i++) {
                String regexp = null;
                Object arg = args[i];
                if (apiParams.length > i) {
                    regexp = ((HQueryParam) apiParams[i][0]).value();
                }

                str = str.replaceAll(regexp == null ? ":([^\\s]+)" : ":" + regexp, String.valueOf(arg));
            }
        }
        return str;
    }

    @SneakyThrows
    private Object handleHardwareQuery(HardwareQuery hardwareQuery, Object[] args, Method method, Environment env, Class<?> aClass, HQueryExecutor hQueryExecutor) {
        ErrorsHandler errorsHandler = method.getAnnotation(ErrorsHandler.class);
        List<String> parts = new ArrayList<>();
        String[] values = hQueryExecutor.getValues(hardwareQuery);
        Stream.of(values).filter(cmd -> !cmd.isEmpty()).forEach(cmd -> {
            String argCmd = replaceStringWithArgs(cmd, args, method);
            String envCmd = SpringUtils.replaceEnvValues(argCmd, env::getProperty);
            parts.add(hQueryExecutor.updateCommand(envCmd));
        });
        if (parts.isEmpty()) {
            return returnOnDisableValue(method, aClass);
        }
        String[] cmdParts = parts.toArray(new String[0]);
        String command = String.join(", ", parts);
        ProcessCache processCache;
        if ((hardwareQuery.cache() || hardwareQuery.cacheValid() > 0)
                && cache.containsKey(command)
                && (hardwareQuery.cacheValid() < 1 || (System.currentTimeMillis() - cache.get(command).executedTime) / 1000 < cache.get(command).cacheValidInSec)) {
            processCache = cache.get(command);
        } else {
            processCache = new ProcessCache(hardwareQuery.cacheValid());
            if (!StringUtils.isEmpty(hardwareQuery.echo())) {
                log.info("Execute: <{}>. Command: <{}>", hardwareQuery.echo(), command);
            } else {
                log.info("Execute command: <{}>", command);
            }
            Process process;
            try {
                if (!StringUtils.isEmpty(hardwareQuery.dir())) {
                    File dir = new File(replaceStringWithArgs(hardwareQuery.dir(), args, method));
                    process = hQueryExecutor.createProcess(cmdParts, null, dir);
                } else {
                    process = hQueryExecutor.createProcess(cmdParts, null, null);
                }

                HardwareProcessIO errors = new HardwareProcessIO(process, hardwareQuery.printOutput() ? Level.WARN : Level.TRACE, Process::getErrorStream);
                HardwareProcessIO inputs = new HardwareProcessIO(process, hardwareQuery.printOutput() ? Level.INFO : Level.TRACE, Process::getInputStream);
                errorProcessContextJobs.add(errors);
                inputProcessContextJobs.add(inputs);

                process.waitFor(hardwareQuery.maxSecondsTimeout(), TimeUnit.SECONDS);
                processCache.retValue = process.exitValue();
                processCache.errors = errors.get();
                processCache.inputs = inputs.get();
            } catch (Exception ex) {
                processCache.retValue = 1;
                processCache.errors = Collections.singletonList(TouchHomeUtils.getErrorMessage(ex));
            } finally {
                if (processCache.errors != null && hardwareQuery.cache() || hardwareQuery.cacheValid() > 0) {
                    cache.put(command, processCache);
                }
            }
        }

        return handleCommandResult(hardwareQuery, method, errorsHandler, command, processCache.retValue, processCache.inputs, processCache.errors);
    }

    private Object returnOnDisableValue(Method method, Class<?> aClass) {
        // in case we expect return num we ignore any errors
        Class<?> returnType = method.getReturnType();
        HardwareRepositoryAnnotation hardwareRepositoryAnnotation = aClass.getDeclaredAnnotation(HardwareRepositoryAnnotation.class);
        if (returnType.isPrimitive()) {
            switch (returnType.getName()) {
                case "int":
                    return hardwareRepositoryAnnotation.intValueOnDisable();
                case "boolean":
                    return hardwareRepositoryAnnotation.boolValueOnDisable();
            }
        }
        if (returnType.isAssignableFrom(String.class)) {
            return hardwareRepositoryAnnotation.stringValueOnDisable();
        }
        return null;
    }

    private Object handleCommandResult(HardwareQuery hardwareQuery, Method method, ErrorsHandler errorsHandler, String command, int retValue, List<String> inputs, List<String> errors) throws IllegalAccessException, InstantiationException {
        Class<?> returnType = method.getReturnType();

        // in case we expect return num we ignore any errors
        if (returnType.isPrimitive()) {
            switch (returnType.getName()) {
                case "int":
                    return retValue;
                case "boolean":
                    return retValue == 0;
            }
        }

        if (retValue != 0) {
            throwErrors(errorsHandler, errors);
            if (errorsHandler != null) {
                String error = errors.isEmpty() ? errorsHandler.onRetCodeError() : String.join("; ", errors);
                if (errorsHandler.logError()) {
                    log.error(error);
                }
                if (errorsHandler.throwError()) {
                    throw new IllegalStateException(error);
                }
            } else {
                log.error("Error while execute command <{}>. Code: <{}>, Msg: <{}>", command, retValue, String.join(", ", errors));
                if (!hardwareQuery.ignoreOnError()) {
                    throw new HardwareException(errors, retValue);
                } else if (!hardwareQuery.valueOnError().isEmpty()) {
                    return hardwareQuery.valueOnError();
                }
            }
        } else {
            for (String error : errors) {
                if (!error.isEmpty()) {
                    log.warn("Error <{}>", error);
                }
            }
            inputs = inputs.stream().map(String::trim).collect(Collectors.toList());
            ListParse listParse = method.getAnnotation(ListParse.class);
            ListParse.LineParse lineParse = method.getAnnotation(ListParse.LineParse.class);
            ListParse.BooleanLineParse booleanParse = method.getAnnotation(ListParse.BooleanLineParse.class);
            ListParse.LineParsers lineParsers = method.getAnnotation(ListParse.LineParsers.class);
            RawParse rawParse = method.getAnnotation(RawParse.class);

            if (listParse != null) {
                String delimiter = listParse.delimiter();
                List<List<String>> buckets = new ArrayList<>();
                List<String> currentBucket = null;

                for (String input : inputs) {
                    if (input.matches(delimiter)) {
                        currentBucket = new ArrayList<>();
                        buckets.add(currentBucket);
                    }
                    if (currentBucket != null) {
                        currentBucket.add(input);
                    }
                }
                Class<?> genericClass = listParse.clazz();
                List<Object> result = new ArrayList<>();
                for (List<String> bucket : buckets) {
                    result.add(handleBucket(bucket, genericClass));
                }
                return result;
            } else if (lineParse != null) {
                return handleBucket(inputs, lineParse, null);
            } else if (lineParsers != null) {
                return handleBucket(inputs, lineParsers);
            } else if (booleanParse != null) {
                return handleBucket(inputs, booleanParse);
            } else if (rawParse != null) {
                return handleBucket(inputs, rawParse, null);
            } else {
                return handleBucket(inputs, returnType);
            }
        }
        return null;
    }

    private Object handleBucket(List<String> input, Class<?> genericClass) throws IllegalAccessException, InstantiationException {
        if (genericClass.isPrimitive()) {
            switch (genericClass.getName()) {
                case "void":
                    return null;
            }
        }
        Object obj = genericClass.newInstance();

        boolean handleFields = false;

        SplitParse splitParse = genericClass.getDeclaredAnnotation(SplitParse.class);
        if (splitParse != null) {
            for (String item : input) {
                String[] split = item.split(splitParse.value());
                for (Field field : FieldUtils.getFieldsListWithAnnotation(genericClass, SplitParse.SplitParseIndex.class)) {
                    int splitIndex = field.getDeclaredAnnotation(SplitParse.SplitParseIndex.class).index();
                    if (splitIndex >= 0 && splitIndex < split.length) {
                        String value = split[splitIndex].trim();
                        FieldUtils.writeField(field, obj, handleType(value, field.getType()), true);
                        handleFields = true;
                    }
                }
            }
        }

        for (Field field : FieldUtils.getFieldsListWithAnnotation(genericClass, RawParse.class)) {
            Object value = handleBucket(input, field.getDeclaredAnnotation(RawParse.class), field);
            FieldUtils.writeField(field, obj, value, true);
            handleFields = true;
        }

        for (Field field : FieldUtils.getFieldsListWithAnnotation(genericClass, ListParse.LineParse.class)) {
            Object value = handleBucket(input, field.getDeclaredAnnotation(ListParse.LineParse.class), field);
            FieldUtils.writeField(field, obj, value, true);
            handleFields = true;
        }

        for (Field field : FieldUtils.getFieldsListWithAnnotation(genericClass, ListParse.BooleanLineParse.class)) {
            Object value = handleBucket(input, field.getDeclaredAnnotation(ListParse.BooleanLineParse.class));
            FieldUtils.writeField(field, obj, value, true);
            handleFields = true;
        }

        List<Field> listFields = FieldUtils.getFieldsListWithAnnotation(genericClass, ListParse.LineParsers.class);
        for (Field field : listFields) {
            ListParse.LineParsers lineParsers = field.getDeclaredAnnotation(ListParse.LineParsers.class);
            Object value = handleBucket(input, lineParsers);
            FieldUtils.writeField(field, obj, value, true);
        }

        if (!handleFields && listFields.isEmpty()) {
            if (genericClass.isAssignableFrom(String.class)) {
                return String.join("", input);
            }
        }

        return obj;
    }

    private Object handleBucket(List<String> inputs, ListParse.LineParse lineParse, Field field) {
        for (String input : inputs) {
            if (input.matches(lineParse.value())) {
                String group = findGroup(input, lineParse.value(), lineParse.group());
                if (group != null) {
                    return handleType(group, field.getType());
                }
            }
        }
        return null;
    }

    private Object handleBucket(List<String> inputs, RawParse rawParse, Field field) {
        return TouchHomeUtils.newInstance(rawParse.value()).handle(inputs, field);
    }

    private Object handleType(String value, Class<?> type) {
        if (type.isAssignableFrom(Integer.class)) {
            return new Integer(value);
        } else if (type.isAssignableFrom(Double.class)) {
            return new Double(value);
        }

        return value;
    }

    private Object handleBucket(List<String> inputs, ListParse.BooleanLineParse lineParse) {
        for (String input : inputs) {
            if (input.matches(lineParse.value())) {
                String group = findGroup(input, lineParse.value(), lineParse.group());
                if (group != null) {
                    if (group.equals(lineParse.when())) {
                        return !lineParse.inverse();
                    }
                }
            }
        }
        if (!lineParse.when().isEmpty()) {
            return lineParse.inverse();
        }

        return null;
    }

    private Object handleBucket(List<String> inputs, ListParse.LineParsers lineParsers) {
        for (ListParse.LineParse lineParse : lineParsers.value()) {
            Object val = handleBucket(inputs, lineParse, null);
            if (val != null) {
                return val;
            }
        }
        return null;
    }

    private String findGroup(String input, String regexp, int group) {
        Matcher m = Pattern.compile(regexp).matcher(input);
        if (m.find()) {
            return m.group(group);
        }
        return null;
    }

    private void throwErrors(ErrorsHandler errorsHandler, List<String> errors) {
        if (!errors.isEmpty() && errorsHandler != null) {
            for (ErrorsHandler.ErrorHandler errorHandler : errorsHandler.errorHandlers()) {
                if (errors.contains(errorHandler.onError())) {
                    throw new IllegalStateException(errorHandler.throwError());
                }
            }
        }
    }

    private <T> List<Class<? extends T>> getClassesWithAnnotation() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return true;
            }
        };

        scanner.addIncludeFilter(new AnnotationTypeFilter(HardwareRepositoryAnnotation.class));
        List<Class<? extends T>> foundClasses = new ArrayList<>();
        for (BeanDefinition bd : scanner.findCandidateComponents(basePackages)) {
            try {
                foundClasses.add((Class<? extends T>) Class.forName(bd.getBeanClassName()));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return foundClasses;
    }

    @RequiredArgsConstructor
    private static class StreamReader implements Runnable {

        private final BlockingQueue<HardwareProcessIO> queue;

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                HardwareProcessIO context = null;
                try {
                    context = queue.take();
                    Process process = context.process;

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.inputStreamFn.apply(process)))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            log.log(context.logLevel, line);
                            context.lines.add(line);
                        }
                    }
                } catch (Exception ex) {
                    log.error("Hardware error occurs while take io stream");
                } finally {
                    if (context != null) {
                        context.sem.release();
                    }
                }
            }
        }
    }

    @RequiredArgsConstructor
    private static class ProcessCache {
        final int cacheValidInSec;
        int retValue;
        List<String> errors;
        List<String> inputs;
        Object response;
        long executedTime = System.currentTimeMillis();
    }

    private class HardwareProcessIO {
        private final Process process;
        private final List<String> lines = new ArrayList<>();
        private final Semaphore sem = new Semaphore(1);
        private Level logLevel;
        private Function<Process, InputStream> inputStreamFn;

        @SneakyThrows
        public HardwareProcessIO(Process process, Level logLevel, Function<Process, InputStream> inputStreamFn) {
            this.process = process;
            this.logLevel = logLevel;
            this.inputStreamFn = inputStreamFn;
            this.sem.acquire();
        }

        @SneakyThrows
        public List<String> get() {
            try {
                sem.acquire();
                return lines;
            } finally {
                sem.release();
            }
        }
    }
}
