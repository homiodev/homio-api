package org.touchhome.bundle.api.service.scan;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.ProgressBar;

public interface ItemDiscoverySupport {
    String getName();

    BaseItemsDiscovery.DeviceScannerResult scan(EntityContext entityContext, ProgressBar progressBar, String headerConfirmButtonKey);
}
