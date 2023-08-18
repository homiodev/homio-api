package org.homio.api.entity.types;

import jakarta.persistence.Entity;
import org.homio.api.entity.device.DeviceBaseEntity;
import org.homio.api.ui.UISidebarMenu;
import org.homio.api.ui.field.action.HasDynamicContextMenuActions;

@Entity
@UISidebarMenu(icon = "fas fa-video", order = 1, parent = UISidebarMenu.TopSidebarMenu.MEDIA,
        bg = "#5950A7", allowCreateNewItems = true, overridePath = "media")
public abstract class MediaEntity extends DeviceBaseEntity
        implements HasDynamicContextMenuActions {

}
