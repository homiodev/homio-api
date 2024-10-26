package org.homio.api.entity.types;

import jakarta.persistence.Entity;
import org.homio.api.entity.device.DeviceBaseEntity;
import org.homio.api.ui.UISidebarMenu;

@Entity
@UISidebarMenu(order = 900, icon = "fas fa-puzzle-piece", bg = "#939E18", allowCreateNewItems = true, overridePath = "misc")
public abstract class MiscEntity extends DeviceBaseEntity {

}
