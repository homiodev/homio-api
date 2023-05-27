package org.homio.api.entity.types;

import jakarta.persistence.Entity;
import org.homio.api.ui.UISidebarMenu;
import org.homio.api.entity.DeviceBaseEntity;

@Entity
@UISidebarMenu(icon = "fas fa-microchip", parent = UISidebarMenu.TopSidebarMenu.HARDWARE, order = 5,
               bg = "#7482d0", allowCreateNewItems = true, overridePath = "controllers")
public abstract class MicroControllerBaseEntity<T extends MicroControllerBaseEntity> extends DeviceBaseEntity<T> {

}
