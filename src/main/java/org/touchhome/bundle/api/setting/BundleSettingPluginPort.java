package org.touchhome.bundle.api.setting;

import com.fazecast.jSerialComm.SerialPort;
import org.apache.commons.lang3.StringUtils;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.json.NotificationEntityJSON;

public interface BundleSettingPluginPort extends BundleSettingPlugin<SerialPort> {

    String PORT_AVAILABLE = "Port available";
    String PORT_UNAVAILABLE = "Port unavailable for setting: ";

    @Override
    default SettingType getSettingType() {
        return SettingType.SelectBoxDynamic;
    }

    @Override
    default NotificationEntityJSON buildToastrNotificationEntity(SerialPort value, String raw, EntityContext entityContext) {
        if (StringUtils.isNotEmpty(raw)) {
            if (value == null) {
                return NotificationEntityJSON.danger(getClass().getSimpleName()).setName(raw).setDescription(PORT_UNAVAILABLE + getClass().getSimpleName());
            } else {
                return NotificationEntityJSON.info(getClass().getSimpleName()).setName(raw).setDescription(PORT_AVAILABLE);
            }
        }
        return null;
    }

    @Override
    default Class<SerialPort> getType() {
        return SerialPort.class;
    }
}
