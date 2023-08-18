package org.homio.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.EntityContext;
import org.homio.api.model.JSON;
import org.homio.api.util.SecureString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.homio.api.util.JsonUtils.OBJECT_MAPPER;

public interface HasJsonData {

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

    default <P> void setJsonData(@NotNull String key, @Nullable P value) {
        if (value == null || value.toString().isEmpty()) {
            getJsonData().remove(key);
        }
        getJsonData().put(key, value);
    }

    @SneakyThrows
    default <P> void setJsonDataObject(@NotNull String key, @Nullable P value) {
        if (value == null || value.toString().isEmpty()) {
            getJsonData().remove(key);
        }
        getJsonData().put(key, OBJECT_MAPPER.writeValueAsString(value));
    }

    default <P> void setJsonDataSecure(@NotNull String key, @Nullable P value) {
        if (value == null || value.toString().isEmpty()) {
            getJsonData().remove(key);
        }
        // ignore if editing and pass 'Secure:XXXXX' to save
        if ("Secure:XXXXX".equals(value)) {
            return;
        }
        getJsonData().put(key, value);
    }

    default <P> void setJsonData(@NotNull String key, @Nullable Integer value, int defaultValue, int min, int max) {
        if (value == null || value == defaultValue) {
            getJsonData().remove(key);
        } else if (value > max || value < min) {
            throw new IllegalArgumentException(format("Value: '%s' must be in range: %s..%s", value, min, max));
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
    default <T> @Nullable T getJsonData(@NotNull String key, @NotNull Class<T> classType) {
        if (getJsonData().has(key)) {
            try {
                return OBJECT_MAPPER.readValue(getJsonData(key), classType);
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    default <E extends Enum> @NotNull E getJsonDataEnum(@NotNull String key, @NotNull E defaultValue) {
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
        return getJsonDataList(key, "~~~");
    }

    default @NotNull List<String> getJsonDataList(@NotNull String key, @NotNull String delimiter) {
        return getJsonDataStream(key, delimiter).collect(Collectors.toList());
    }

    default @NotNull Set<String> getJsonDataSet(@NotNull String key) {
        return getJsonDataSet(key, "~~~");
    }

    default @NotNull Set<String> getJsonDataSet(@NotNull String key, @NotNull String delimiter) {
        return getJsonDataStream(key, delimiter).collect(Collectors.toSet());
    }

    default @NotNull Stream<String> getJsonDataStream(@NotNull String key, @NotNull String delimiter) {
        return Stream.of(getJsonData().optString(key, "").split(delimiter))
                .filter(StringUtils::isNotEmpty);
    }

    default @NotNull Long getJsonData(@NotNull String key, long defaultValue) {
        return getJsonData().optLong(key, defaultValue);
    }

    default @NotNull String getJsonData(@NotNull String key) {
        return getJsonData().optString(key);
    }

    default String getJsonDataEntity(String key, EntityContext entityContext) {
        String value = getJsonData(key);
        if (isNotEmpty(value)) {
            BaseEntity entity = entityContext.getEntity(value);
            if (entity != null) {
                return entity.getEntityID() + "~~~" + entity.getTitle();
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
