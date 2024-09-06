package org.homio.api.entity.device;

import org.homio.api.entity.BaseEntityIdentifier;
import org.homio.api.entity.HasJsonData;
import org.homio.api.entity.HasStatusAndMsg;
import org.homio.api.model.HasEntityIdentifier;
import org.homio.api.model.Status;
import org.homio.api.model.Status.EntityStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DeviceContract extends HasJsonData, HasEntityIdentifier, BaseEntityIdentifier, HasStatusAndMsg {

    String getIeeeAddress();

    void setIeeeAddress(String value);

    default String getModel() {
        return getJsonData("model", "");
    }

    default void setModel(String value) {
        setJsonData("model", value);
    }

    /**
     * Uses on UI to set png image with appropriate status and mark extra image if need
     */
    default @Nullable Status.EntityStatus getEntityStatus() {
        Status status = getStatus();
        return new EntityStatus(status);
    }

    // May be required for @UIFieldColorBgRef("statusColor")
    default @NotNull String getStatusColor() {
        EntityStatus entityStatus = getEntityStatus();
        if (entityStatus == null || entityStatus.getValue().isOnline()) {
            return "";
        }
        return entityStatus.getColor() + "30";
    }
}
