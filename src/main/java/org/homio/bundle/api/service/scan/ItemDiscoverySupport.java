package org.homio.bundle.api.service.scan;

import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.ui.field.ProgressBar;

public interface ItemDiscoverySupport {
    String getName();

    BaseItemsDiscovery.DeviceScannerResult scan(EntityContext entityContext, ProgressBar progressBar,
                                                String headerConfirmButtonKey);
}
