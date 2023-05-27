package org.homio.api.setting;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Collection;
import org.homio.api.EntityContext;
import org.homio.api.model.OptionModel;
import org.homio.api.ui.field.UIFieldType;
import org.json.JSONObject;

public interface SettingPluginOptionsPort extends SettingPlugin<SerialPort>, SettingPluginOptions<SerialPort> {

    @Override
    default UIFieldType getSettingType() {
        return UIFieldType.SelectBoxDynamic;
    }

    @Override
    default Collection<OptionModel> getOptions(EntityContext entityContext, JSONObject params) {
        return OptionModel.listOfPorts(withEmpty());
    }

    default boolean withEmpty() {
        return false;
    }

    @Override
    default String writeValue(SerialPort value) {
        return value == null ? "" : value.getSystemPortName();
    }

    @Override
    default Class<SerialPort> getType() {
        return SerialPort.class;
    }

    @Override
    default boolean lazyLoad() {
        return true;
    }
}
