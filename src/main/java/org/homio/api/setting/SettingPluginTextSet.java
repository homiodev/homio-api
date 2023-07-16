package org.homio.api.setting;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.homio.api.EntityContext;
import org.jetbrains.annotations.NotNull;

public interface SettingPluginTextSet extends SettingPlugin<Set<String>> {

    @Override
    default @NotNull Class<Set<String>> getType() {
        return (Class<Set<String>>) Collections.<String>emptySet().getClass();
    }

    String[] defaultValue();

    default String getPattern() {
        return null;
    }

    @Override
    default @NotNull String getDefaultValue() {
        return String.join("~~~", defaultValue());
    }

    @Override
    default Set<String> parseValue(EntityContext entityContext, String value) {
        return value == null ? Collections.emptySet() : Stream.of(value.split("~~~")).collect(Collectors.toSet());
    }

    @Override
    default @NotNull String writeValue(Set<String> value) {
        return value.stream().collect(Collectors.joining("~~~"));
    }

    @Override
    default @NotNull SettingType getSettingType() {
        return SettingType.Chips;
    }
}
