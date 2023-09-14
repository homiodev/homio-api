package org.homio.api.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.util.NumberUtils;
import org.springframework.util.SystemPropertyUtils;

/**
 * Represent object with ability to change it's value and notify observable classes
 */
public class UpdatableValue<T> {

    // any class which uses this UpdatableValue may listen it's changes
    private final List<Consumer<T>> updateListeners = new ArrayList<>();
    private T value;
    // when we create UpdatableValue which base on another UpdatableValue we must reflect base changes.
    private @Getter @NotNull final List<Consumer<T>> reflectListeners = new ArrayList<>();
    private @Getter @NotNull final String name;
    private Function<String, T> stringConverter;
    private Function<T, T> extraFunc;
    private Set<Validator> validators = new HashSet<>();
    private @Getter int updateCount;
    private @Getter long lastRefreshTime;
    private boolean fetchFreshValueStarted;

    private UpdatableValue(@NotNull String name) {
        this.name = name;
    }

    /**
     * Create UpdatableValue with null initial.
     *
     * @param name      value name
     * @param classType value type
     * @param <T>       - value type
     * @return UpdatableValue
     */
    public static <T> UpdatableValue<T> deferred(@NotNull String name, @NotNull Class<T> classType) {
        UpdatableValue<T> updatableValue = new UpdatableValue<>(name);
        updatableValue.stringConverter = findStringConverter(classType);
        return updatableValue;
    }

    public static <T> @NotNull UpdatableValue<T> wrap(@NotNull T value, @NotNull String name) {
        UpdatableValue<T> updatableValue = new UpdatableValue<>(name);
        updatableValue.value = value;
        updatableValue.lastRefreshTime = System.currentTimeMillis();
        updatableValue.stringConverter = findStringConverter(value.getClass());
        return updatableValue;
    }

    public static <T> UpdatableValue<T> ofNullable(@Nullable T value, @NotNull String name, @NotNull Class<T> valueType) {
        if (value == null) {
            return deferred(name, valueType);
        }
        return wrap(value, name);
    }

    public T getValue() {
        return extraFunc == null ? value : extraFunc.apply(value);
    }

    /**
     * Function get value and check if value is older that Duration and fire update handler
     *
     * @param duration - duration during which value will be 'fresh'
     * @return value
     */
    public T getFreshValue(Duration duration, Runnable updateHandler) {
        if (!fetchFreshValueStarted && System.currentTimeMillis() - lastRefreshTime > duration.toMillis()) {
            fetchFreshValueStarted = true;
            updateHandler.run();
        }
        return getValue();
    }

    public T getFreshValue(Duration duration, Supplier<T> updateSupplier) {
        return getFreshValue(duration, () -> {
            update(updateSupplier.get());
        });
    }

    public void validate(T value) {
        for (Validator validator : validators) {
            validator.validate(value);
        }
    }

    public T parse(String value) {
        return stringConverter.apply(value);
    }

    public Object parseAndUpdate(String updatedValue) {
        return update(stringConverter.apply(updatedValue));
    }

