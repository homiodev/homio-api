package org.homio.bundle.api.entity.types;

import org.homio.bundle.api.entity.DeviceBaseEntity;
import org.homio.bundle.api.ui.UISidebarMenu;
import org.homio.bundle.api.ui.UISidebarMenu.TopSidebarMenu;

/**
 * Common class for entities which respond for users/ssh/tls/etc...
 */
@UISidebarMenu(icon = "fas fa-address-card", parent = TopSidebarMenu.ITEMS, bg = "#9BA127", overridePath = "identity")
public abstract class IdentityEntity<T extends IdentityEntity> extends DeviceBaseEntity<T> {

}
