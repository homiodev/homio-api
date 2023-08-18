package org.homio.api.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static org.apache.commons.lang3.StringUtils.trimToNull;

public class JsonUtils {

    public static final ObjectMapper OBJECT_MAPPER;
    public static final ObjectMapper YAML_OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        YAML_OBJECT_MAPPER = new ObjectMapper(new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @SneakyThrows
    public static <T> T readAndMergeJSON(String resource, T targetObject) {
        ObjectReader updater = OBJECT_MAPPER.readerForUpdating(targetObject);
        for (URL url : Collections.list(CommonUtils.class.getClassLoader().getResources(resource))) {
            updater.readValue(url);
        }
        return targetObject;
    }

    @SneakyThrows
    public static <T> List<T> readJSON(String resource, Class<T> targetClass) {
        Enumeration<URL> resources = CommonUtils.class.getClassLoader().getResources(resource);
        List<T> list = new ArrayList<>();
        while (resources.hasMoreElements()) {
            list.add(OBJECT_MAPPER.readValue(resources.nextElement(), targetClass));
        }
        return list;
    }

    public static boolean hasJsonPath(@NotNull JsonNode node, @NotNull String path) {
        return !getJsonPath(node, path).isMissingNode();
    }

    public static @NotNull JsonNode getJsonPath(@NotNull JsonNode node, @NotNull String path) {
        JsonNode cursor = node;
        for (String item : path.split("/")) {
            cursor = cursor.path(item);
        }
        return cursor;
    }

    /**
     * Update node value if not match expected value. Create missing nodes.
     *
     * @param node            target node to modify
     * @param path            - full path to value. i.e.: 'my/path/to/value'
     * @param requireUpdateFn - if require to update
     * @param updateFn        - function to update value
     * @return if node was updated
     */
    public static boolean updateJsonPath(@NotNull JsonNode node, @NotNull String path, @NotNull Predicate<JsonNode> requireUpdateFn,
                                         @NotNull BiConsumer<ObjectNode, String> updateFn) {
        JsonNode cursor = node;
        JsonNode parent = null;
        String[] pathItems = path.split("/");
        for (String item : pathItems) {
            if (!cursor.has(item)) {
                ((ObjectNode) cursor).set(item, OBJECT_MAPPER.createObjectNode());
            }
            parent = cursor;
            cursor = cursor.path(item);
        }
        if (requireUpdateFn.test(cursor)) {
            updateFn.accept((ObjectNode) parent, pathItems[pathItems.length - 1]);
            return true;
        }
        return false;
    }

    /**
     * Update node value if not match expected integer value.
     *
     * @param jsonNode target node
     * @param path     - path
     * @param value    - expected value
     * @return if node was updated
     */
    public static boolean updateJsonPath(@NotNull JsonNode jsonNode, @NotNull String path, int value) {
        return updateJsonPath(jsonNode, path, node -> node.asInt(-1) != value, (node, s) -> node.put(s, value));
    }

    public static JSONObject putOpt(JSONObject jsonObject, String key, String value) {
        return putOpt(jsonObject, key, (Object) trimToNull(value));
    }

    public static JSONObject putOpt(JSONObject jsonObject, String key, Object value) {
        if (StringUtils.isNotEmpty(key) && value != null) {
            jsonObject.put(key, value);
        }
        return jsonObject;
    }

    public static boolean isValidJson(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException ignore) {
            try {
                new JSONArray(json);
            } catch (JSONException ne) {
                return false;
            }
        }
        return true;
    }
}
