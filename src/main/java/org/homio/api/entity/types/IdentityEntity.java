package org.homio.api.entity.types;

import jakarta.persistence.Entity;
import org.homio.api.entity.device.DeviceBaseEntity;
import org.homio.api.ui.UISidebarMenu;
import org.homio.api.ui.UISidebarMenu.TopSidebarMenu;

/** Common class for entities which respond for users/ssh/tls/etc... */
@Entity
@UISidebarMenu(
    order = 300,
    icon = "fas fa-address-card",
    parent = TopSidebarMenu.ITEMS,
    bg = "#9BA127",
    overridePath = "identity",
    allowCreateNewItems = true)
public abstract class IdentityEntity extends DeviceBaseEntity {}
