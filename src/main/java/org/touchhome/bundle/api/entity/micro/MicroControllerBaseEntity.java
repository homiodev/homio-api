package org.touchhome.bundle.api.entity.micro;

import lombok.extern.log4j.Log4j2;
import org.touchhome.bundle.api.entity.DeviceBaseEntity;
import org.touchhome.bundle.api.service.scan.BaseBeansItemsDiscovery;
import org.touchhome.bundle.api.ui.UISidebarButton;
import org.touchhome.bundle.api.ui.UISidebarMenu;

import static org.touchhome.bundle.api.entity.micro.MicroControllerBaseEntity.MicroControllersDiscovery;

@Log4j2
@UISidebarMenu(icon = "fas fa-microchip", parent = UISidebarMenu.TopSidebarMenu.HARDWARE, order = 5,
        bg = "#7482d0", allowCreateNewItems = true, overridePath = "controllers")
@UISidebarButton(buttonIcon = "fas fa-qrcode", confirm = "TITLE.SCAN_CONTROLLERS", buttonIconColor = "#7482D0",
        buttonTitle = "TITLE.SCAN_CONTROLLERS", handlerClass = MicroControllersDiscovery.class)
public abstract class MicroControllerBaseEntity<T extends MicroControllerBaseEntity> extends DeviceBaseEntity<T> {

    public static class MicroControllersDiscovery extends BaseBeansItemsDiscovery {

        public MicroControllersDiscovery() {
            super(MicroControllerScanner.class);
        }
    }
}
