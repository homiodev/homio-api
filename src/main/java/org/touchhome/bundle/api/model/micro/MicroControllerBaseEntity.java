package org.touchhome.bundle.api.model.micro;

import lombok.extern.log4j.Log4j2;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.DeviceBaseEntity;
import org.touchhome.bundle.api.ui.UISidebarButton;
import org.touchhome.bundle.api.ui.UISidebarMenu;
import org.touchhome.bundle.api.ui.action.UISidebarButtonHandler;
import org.touchhome.bundle.api.util.FlowMap;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.touchhome.bundle.api.model.micro.MicroControllerBaseEntity.ScanMicroControllers;
import static org.touchhome.bundle.api.util.TouchHomeUtils.getErrorMessage;

@Log4j2
@UISidebarMenu(icon = "fas fa-microchip", parent = UISidebarMenu.TopSidebarMenu.HARDWARE, order = 5,
        bg = "#7482d0", allowCreateNewItems = true)
@UISidebarButton(buttonIcon = "fas fa-qrcode", confirm = "ACTION.SCAN_CONTROLLERS", buttonIconColor = "#7482D0",
        onDone = "ACTION.SCAN_CONTROLLERS_STARTED", buttonTitle = "TITLE.SCAN_CONTROLLERS", handlerClass = ScanMicroControllers.class)
public abstract class MicroControllerBaseEntity<T extends MicroControllerBaseEntity> extends DeviceBaseEntity<T> {
    private static AtomicBoolean started = new AtomicBoolean();

    public static class ScanMicroControllers implements UISidebarButtonHandler {

        @Override
        public void accept(EntityContext entityContext) {
            if (started.compareAndSet(false, true)) {
                entityContext.run("scan-micro-controllers", () -> {
                    log.info("Start scan for controllers");
                    try {
                        int foundDevicesCount = 0;
                        for (MicroControllerScanner microControllerScanner : entityContext.getBeansOfType(MicroControllerScanner.class)) {
                            foundDevicesCount += microControllerScanner.scan();
                        }
                        entityContext.sendInfoMessage("FOUND_CONTROLLER_RESULT", FlowMap.of("COUNT", String.valueOf(foundDevicesCount)));
                    } catch (Exception ex) {
                        entityContext.sendErrorMessage("SCAN_CONTROLLER_ERROR", FlowMap.of("MSG", getErrorMessage(ex)), ex);
                    } finally {
                        log.info("Done scan for controllers");
                        started.set(false);
                    }
                }, true);
            } else {
                entityContext.sendErrorMessage("SCAN_CONTROLLER_ALREADY_STARTED");
            }
        }
    }
}
