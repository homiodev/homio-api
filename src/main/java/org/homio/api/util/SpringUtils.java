package org.homio.api.util;

import com.pivovarit.function.ThrowingBiFunction;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpringUtils {

  public static final Pattern ENV_PATTERN = Pattern.compile("\\$\\{.*?}");
  public static final Pattern HASH_PATTERN = Pattern.compile("#\\{.*?}");

  public static final Pattern PATTERN = Pattern.compile("\\$\\{.*?}+");

  public static final int PATTERN_PREFIX_LENGTH = "${".length();

  public static final int PATTERN_SUFFIX_LENGTH = "}".length();

  public static String replaceEnvValues(String text, ReplaceEnvHandler replaceEnvHandler) {
    return replaceValues(ENV_PATTERN, text, replaceEnvHandler);
  }

  public static String replaceHashValues(String text, ReplaceEnvHandler replaceEnvHandler) {
    return replaceValues(HASH_PATTERN, text, replaceEnvHandler);
  }

  @SneakyThrows
  public static String replaceValues(Pattern pattern, String text, ReplaceEnvHandler replaceEnvHandler) {
    Matcher matcher = pattern.matcher(text);
    StringBuffer noteBuffer = new StringBuffer();
    while (matcher.find()) {
      String group = matcher.group();
      String[] array = getSpringValuesPattern(group);
      String replace = replaceEnvHandler.replace(array[0], array[1], noteBuffer);
      matcher.appendReplacement(noteBuffer, replace);
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

  public static String replaceValues(String text, BiFunction<String, String, String> propertyGetter) {
    return replaceValues(text, propertyGetter, PATTERN, PATTERN_PREFIX_LENGTH, PATTERN_SUFFIX_LENGTH);
  }

  /**
   * Search by PATTERN in 'text' property and apply 'propertyGetter' function to it
   *
   * @param text           - text to search pattern values
   * @param propertyGetter - function to apply
   * @param pattern        -
   * @param prefixLength   -
   * @param suffixLength   -
   * @return updated text
   */
  public static String replaceValues(String text, BiFunction<String, String, String> propertyGetter, Pattern pattern,
                                     int prefixLength, int suffixLength) {
    Matcher matcher = pattern.matcher(text);
    StringBuffer noteBuffer = new StringBuffer();
    while (matcher.find()) {
      String group = matcher.group();
      String envProperty = getEnvProperty(group, propertyGetter, prefixLength, suffixLength);
      // fire recursively replace in case of compose env variable
      String value = replaceValues(envProperty, propertyGetter, pattern, prefixLength, suffixLength);
      matcher.appendReplacement(noteBuffer, value);
    }
    matcher.appendTail(noteBuffer);
    return noteBuffer.toString();
  }

  public static String getEnvProperty(String value, BiFunction<String, String, String> propertyGetter,
                                      int prefixLength, int suffixLength) {
    String[] array = getSpringValuesPattern(value, prefixLength, suffixLength);
    return propertyGetter.apply(array[0], array[1]);
  }

  public static String[] getSpringValuesPattern(String value, int prefixLength, int suffixLength) {
    String valuePattern = value.substring(prefixLength, value.length() - suffixLength);
    return valuePattern.contains(":") ? valuePattern.split(":", 2) : new String[]{valuePattern, ""};
  }

  public static String fixValue(String strValue) {
    if (strValue.startsWith("#{")) {
      strValue = strValue.substring(strValue.indexOf("${"), strValue.length() - 1);
      strValue = strValue.substring(0, strValue.lastIndexOf("}") + 1);
    }
    return strValue;
  }

  public interface ReplaceEnvHandler {

    String replace(String env, String defValue, StringBuffer fullPrefix) throws Exception;
  }
}
