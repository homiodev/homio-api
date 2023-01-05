package org.touchhome.bundle.api.entity.types;

import lombok.extern.log4j.Log4j2;
import org.touchhome.bundle.api.entity.DeviceBaseEntity;
import org.touchhome.bundle.api.ui.UISidebarMenu;

@Log4j2
@UISidebarMenu(icon = "fas fa-microchip", parent = UISidebarMenu.TopSidebarMenu.HARDWARE, order = 5,
        bg = "#7482d0", allowCreateNewItems = true, overridePath = "controllers")
public abstract class MicroControllerBaseEntity<T extends MicroControllerBaseEntity> extends DeviceBaseEntity<T> {

}