    public T update(T updatedValue) {
        T prevValue = value;
        lastRefreshTime = System.currentTimeMillis();
        fetchFreshValueStarted = false;
        if (value == null || !value.equals(updatedValue)) {
            validate(updatedValue);
            value = updatedValue;
            updateCount++;

            // invoke all listeners
            updateListeners.forEach(consumer -> consumer.accept(getValue()));
            // reflect Listeners must according to code order(i.e. setCorePoolSize must precede setMaxPoolSize)
            reflectListeners.forEach(consumer -> consumer.accept(getValue()));
        }
        return prevValue;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public void addListener(Consumer<T> listener) {
        this.updateListeners.add(listener);
    }

    public UpdatableValue<T> andExtra(Function<T, T> extraFunc) {
        UpdatableValue<T> updatableValueWithExtra = UpdatableValue.wrap(this.value, this.name);
        updatableValueWithExtra.extraFunc = extraFunc;
        updatableValueWithExtra.stringConverter = this.stringConverter;
        this.reflectListeners.add(updatableValueWithExtra::update);
        return updatableValueWithExtra;
    }

    private static <T> Function<String, T> findStringConverter(Class<?> genericClass) {
        if (genericClass.isEnum()) {
            return s -> (T) Enum.valueOf((Class<Enum>) genericClass, s);
        } else if (Number.class.isAssignableFrom(genericClass)) {
            return s -> (T) NumberUtils.parseNumber(s, ((Class<? extends Number>) genericClass));
        }
        return s -> (T) s;
    }

    // fetch field name either from @Field or from @Value or from defaultValueSupplier
    private static String getNameFromAnnotation(/*Column field,*/ Value value, Supplier<String> defaultValueSupplier) {
        /*if (field != null) {
            return field.name();
        } else*/
        if (value != null) {
            String v = value.value();
            return v.substring(
                v.indexOf(SystemPropertyUtils.PLACEHOLDER_PREFIX) + SystemPropertyUtils.PLACEHOLDER_PREFIX.length(),
                v.contains(SystemPropertyUtils.VALUE_SEPARATOR) ? v.indexOf(SystemPropertyUtils.VALUE_SEPARATOR)
                    : v.indexOf(SystemPropertyUtils.PLACEHOLDER_SUFFIX));
        }
        return defaultValueSupplier.get();
    }

    private interface Validator {

        void validate(Object value);
    }

    /**
     * Uses to ObjectMapper conversion
     */
    public static final class UpdatableValueSerializer extends JsonSerializer<UpdatableValue> {

        @Override
        public void serialize(UpdatableValue updatableValue, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeObject(updatableValue.value);
        }
    }

    /**
     * Uses to inject spring @Value annotation to field
     */
    public static final class UpdatableValueConverter implements ConditionalGenericConverter {

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            Set<ConvertiblePair> convertiblePairs = new HashSet<>();
            convertiblePairs.add(new ConvertiblePair(String.class, UpdatableValue.class));

            return convertiblePairs;
        }

        @Override
        public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
            return UpdatableValue.class.isAssignableFrom(targetType.getType());
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            try {
                Class<?> genericClass = (Class<?>) targetType.getResolvableType().getGeneric(0).getType();
                if (!UpdatableValue.class.isAssignableFrom(targetType.getType())) {
                    throw new RuntimeException("Failed cast targetType <" + targetType.getType() + "> to UpdatableValue in " + targetType.getSource());
                }
                if (genericClass == null) {
                    throw new RuntimeException("UpdatableValue has no generic type specified in " + targetType.getSource());
                }
                String name = getNameFromAnnotation(/*targetType.getAnnotation(Column.class),*/
                    targetType.getAnnotation(Value.class), () -> {
                        // must never through as UpdatableValueConverter uses by spring only with @Value annotation
                        throw new RuntimeException("Can not fetch UpdatableValue name from " + targetType.getSource());
                    });
                UpdatableValue updatableValue = new UpdatableValue<>(name);
                updatableValue.stringConverter = findStringConverter(genericClass);

                updatableValue.validators = collectValidators(targetType, a -> {
                    if (a.annotationType().isAssignableFrom(Min.class)) {
                        return ((Min) a).value();
                    }
                    if (a.annotationType().isAssignableFrom(Max.class)) {
                        return ((Max) a).value();
                    }
                    return null;
                });

                updatableValue.value = updatableValue.stringConverter.apply(source.toString());
                updatableValue.validate(updatableValue.value);

                return updatableValue;
            } catch (Exception ex) {
                throw new RuntimeException("Could not create instance of type: " + targetType + ". Source: " + source + ": " + ex.getMessage());
            }
        }

        private Set<Validator> collectValidators(TypeDescriptor targetType, Function<Annotation, Long> exceedConverter) {
            return Stream.of(targetType.getAnnotations())
                         .filter(a -> exceedConverter.apply(a) != null) // ensures that only known annotations uses
                         .map(a -> (Validator) value -> {
                             Long exceedValue = exceedConverter.apply(a);
                             Long actualValue = ((Number) value).longValue();
                             if ((actualValue < exceedValue && a.annotationType().isAssignableFrom(Min.class)) ||
                                 (actualValue > exceedValue && a.annotationType().isAssignableFrom(Max.class))) {
                                 throw new IllegalArgumentException(
                                     String.format("Validation fails for value <%s>. %s value is <%d>", value, a.annotationType().getSimpleName(),
                                         exceedValue));
                             }
                         })
                         .collect(Collectors.toSet());
        }
    }
}
