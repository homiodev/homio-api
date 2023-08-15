package org.homio.api.entity.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import org.homio.api.entity.device.DeviceBaseEntity;
import org.homio.api.exception.ProhibitedExecution;
import org.homio.api.ui.UISidebarMenu;
import org.homio.api.ui.field.UIFieldIgnore;

@Entity
@UISidebarMenu(icon = "fas fa-database", order = 200, bg = "#8B2399", allowCreateNewItems = true, overridePath = "storage")
public abstract class StorageEntity extends DeviceBaseEntity {

    @Override
    @JsonIgnore
    @UIFieldIgnore
    public String getPlace() {
        throw new ProhibitedExecution();
    }
}
