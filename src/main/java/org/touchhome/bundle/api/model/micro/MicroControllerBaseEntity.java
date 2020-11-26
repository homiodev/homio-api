package org.touchhome.bundle.api.model.micro;

import lombok.extern.log4j.Log4j2;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.json.ActionResponse;
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
@UISidebarButton(buttonIcon = "fas fa-qrcode", confirm = "MC.SCAN_CONTROLLERS", buttonIconColor = "#7482D0",
        buttonTitle = "TITLE.SCAN_CONTROLLERS", handlerClass = ScanMicroControllers.class)
public abstract class MicroControllerBaseEntity<T extends MicroControllerBaseEntity> extends DeviceBaseEntity<T> {
    private static AtomicBoolean started = new AtomicBoolean();

    public static class ScanMicroControllers implements UISidebarButtonHandler {

        @Override
        public ActionResponse apply(EntityContext entityContext) {
            if (started.compareAndSet(false, true)) {
                entityContext.bgp().run("scan-micro-controllers", () -> {
                    log.info("Start scan for controllers");
                    entityContext.ui().showAlwaysOnViewNotification("MC.SCAN_INFO", "fas fa-hourglass-end fa-spin", "#5A3E18");
                    try {
                        int foundDevicesCount = 0;
                        for (MicroControllerScanner microControllerScanner : entityContext.getBeansOfType(MicroControllerScanner.class)) {
                            foundDevicesCount += microControllerScanner.scan();
                        }
                        entityContext.ui().sendInfoMessage("MC.FOUND_CONTROLLER_RESULT", FlowMap.of("COUNT", String.valueOf(foundDevicesCount)));
                    } catch (Exception ex) {
                        entityContext.ui().sendErrorMessage("MC.SCAN_CONTROLLER_ERROR", FlowMap.of("MSG", getErrorMessage(ex)), ex);
                    } finally {
                        log.info("Done scan for controllers");
                        started.set(false);
                    }
                    entityContext.ui().hideAlwaysOnViewNotification("MC.SCAN_INFO");
                }, true);
            } else {
                return new ActionResponse("MC.SCAN_CONTROLLER_ALREADY_STARTED", ActionResponse.ResponseAction.ShowWarnMsg);
            }
            return new ActionResponse("MC.SCAN_CONTROLLERS_STARTED");
        }
    }
}
