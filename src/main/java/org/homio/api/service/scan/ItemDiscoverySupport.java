package org.homio.api.service.scan;

import org.homio.api.EntityContext;
import org.homio.api.ui.field.ProgressBar;

public interface ItemDiscoverySupport {
    String getName();

    BaseItemsDiscovery.DeviceScannerResult scan(EntityContext entityContext, ProgressBar progressBar,
                                                String headerConfirmButtonKey);
}
