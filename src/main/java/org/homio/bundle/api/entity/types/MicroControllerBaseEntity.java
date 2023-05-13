package org.homio.bundle.api.entity.types;

import jakarta.persistence.Entity;
import org.homio.bundle.api.entity.DeviceBaseEntity;
import org.homio.bundle.api.ui.UISidebarMenu;

@Entity
@UISidebarMenu(icon = "fas fa-microchip", parent = UISidebarMenu.TopSidebarMenu.HARDWARE, order = 5,
               bg = "#7482d0", allowCreateNewItems = true, overridePath = "controllers")
public abstract class MicroControllerBaseEntity<T extends MicroControllerBaseEntity> extends DeviceBaseEntity<T> {

}
