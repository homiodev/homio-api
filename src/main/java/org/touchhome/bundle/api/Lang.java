package org.touchhome.bundle.api;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.touchhome.bundle.api.util.FlowMap;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum Lang {
    en;

    private static final Map<String, ObjectNode> i18nLang = new HashMap<>();
    public static String DEFAULT_LANG = "en";
    public static String CURRENT_LANG;

    public static void clear() {
        i18nLang.clear();
    }

    public static ObjectNode getLangJson(String lang) {
        return getJson(lang, false);
    }

    public static String findPathText(String name) {
        ObjectNode objectNode = getJson(null, false);
        return objectNode.at("/" + name.replaceAll("\\.", "/")).textValue();
    }

    public static String getServerMessage(String message, FlowMap messageParam) {
        return getServerMessage(message, messageParam == null ? null : messageParam.getParams());
    }

    public static String getServerMessage(String message) {
        return getServerMessage(message, (Map<String, String>) null);
    }

    public static String getServerMessage(String message, String param0, String value0) {
        return getServerMessage(message, Collections.singletonMap(param0, value0));
    }

    public static String getServerMessage(String message, Map<String, String> params) {
        if (StringUtils.isEmpty(message)) {
            return message;
        }
        ObjectNode langJson = getJson(null, true);
        String text = StringUtils.defaultIfEmpty(langJson.at("/" + message.replaceAll("\\.", "/")).textValue(), message);
        return params == null ? text : StrSubstitutor.replace(text, params, "{{", "}}");
    }

    private static ObjectNode getJson(String lang, boolean isServer) {
        lang = lang == null ? StringUtils.defaultString(CURRENT_LANG, DEFAULT_LANG) : lang;
        String key = lang + (isServer ? "_server" : "");
        if (!i18nLang.containsKey(key)) {
            i18nLang.put(key, TouchHomeUtils.readAndMergeJSON("i18n/" + key + ".json",
                    TouchHomeUtils.OBJECT_MAPPER.createObjectNode()));
        }
        return i18nLang.get(key);
    }
}
