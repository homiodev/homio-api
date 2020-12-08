package org.touchhome.bundle.api.setting;

import com.fazecast.jSerialComm.SerialPort;
import org.apache.commons.lang3.StringUtils;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.json.NotificationEntityJSON;
import org.touchhome.bundle.api.json.Option;

import java.util.Collection;

public interface BundleSettingPluginPort extends BundleSettingPlugin<SerialPort> {

    String PORT_AVAILABLE = "Port available";
    String PORT_UNAVAILABLE = "Port unavailable for setting: ";

    @Override
    default SettingType getSettingType() {
        return SettingType.SelectBoxDynamic;
    }

    @Override
    default Collection<Option> loadAvailableValues(EntityContext entityContext) {
        return Option.listOfPorts(withEmpty());
    }

    default boolean withEmpty() {
        return true;
    }

    @Override
    default NotificationEntityJSON buildToastrNotificationEntity(SerialPort value, String raw, EntityContext entityContext) {
        if (StringUtils.isNotEmpty(raw)) {
            if (value == null) {
                return NotificationEntityJSON.danger(getClass().getSimpleName()).setName(raw).setValue(PORT_UNAVAILABLE + getClass().getSimpleName());
            } else {
                return NotificationEntityJSON.info(getClass().getSimpleName()).setName(raw).setValue(PORT_AVAILABLE);
            }
        }
        return null;
    }

    @Override
    default String writeValue(SerialPort value) {
        return value == null ? "" : value.getSystemPortName();
    }

    @Override
    default Class<SerialPort> getType() {
        return SerialPort.class;
    }
}
