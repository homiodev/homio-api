package org.homio.bundle.api.setting;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.ui.field.UIFieldType;

public interface SettingPluginIntegerSet extends SettingPlugin<Set<Integer>> {

    @Override
    default Class<Set<Integer>> getType() {
        return (Class<Set<Integer>>) Collections.<Integer>emptySet().getClass();
    }

    int[] defaultValue();

    @Override
    default String getDefaultValue() {
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
    default String writeValue(Set<Integer> value) {
        return value.stream().map(Object::toString).collect(Collectors.joining("~~~"));
    }

    @Override
    default UIFieldType getSettingType() {
        return UIFieldType.Chips;
    }
}
