package org.homio.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.type.MapType;
import com.pivovarit.function.ThrowingConsumer;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.Context;
import org.homio.api.model.JSON;
import org.homio.api.util.CommonUtils;
import org.homio.api.util.SecureString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.homio.api.util.JsonUtils.OBJECT_MAPPER;

public interface HasJsonData {

    String LIST_DELIMITER = "~~~";
    String LEVEL_DELIMITER = "-->";

    @JsonIgnore
    @NotNull
    JSON getJsonData();

    default long getJsonDataHashCode(@Nullable String key, @Nullable String... extraKeys) {
        long code = 0;
        if (key != null) {
            Object value = getJsonData().opt(key);
            code += (value == null ? 0 : value.hashCode());
        }
        if (extraKeys != null) {
            for (String extraKey : extraKeys) {
                if (extraKey != null) {
                    Object value = getJsonData().opt(extraKey);
                    code += (value == null ? 0 : value.hashCode());
                }
            }
        }
        return code;
    }

    default boolean deepEqual(HasJsonData other, String... keys) {
        for (String key : keys) {
            if (!Objects.equals(getJsonData().opt(key), other.getJsonData().opt(key))) {
                return false;
            }
        }
        return true;
    }

    default void setJsonDataAsSet(@NotNull String key, @Nullable String value) {
        if (value == null || value.isEmpty()) {
            getJsonData().remove(key);
        } else {
            value =
                    String.join(LIST_DELIMITER, new HashSet<>(Arrays.asList(value.split(LIST_DELIMITER))));
            getJsonData().put(key, value);
        }
    }

    default <P> void setJsonData(@NotNull String key, @Nullable P value) {
        if (value == null || value.toString().isEmpty()) {
            getJsonData().remove(key);
        } else {
            getJsonData().put(key, value);
        }
    }

    @SneakyThrows
    default <P> void setJsonDataObject(@NotNull String key, @Nullable P value) {
        if (value == null || value.toString().isEmpty()) {
            getJsonData().remove(key);
        } else {
            getJsonData().put(key, OBJECT_MAPPER.writeValueAsString(value));
        }
    }

    default <P> void setJsonDataSecure(@NotNull String key, @Nullable P value) {
        if (value == null || value.toString().isEmpty()) {
            getJsonData().remove(key);
            return;
        }
        // ignore if editing and pass 'Secure:XXXXX' to save
        if ("Secure:XXXXX".equals(value)) {
            return;
        }
        getJsonData().put(key, value);
    }

    default <P> void setJsonData(
            @NotNull String key, @Nullable Integer value, int defaultValue, int min, int max) {
        if (value == null || value == defaultValue) {
            getJsonData().remove(key);
        } else if (value > max || value < min) {
            throw new IllegalArgumentException(
                    format("Value: '%s' must be in range: %s..%s", value, min, max));
        } else {
            getJsonData().put(key, value);
        }
    }

    default Optional<Number> getJsonDataNumber(@NotNull String key) {
        return Optional.ofNullable(getJsonData().optNumber(key));
    }

    default int getJsonData(@NotNull String key, int defaultValue) {
        return getJsonData().optInt(key, defaultValue);
    }

    @SneakyThrows
    default <T> @Nullable T getJsonData(
            @NotNull String key, @NotNull Class<T> classType, boolean createNew) {
        if (getJsonData().has(key)) {
            try {
                return OBJECT_MAPPER.readValue(getJsonData(key), classType);
            } catch (Exception ignore) {
            }
        }
        return createNew ? CommonUtils.newInstance(classType) : null;
    }

