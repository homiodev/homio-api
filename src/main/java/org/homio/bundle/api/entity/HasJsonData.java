package org.homio.bundle.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.homio.bundle.api.model.JSON;
import org.homio.bundle.api.util.SecureString;
import org.homio.bundle.api.util.TouchHomeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HasJsonData {

    @JsonIgnore
    @NotNull
    JSON getJsonData();

    default <P> void setJsonData(@NotNull String key, @Nullable P value) {
        getJsonData().put(key, value);
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
            return TouchHomeUtils.OBJECT_MAPPER.readValue(getJsonData(key), classType);
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
