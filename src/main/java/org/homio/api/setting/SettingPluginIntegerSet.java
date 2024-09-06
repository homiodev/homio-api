package org.homio.api.setting;

import org.homio.api.Context;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.homio.api.entity.HasJsonData.LIST_DELIMITER;

public interface SettingPluginIntegerSet extends SettingPlugin<Set<Integer>> {

    @Override
    default @NotNull Class<Set<Integer>> getType() {
        return (Class<Set<Integer>>) Collections.<Integer>emptySet().getClass();
    }

    int[] defaultValue();

    @Override
    default @NotNull String getDefaultValue() {
        Set<String> values = new HashSet<>();
        for (int value : defaultValue()) {
            values.add(String.valueOf(value));
        }
        return String.join(LIST_DELIMITER, values);
    }

    @Override
    default Set<Integer> deserializeValue(Context context, String value) {
        if (value == null) {
            return Collections.emptySet();
        }
        return Stream.of(value.split(LIST_DELIMITER)).map(Integer::parseInt).collect(Collectors.toSet());
    }

    @Override
    default @NotNull String serializeValue(Set<Integer> value) {
        return value.stream().map(Object::toString).collect(Collectors.joining(LIST_DELIMITER));
    }

    @Override
    default @NotNull SettingType getSettingType() {
        return SettingType.Chips;
    }
}