    @SneakyThrows
    default <T> @NotNull List<T> getJsonDataList(@NotNull String key, @NotNull Class<T> classType) {
        if (getJsonData().has(key)) {
            try {
                return OBJECT_MAPPER.readValue(
                        getJsonData(key),
                        OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, classType));
            } catch (Exception ignore) {
            }
        }
        return new ArrayList<>();
    }

    @SneakyThrows
    default <T> @NotNull Set<T> getJsonDataSet(@NotNull String key, @NotNull Class<T> classType) {
        if (getJsonData().has(key)) {
            try {
                return OBJECT_MAPPER.readValue(
                        getJsonData(key),
                        OBJECT_MAPPER.getTypeFactory().constructCollectionType(Set.class, classType));
            } catch (Exception ignore) {
            }
        }
        return new HashSet<>();
    }

    @SneakyThrows
    default <T> @NotNull Map<String, T> getJsonDataMap(
            @NotNull String key, @NotNull Class<T> classType) {
        if (getJsonData().has(key)) {
            try {
                MapType mapType =
                        OBJECT_MAPPER.getTypeFactory().constructMapType(HashMap.class, String.class, classType);
                return OBJECT_MAPPER.readValue(getJsonData(key), mapType);
            } catch (Exception ignore) {
            }
        }
        return new HashMap<>();
    }

    @SneakyThrows
    default <T> void updateJsonDataMap(
            @NotNull String key,
            @NotNull Class<T> classType,
            ThrowingConsumer<Map<String, T>, Exception> updateFn) {
        Map<String, T> map = getJsonDataMap(key, classType);
        updateFn.accept(map);
        setJsonData(key, OBJECT_MAPPER.writeValueAsString(map));
    }

    default <E extends Enum> @NotNull E getJsonDataEnum(
            @NotNull String key, @NotNull E defaultValue) {
        String jsonData = getJsonData(key);

        E[] enumConstants = (E[]) defaultValue.getDeclaringClass().getEnumConstants();
        for (E enumValue : enumConstants) {
            if (enumValue.name().equals(jsonData)) {
                return enumValue;
            }
        }
        return defaultValue;
    }

    default <E extends Enum> void setJsonDataEnum(@NotNull String key, @Nullable E value) {
        setJsonData(key, value == null ? "" : value.name());
    }

    default boolean getJsonData(@NotNull String key, boolean defaultValue) {
        return getJsonData().optBoolean(key, defaultValue);
    }

    default @Nullable String getJsonData(@NotNull String key, @Nullable String defaultValue) {
        return getJsonData().optString(key, defaultValue);
    }

    default @NotNull String getJsonDataRequire(@NotNull String key, @NotNull String defaultValue) {
        return getJsonData().optString(key, defaultValue);
    }

    default @NotNull List<String> getJsonDataList(@NotNull String key) {
        return getJsonDataList(key, LIST_DELIMITER);
    }

    default void setJsonDataList(@NotNull String key, @Nullable Collection<String> values) {
        setJsonData(key, values == null ? "" : String.join(LIST_DELIMITER, values));
    }

    default @NotNull List<String> getJsonDataList(@NotNull String key, @NotNull String delimiter) {
        return getJsonDataStream(key, delimiter).collect(Collectors.toList());
    }

    default @NotNull Set<String> getJsonDataSet(@NotNull String key) {
        return getJsonDataSet(key, LIST_DELIMITER);
    }

    default @NotNull Set<String> getJsonDataSet(@NotNull String key, @NotNull String delimiter) {
        return getJsonDataStream(key, delimiter).collect(Collectors.toSet());
    }

    default @NotNull Stream<String> getJsonDataStream(
            @NotNull String key, @NotNull String delimiter) {
        return Stream.of(getJsonData().optString(key, "").split(delimiter))
                .filter(StringUtils::isNotEmpty);
    }

    default @NotNull Long getJsonData(@NotNull String key, long defaultValue) {
        return getJsonData().optLong(key, defaultValue);
    }

    default @NotNull String getJsonData(@NotNull String key) {
        return getJsonData().optString(key);
    }

    default String getJsonDataEntity(String key, Context context) {
        String value = getJsonData(key);
        if (isNotEmpty(value)) {
            BaseEntity entity = context.db().get(value);
            if (entity != null) {
                return entity.getEntityID() + LIST_DELIMITER + entity.getTitle();
            }
        }
        return value;
    }

    default @NotNull SecureString getJsonSecure(@NotNull String key) {
        return new SecureString(getJsonData(key));
    }

    default @NotNull SecureString getJsonSecure(@NotNull String key, @NotNull String defaultValue) {
        return new SecureString(getJsonData(key, defaultValue));
    }

    default double getJsonData(@NotNull String key, double defaultValue) {
        return getJsonData().optDouble(key, defaultValue);
    }
}
