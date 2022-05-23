package org.touchhome.bundle.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.touchhome.bundle.api.util.SecureString;
import org.touchhome.common.util.CommonUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface HasJsonData<T> {

    @JsonIgnore
    JSONObject getJsonData();

    default <P> T setJsonData(String key, P value) {
        getJsonData().put(key, value);
        return (T) this;
    }

    default Integer getJsonData(String key, int defaultValue) {
        return getJsonData().optInt(key, defaultValue);
    }

    @SneakyThrows
    default <T> T getJsonData(String key, Class<T> classType) {
        if (getJsonData().has(key)) {
            return CommonUtils.OBJECT_MAPPER.readValue(getJsonData(key), classType);
        }
        return null;
    }

    default <E extends Enum> E getJsonDataEnum(String key, E defaultValue) {
        String jsonData = getJsonData(key);

        E[] enumConstants = (E[]) defaultValue.getDeclaringClass().getEnumConstants();
        for (E enumValue : enumConstants) {
            if (enumValue.name().equals(jsonData)) {
                return enumValue;
            }
        }
        return defaultValue;
    }

    default <E extends Enum> T setJsonDataEnum(String key, E value) {
        setJsonData(key, value == null ? "" : value.name());
        return (T) this;
    }

    default Boolean getJsonData(String key, boolean defaultValue) {
        return getJsonData().optBoolean(key, defaultValue);
    }

    default String getJsonData(String key, String defaultValue) {
        return getJsonData().optString(key, defaultValue);
    }

    default List<String> getJsonDataList(String key) {
        return getJsonDataList(key, "~~~");
    }

    default List<String> getJsonDataList(String key, String delimiter) {
        return Stream.of(getJsonData().optString(key, "").split(delimiter))
                .filter(StringUtils::isNotEmpty).collect(Collectors.toList());
    }

    default Long getJsonData(String key, long defaultValue) {
        return getJsonData().optLong(key, defaultValue);
    }

    default String getJsonData(String key) {
        return getJsonData().optString(key);
    }

    default SecureString getJsonSecure(String key) {
        return new SecureString(getJsonData(key));
    }

    default SecureString getJsonSecure(String key, String defaultValue) {
        return new SecureString(getJsonData(key, defaultValue));
    }

    default Double getJsonData(String key, double defaultValue) {
        return getJsonData().optDouble(key, defaultValue);
    }
}
