package org.homio.api.entity.types;

import jakarta.persistence.Entity;
import org.homio.api.entity.device.DeviceBaseEntity;
import org.homio.api.ui.UISidebarMenu;

@Entity
@UISidebarMenu(order = 200, icon = "fas fa-database", bg = "#8B2399", allowCreateNewItems = true, overridePath = "storage")
public abstract class StorageEntity extends DeviceBaseEntity {

}
