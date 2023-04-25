package org.homio.bundle.api.entity.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.homio.bundle.api.entity.DeviceBaseEntity;
import org.homio.bundle.api.exception.ProhibitedExecution;
import org.homio.bundle.api.ui.UISidebarMenu;
import org.homio.bundle.api.ui.field.UIFieldIgnore;

@UISidebarMenu(icon = "fas fa-database", order = 200, bg = "#8B2399", allowCreateNewItems = true, overridePath = "storage")
public abstract class StorageEntity<T extends StorageEntity> extends DeviceBaseEntity<T> {

    @Override
    @JsonIgnore
    @UIFieldIgnore
    public String getPlace() {
        throw new ProhibitedExecution();
    }
}
