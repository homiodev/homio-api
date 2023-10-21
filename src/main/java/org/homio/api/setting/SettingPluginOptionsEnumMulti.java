package org.homio.api.setting;

import static org.homio.api.entity.HasJsonData.LIST_DELIMITER;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.homio.api.Context;
import org.homio.api.model.OptionModel;
import org.homio.api.model.OptionModel.KeyValueEnum;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public interface SettingPluginOptionsEnumMulti<T extends Enum<T>> extends SettingPluginOptions<Set<T>> {

    T[] defaultValue();

    @Override
    default @NotNull Class<Set<T>> getType() {
        return (Class<Set<T>>) Collections.<T>emptySet().getClass();
    }

    Class<T> getEnumType();

    @Override
    default @NotNull String getDefaultValue() {
        Set<String> values = new HashSet<>();
        for (T value : defaultValue()) {
            values.add(value.name());
        }
        return String.join(LIST_DELIMITER, values);
    }

    @Override
    default @NotNull String writeValue(@Nullable Set<T> value) {
        return value == null ? "" : value.stream().map(Enum::name).collect(Collectors.joining(LIST_DELIMITER));
    }

    @Override
    default Set<T> parseValue(Context context, String value) {
        if (value == null) {
            return Collections.emptySet();
        }
        return Stream.of(value.split(LIST_DELIMITER)).map(s -> {
            for (T item : getEnumType().getEnumConstants()) {
                if (s.equals(item.name())) {
                    return item;
                }
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    default @NotNull Collection<OptionModel> getOptions(Context context, JSONObject params) {
        if (KeyValueEnum.class.isAssignableFrom(getType())) {
            return OptionModel.list((Class<? extends KeyValueEnum>) getEnumType());
        }
        return OptionModel.enumList(getEnumType());
    }
}
