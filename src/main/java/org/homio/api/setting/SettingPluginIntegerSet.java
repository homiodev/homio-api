package org.homio.api.setting;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.homio.api.EntityContext;
import org.jetbrains.annotations.NotNull;

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
        return String.join("~~~", values);
    }

    @Override
    default Set<Integer> parseValue(EntityContext entityContext, String value) {
        if (value == null) {
            return Collections.emptySet();
        }
        return Stream.of(value.split("~~~")).map(Integer::parseInt).collect(Collectors.toSet());
    }

    @Override
    default @NotNull String writeValue(Set<Integer> value) {
        return value.stream().map(Object::toString).collect(Collectors.joining("~~~"));
    }

    @Override
    default @NotNull SettingType getSettingType() {
        return SettingType.Chips;
    }
}
