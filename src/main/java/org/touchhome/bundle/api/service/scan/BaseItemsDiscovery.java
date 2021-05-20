package org.touchhome.bundle.api.service.scan;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.ActionResponseModel;
import org.touchhome.bundle.api.model.HasEntityIdentifier;
import org.touchhome.bundle.api.model.ProgressBar;
import org.touchhome.bundle.api.ui.action.UIActionHandler;
import org.touchhome.bundle.api.util.FlowMap;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.touchhome.bundle.api.util.TouchHomeUtils.getErrorMessage;

/**
 * Base class for scan devices, controllers, camera, etc...
 */
@Log4j2
public abstract class BaseItemsDiscovery implements UIActionHandler {

    protected abstract List<DevicesScanner> getScanners(EntityContext entityContext);

    protected abstract String getBatchName();

    /**
     * Max time in seconds for wait each DevicesScanner to be done.
     */
    protected int getMaxTimeToWaitInSeconds() {
        return 10 * 60;
    }

    protected String getHeaderIcon() {
        return "fas fa-hourglass-end";
    }

    protected String getHeaderIconColor() {
        return "#C6241F";
    }

    @Override
    public ActionResponseModel handleAction(EntityContext entityContext, JSONObject ignore) {
        List<DevicesScanner> scanners = getScanners(entityContext);
        if (scanners.isEmpty()) {
            return ActionResponseModel.showWarn("SCAN.NO_PROCESSES");
        }

        log.info("Start batch scanning for <{}>", getBatchName());
        String headerButtonKey = "SCAN." + getBatchName();

        // show scan button on header. All scan results may attach confirm actions to it.
        entityContext.ui().addHeaderButton(headerButtonKey, getHeaderIconColor(), null, getHeaderIcon());

        entityContext.bgp().runInBatch(getBatchName(), getMaxTimeToWaitInSeconds(), scanners,
                scanner -> {
                    log.info("Start scan in thread <{}>", scanner.name);
                    AtomicInteger status = TouchHomeUtils.getStatusMap().computeIfAbsent("scan-" + scanner.name, s -> new AtomicInteger(0));
                    if (status.compareAndSet(0, 1)) {
                        return () -> entityContext.ui().runWithProgressAndGet(scanner.name, true,
                                progressBar -> {
                                    try {
                                        return scanner.handler.handle(entityContext, progressBar, headerButtonKey);
                                    } catch (Exception ex) {
                                        log.error("Error while execute task: " + scanner.name, ex);
                                        return new DeviceScannerResult();
                                    }
                                },
                                ex -> {
                                    log.info("Done scan for <{}>", scanner.name);
                                    status.set(0);
                                    if (ex != null) {
                                        entityContext.ui().sendErrorMessage("SCAN.ERROR", FlowMap.of("MSG", getErrorMessage(ex)), ex);
                                    }
                                });
                    } else {
                        log.warn("Scan for <{}> already in progress", scanner.name);
                    }
                    return null;
                }, completedTasks -> {
                }, (Consumer<List<DeviceScannerResult>>) result -> {
                    int foundNewCount = 0;
                    int foundOldCount = 0;
                    for (DeviceScannerResult deviceScannerResult : result) {
                        foundNewCount += deviceScannerResult.newCount.get();
                        foundOldCount += deviceScannerResult.existedCount.get();
                    }
                    entityContext.ui().sendInfoMessage("SCAN.RESULT", FlowMap.of("OLD", foundOldCount, "NEW", foundNewCount));
                    // re-show header button without rotation
                    entityContext.ui().removeHeaderButton(headerButtonKey, "fas fa-poll-h", false);
                    log.info("Done batch scanning for <{}>", getBatchName());
                });
        return ActionResponseModel.showSuccess("SCAN.STARTED");
    }

    public interface DeviceScannerHandler {
        /**
         * Fires to start search for new items
         *
         * @param headerConfirmationButtonKey - special header button where confirm request to attach
         * @return found items count
         */
        DeviceScannerResult handle(EntityContext entityContext, ProgressBar progressBar, String headerConfirmationButtonKey);
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class DeviceScannerResult {
        private AtomicInteger existedCount = new AtomicInteger(0);
        private AtomicInteger newCount = new AtomicInteger(0);

        public DeviceScannerResult(int existedCount, int newCount) {
            this.existedCount.set(existedCount);
            this.newCount.set(newCount);
        }
    }

    public static class DevicesScanner implements HasEntityIdentifier {
        private final String name;
        private final DeviceScannerHandler handler;

        public DevicesScanner(String name, DeviceScannerHandler handler) {
            this.name = name;
            this.handler = handler;
        }

        @Override
        public String getEntityID() {
            return name;
        }
    }
}
