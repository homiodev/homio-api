package org.touchhome.bundle.api.model;

import org.touchhome.bundle.api.ui.UISidebarMenu;

@UISidebarMenu(icon = "fas fa-microchip", parent = UISidebarMenu.TopSidebarMenu.HARDWARE, order = 5, bg = "#7482d0", allowCreateNewItems = true)
public class MicroControllerBaseEntity<T extends MicroControllerBaseEntity> extends DeviceBaseEntity<T> {
}
