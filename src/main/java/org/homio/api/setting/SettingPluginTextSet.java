package org.homio.api.setting;

import static org.homio.api.entity.HasJsonData.LIST_DELIMITER;
import static org.homio.api.util.JsonUtils.putOpt;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.homio.api.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

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
        return String.join(LIST_DELIMITER, defaultValue());
    }

    @Override
    default Set<String> deserializeValue(Context context, String value) {
        return value == null ? Collections.emptySet() : Stream.of(value.split(LIST_DELIMITER)).collect(Collectors.toSet());
    }

    @Override
    default @NotNull String serializeValue(Set<String> value) {
        return String.join(LIST_DELIMITER, value);
    }

    @Override
    default @NotNull SettingType getSettingType() {
        return SettingType.Chips;
    }

    default @Nullable List<String> getMandatoryValues() {
        return null;
    }

    default @NotNull JSONObject getParameters(Context context, String value) {
        JSONObject parameters = SettingPlugin.super.getParameters(context, value);
        putOpt(parameters, "mandatoryValues", getMandatoryValues());
        return parameters;
    }
}
