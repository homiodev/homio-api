package org.touchhome.bundle.api.util;

import com.pivovarit.function.ThrowingBiFunction;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpringUtils {

    public static final Pattern ENV_PATTERN = Pattern.compile("\\$\\{.*?}");
    public static final Pattern HASH_PATTERN = Pattern.compile("#\\{.*?}");

    public static String replaceEnvValues(String text, ThrowingBiFunction<String, String, String, Exception> propertyGetter) {
        return replaceValues(ENV_PATTERN, text, propertyGetter);
    }

    public static String replaceHashValues(String text, ThrowingBiFunction<String, String, String, Exception> propertyGetter) {
        return replaceValues(HASH_PATTERN, text, propertyGetter);
    }

    public static String replaceValues(Pattern pattern, String text, ThrowingBiFunction<String, String, String, Exception> propertyGetter) {
        Matcher matcher = pattern.matcher(text);
        StringBuffer noteBuffer = new StringBuffer();
        while (matcher.find()) {
            String group = matcher.group();
            matcher.appendReplacement(noteBuffer, getEnvProperty(group, propertyGetter));
        }
        matcher.appendTail(noteBuffer);
        return noteBuffer.length() == 0 ? text : noteBuffer.toString();
    }

    public static List<String> getPatternValues(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        List<String> result = new ArrayList<>();
        while (matcher.find()) {
            String group = matcher.group();
            result.add(getSpringValuesPattern(group)[0]);
        }
        return result;
    }

    @SneakyThrows
    public static String getEnvProperty(String value, ThrowingBiFunction<String, String, String, Exception> propertyGetter) {
        String[] array = getSpringValuesPattern(value);
        return propertyGetter.apply(array[0], array[1]);
    }

    public static String[] getSpringValuesPattern(String value) {
        String valuePattern = value.substring(2, value.length() - 1);
        return valuePattern.contains(":") ? valuePattern.split(":") : new String[]{valuePattern, ""};
    }
}
