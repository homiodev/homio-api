package org.touchhome.bundle.api.manager;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.touchhome.bundle.api.NotificationMessageEntityContext;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Class provides text translate rely on en.json files
 */
public class En {
    private static final En INSTANCE = new En();
    public static String DEFAULT_LANG = "en";
    public static String CURRENT_LANG;

    private final Map<String, ObjectNode> i18nLang = new HashMap<>();

    public static En get() {
        return INSTANCE;
    }

    public void clear() {
        i18nLang.clear();
    }

    public ObjectNode getLangJson(String lang) {
        return getJson(lang, false);
    }

    public String findPathText(String name) {
        ObjectNode objectNode = getJson(null, false);
        return objectNode.at("/" + name.replaceAll("\\.", "/")).textValue();
    }

    public String getServerMessage(String message, NotificationMessageEntityContext.MessageParam messageParam) {
        return getServerMessage(message, messageParam.getParams());
    }

    public String getServerMessage(String message, Map<String, String> params) {
        ObjectNode langJson = getJson(null, true);
        String text = langJson.at("/" + message.replaceAll("\\.", "/")).textValue();
        if (params != null) {
            return StrSubstitutor.replace(text, params, "{{", "}}");
        }
        return text;
    }

    private ObjectNode getJson(String lang, boolean isServer) {
        lang = lang == null ? StringUtils.defaultString(CURRENT_LANG, DEFAULT_LANG) : lang;
        String key = lang + (isServer ? "_server" : "");
        if (!i18nLang.containsKey(key)) {
            i18nLang.put(key, TouchHomeUtils.readAndMergeJSON("i18n/" + key + ".json",
                    TouchHomeUtils.OBJECT_MAPPER.createObjectNode()));
        }
        return i18nLang.get(key);
    }
}
