package org.homio.api.service.scan;

import org.homio.api.EntityContext;
import org.homio.hquery.ProgressBar;

public interface ItemDiscoverySupport {

    String getName();

    BaseItemsDiscovery.DeviceScannerResult scan(EntityContext entityContext, ProgressBar progressBar,
        String headerConfirmButtonKey);
}
