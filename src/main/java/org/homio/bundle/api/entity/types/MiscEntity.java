package org.homio.bundle.api.entity.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.homio.bundle.api.entity.DeviceBaseEntity;
import org.homio.bundle.api.exception.ProhibitedExecution;
import org.homio.bundle.api.ui.UISidebarMenu;
import org.homio.bundle.api.ui.field.UIFieldIgnore;

@UISidebarMenu(icon = "fas fa-puzzle-piece", order = 100, bg = "#939E18", allowCreateNewItems = true, overridePath = "misc")
public abstract class MiscEntity<T extends MiscEntity> extends DeviceBaseEntity<T> {
    @Override
    @UIFieldIgnore
    @JsonIgnore
    public String getPlace() {
        throw new ProhibitedExecution();
    }
}
