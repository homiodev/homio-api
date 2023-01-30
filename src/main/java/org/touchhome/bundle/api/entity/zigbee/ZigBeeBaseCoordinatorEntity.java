package org.touchhome.bundle.api.entity.zigbee;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fazecast.jSerialComm.SerialPort;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.entity.HasJsonData;
import org.touchhome.bundle.api.entity.HasStatusAndMsg;
import org.touchhome.bundle.api.model.HasEntityIdentifier;
import org.touchhome.bundle.api.model.HasEntityLog;
import org.touchhome.bundle.api.service.EntityService;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.UIFieldGroup;
import org.touchhome.bundle.api.ui.field.UIFieldSlider;
import org.touchhome.bundle.api.ui.field.selection.UIFieldDevicePortSelection;
import org.touchhome.bundle.api.ui.field.selection.UIFieldSelectNoValue;
import org.touchhome.bundle.api.ui.field.selection.UIFieldSelectValueOnEmpty;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.util.Collection;
import java.util.Map;

public interface ZigBeeBaseCoordinatorEntity<T extends ZigBeeBaseCoordinatorEntity, S extends EntityService.ServiceInstance>
        extends HasJsonData, HasEntityLog, HasEntityIdentifier, HasStatusAndMsg<T>, EntityService<S, T> {

    @UIField(order = 1, inlineEdit = true)
    @UIFieldGroup(value = "General", order = 1)
    default boolean isStart() {
        return getJsonData("start", false);
    }

    default T setStart(boolean start) {
        setJsonData("start", start);
        return (T) this;
    }

    @UIField(order = 3, required = true)
    @UIFieldDevicePortSelection
    @UIFieldSelectValueOnEmpty(label = "selection.selectPort", icon = "fas fa-door-open")
    @UIFieldSelectNoValue("error.noPortsAvailable")
    @UIFieldGroup(value = "Port", order = 5, borderColor = "#29A397")
    default String getPort() {
        return getJsonData("port", "");
    }

    default T setPort(String value) {
        SerialPort serialPort = TouchHomeUtils.getSerialPort(value);
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
    @UIFieldGroup(value = "Discovery", order = 15, borderColor = "#663453")
    default int getDiscoveryDuration() {
        return getJsonData("dd", 254);
    }

    default void setDiscoveryDuration(int value) {
        setJsonData("dd", value);
    }

    /**
     * Return all available properties
     * Key - device's ieeeAddress
     * Value - Map<propertyName, property
     */
    @JsonIgnore
    @NotNull Map<String, Map<String, ? extends ZigBeeProperty>> getCoordinatorTree();

    /**
     * Return all zigbee devices registered in this coordinator
     */
    @JsonIgnore
    @NotNull Collection<ZigBeeDeviceBaseEntity> getZigBeeDevices();

    @JsonIgnore
    @Nullable ZigBeeDeviceBaseEntity getZigBeeDevice(@NotNull String ieeeAddress);
}
