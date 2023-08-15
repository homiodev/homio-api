package org.homio.api.entity.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import org.homio.api.entity.device.DeviceBaseEntity;
import org.homio.api.exception.ProhibitedExecution;
import org.homio.api.ui.UISidebarMenu;
import org.homio.api.ui.field.UIFieldIgnore;

@Entity
@UISidebarMenu(icon = "fas fa-puzzle-piece", order = 100, bg = "#939E18", allowCreateNewItems = true, overridePath = "misc")
public abstract class MiscEntity extends DeviceBaseEntity {

    @Override
    @JsonIgnore
    @UIFieldIgnore
    public String getPlace() {
        throw new ProhibitedExecution();
    }
}
