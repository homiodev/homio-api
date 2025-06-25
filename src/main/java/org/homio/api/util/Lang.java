package org.homio.api.util;

import static org.homio.api.util.JsonUtils.OBJECT_MAPPER;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Lang {
  en,
  ru;

  private static final Map<String, ObjectNode> i18nLang = new HashMap<>();
  public static String CURRENT_LANG = "en";

  public static void clear() {
    i18nLang.clear();
  }

  public static ObjectNode getLangJson(@Nullable String lang) {
    return getJson(lang, false);
  }

  public static String findPathText(@NotNull String name) {
    ObjectNode objectNode = getJson(null, false);
    return objectNode.at("/" + name.replaceAll("\\.", "/")).textValue();
  }

  public static String getServerMessageOrDefault(
      @Nullable String message, @Nullable String defaultValue) {
    return getServerMessage(message, Map.of(), defaultValue);
  }

  public static String getServerMessage(@Nullable String message, @Nullable String value) {
    return getServerMessage(message, "VALUE", StringUtils.defaultIfEmpty(value, ""));
  }

  public static String getServerMessage(@Nullable String message) {
    return getServerMessage(message, (Map<String, Object>) null);
  }

  public static Optional<String> getServerMessageOptional(@Nullable String message) {
    String result = getServerMessage(message, (Map<String, Object>) null);
    return Optional.ofNullable(Objects.equals(result, message) ? null : result);
  }

  public static String getServerMessage(
      @Nullable String message, @NotNull String param0, @NotNull String value0) {
    return getServerMessage(message, Map.of(param0, value0));
  }

  public static String getServerMessage(
      @Nullable String message,
      @Nullable Map<String, Object> params,
      @Nullable String defaultValue) {
    if (StringUtils.isEmpty(message)) {
      return message;
    }
    String result = getServerMessage(CURRENT_LANG, message, params);
    if (result.equals(message) && !"en".equals(CURRENT_LANG)) {
      result = getServerMessage("en", message, params);
    }
    if (result.equals(message)) {
      return defaultValue;
    }
    return result;
  }

  public static String getServerMessage(
      @Nullable String message, @Nullable Map<String, Object> params) {
    if (StringUtils.isEmpty(message)) {
      return message;
    }
    String result = getServerMessage(CURRENT_LANG, message, params);
    if (result.equals(message) && !"en".equals(CURRENT_LANG)) {
      result = getServerMessage("en", message, params);
    }
    return result;
  }

  public static String getServerMessage(
      @NotNull String language, @NotNull String message, @Nullable Map<String, Object> params) {
    ObjectNode langJson = getJson(language, true);
    String text =
        StringUtils.defaultIfEmpty(
            langJson.at("/" + message.replaceAll("\\.", "/")).textValue(), message);
    return params == null ? text : StringSubstitutor.replace(text, params, "{{", "}}");
  }

  private static ObjectNode getJson(@Nullable String lang, boolean isServer) {
    String langStr = lang == null ? CURRENT_LANG : lang;
    String key = langStr + (isServer ? "_server" : "");
    if (!i18nLang.containsKey(key)) {
      i18nLang.put(
          key,
          JsonUtils.readAndMergeJSON("i18n/" + key + ".json", OBJECT_MAPPER.createObjectNode()));
    }
    return i18nLang.get(key);
  }
}
