package org.homio.api.setting;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Collection;
import org.homio.api.Context;
import org.homio.api.model.OptionModel;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public interface SettingPluginOptionsPort extends SettingPlugin<SerialPort>, SettingPluginOptions<SerialPort> {

    @Override
    default @NotNull SettingType getSettingType() {
        return SettingType.SelectBoxDynamic;
    }

    @Override
    default @NotNull Collection<OptionModel> getOptions(Context context, JSONObject params) {
        return OptionModel.listOfPorts(withEmpty());
    }

    default boolean withEmpty() {
        return false;
    }

    @Override
    default @NotNull String writeValue(SerialPort value) {
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
