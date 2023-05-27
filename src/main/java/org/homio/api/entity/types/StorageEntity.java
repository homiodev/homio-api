package org.homio.api.entity.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import org.homio.api.ui.UISidebarMenu;
import org.homio.api.ui.field.UIFieldIgnore;
import org.homio.api.entity.DeviceBaseEntity;
import org.homio.api.exception.ProhibitedExecution;

@Entity
@UISidebarMenu(icon = "fas fa-database", order = 200, bg = "#8B2399", allowCreateNewItems = true, overridePath = "storage")
public abstract class StorageEntity<T extends StorageEntity> extends DeviceBaseEntity<T> {

    @Override
    @JsonIgnore
    @UIFieldIgnore
    public String getPlace() {
        throw new ProhibitedExecution();
    }
}
