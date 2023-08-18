package org.homio.api.setting;

import com.fazecast.jSerialComm.SerialPort;
import org.homio.api.EntityContext;
import org.homio.api.model.OptionModel;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Collection;

public interface SettingPluginOptionsPort extends SettingPlugin<SerialPort>, SettingPluginOptions<SerialPort> {

    @Override
    default @NotNull SettingType getSettingType() {
        return SettingType.SelectBoxDynamic;
    }

    @Override
    default @NotNull Collection<OptionModel> getOptions(EntityContext entityContext, JSONObject params) {
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
