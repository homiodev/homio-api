package org.homio.api.setting;

import com.fazecast.jSerialComm.SerialPort;
import org.homio.api.Context;
import org.homio.api.model.OptionModel;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Collection;

public interface SettingPluginOptionsPort extends SettingPlugin<SerialPort>, SettingPluginOptions<SerialPort> {

    @Override
    default @NotNull SettingType getSettingType() {
        return viewAsButton() ? SettingType.SelectBoxButton : SettingType.SelectBox;
    }

    @Override
    default @NotNull Collection<OptionModel> getOptions(Context context, JSONObject params) {
        return OptionModel.listOfPorts(withEmpty());
    }

    default boolean withEmpty() {
        return false;
    }

    @Override
    default @NotNull String serializeValue(SerialPort value) {
        return value == null ? "" : value.getSystemPortName();
    }

    @Override
    default @NotNull Class<SerialPort> getType() {
        return SerialPort.class;
    }

    @Override
    default boolean lazyLoad() {
        return true;
    }
}
