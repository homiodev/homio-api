package org.homio.api.entity.device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.homio.api.entity.BaseEntity;
import org.homio.api.model.device.ConfigDeviceDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Uses in case if we don't need device endpoint configurations
 */
public interface DeviceEndpointsBehaviourContractStub extends DeviceEndpointsBehaviourContract {

  @JsonIgnore
  default @NotNull String getDeviceFullName() {
    if (this instanceof BaseEntity be) {
      return be.getTitle();
    }
    return "-";
  }

  default @NotNull List<ConfigDeviceDefinition> findMatchDeviceConfigurations() {
    return List.of();
  }
}
