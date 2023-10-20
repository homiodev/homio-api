package org.homio.api.service.discovery;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.homio.api.EntityContext;
import org.homio.hquery.ProgressBar;

public interface ItemDiscoverySupport {

    String getName();

    DeviceScannerResult scan(EntityContext entityContext, ProgressBar progressBar,
        String headerConfirmButtonKey);

    @Getter
    @NoArgsConstructor
    class DeviceScannerResult {

        private final AtomicInteger existedCount = new AtomicInteger(0);
        private final AtomicInteger newCount = new AtomicInteger(0);

        public DeviceScannerResult(int existedCount, int newCount) {
            this.existedCount.set(existedCount);
            this.newCount.set(newCount);
        }
    }
}
