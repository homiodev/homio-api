package org.touchhome.bundle.api.setting;

import org.touchhome.bundle.api.EntityContext;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface SettingPluginTextSet extends SettingPlugin<Set<String>> {

    @Override
    default Class<Set<String>> getType() {
        return (Class<Set<String>>) Collections.<String>emptySet().getClass();
    }

    String[] defaultValue();

    @Override
    default String getDefaultValue() {
        return String.join("~~~", defaultValue());
    }

    @Override
    default Set<String> parseValue(EntityContext entityContext, String value) {
        return value == null ? Collections.emptySet() : Stream.of(value.split("~~~")).collect(Collectors.toSet());
    }

    @Override
    default String writeValue(Set<String> value) {
        return value.stream().collect(Collectors.joining("~~~"));
    }

    @Override
    default SettingType getSettingType() {
        return SettingType.Chips;
    }
}
