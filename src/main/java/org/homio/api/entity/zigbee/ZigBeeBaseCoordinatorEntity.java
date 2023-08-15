package org.homio.api.entity.zigbee;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fazecast.jSerialComm.SerialPort;
import java.util.Collection;
import java.util.Map;
import org.homio.api.entity.HasJsonData;
import org.homio.api.entity.HasStatusAndMsg;
import org.homio.api.entity.log.HasEntityLog;
import org.homio.api.model.HasEntityIdentifier;
import org.homio.api.model.endpoint.DeviceEndpoint;
import org.homio.api.service.EntityService;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldGroup;
import org.homio.api.ui.field.UIFieldSlider;
import org.homio.api.ui.field.selection.UIFieldDevicePortSelection;
import org.homio.api.ui.field.selection.UIFieldSelectNoValue;
import org.homio.api.ui.field.selection.UIFieldSelectValueOnEmpty;
import org.homio.api.util.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ZigBeeBaseCoordinatorEntity<T extends ZigBeeBaseCoordinatorEntity, S extends EntityService.ServiceInstance>
    extends HasJsonData, HasEntityLog, HasEntityIdentifier, HasStatusAndMsg, EntityService<S, T> {

    @UIField(order = 1, inlineEdit = true)
    @UIFieldGroup(value = "GENERAL", order = 1)
    default boolean isStart() {
        return getJsonData("start", true);
    }

    default T setStart(boolean start) {
        setJsonData("start", start);
        return (T) this;
    }

    @UIField(order = 3, required = true)
    @UIFieldDevicePortSelection
    @UIFieldSelectValueOnEmpty(label = "SELECTION.SELECT_PORT", icon = "fas fa-door-open")
    @UIFieldSelectNoValue("W.ERROR.NO_PORT")
    @UIFieldGroup(value = "PORT", order = 5, borderColor = "#29A397")
    default String getPort() {
        return getJsonData("port", "");
    }

    default T setPort(String value) {
        SerialPort serialPort = CommonUtils.getSerialPort(value);
        if (serialPort != null) {
            setSerialPort(serialPort);
        }
        return (T) this;
    }

    @JsonIgnore
    default String getPortD() {
        return getJsonData("port_d", "");
    }

    default T setSerialPort(SerialPort serialPort) {
        setJsonData("port", serialPort.getSystemPortName());
        setJsonData("port_d", serialPort.getDescriptivePortName());
        return (T) this;
    }

    @UIField(order = 1)
    @UIFieldSlider(min = 60, max = 254)
    @UIFieldGroup(value = "DISCOVERY", order = 15, borderColor = "#663453")
    default int getDiscoveryDuration() {
        return getJsonData("dd", 254);
    }

    default void setDiscoveryDuration(int value) {
        setJsonData("dd", value);
    }

    /**
     * @return all available properties Key - device's ieeeAddress Value - Map[propertyName, property]
     */
    @JsonIgnore
    @NotNull Map<String, Map<String, ? extends DeviceEndpoint>> getCoordinatorTree();

    /**
     * @return all zigbee devices registered in this coordinator
     */
    @JsonIgnore
    @NotNull Collection<ZigBeeDeviceBaseEntity> getZigBeeDevices();

    @JsonIgnore
    @Nullable ZigBeeDeviceBaseEntity getZigBeeDevice(@NotNull String ieeeAddress);
}
