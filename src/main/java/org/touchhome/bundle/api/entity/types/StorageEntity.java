package org.touchhome.bundle.api.entity.types;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.touchhome.bundle.api.entity.DeviceBaseEntity;
import org.touchhome.bundle.api.entity.PlaceEntity;
import org.touchhome.bundle.api.ui.UISidebarMenu;
import org.touchhome.bundle.api.ui.field.UIFieldIgnore;

import javax.persistence.Entity;

@Getter
@Setter
@Entity
@Accessors(chain = true)
@UISidebarMenu(icon = "fas fa-database", order = 200, bg = "#8B2399", allowCreateNewItems = true, overridePath = "storage")
public abstract class StorageEntity<T extends StorageEntity> extends DeviceBaseEntity<T> {
    @Override
    @UIFieldIgnore
    public PlaceEntity getOwnerPlace() {
        return super.getOwnerPlace();
    }
}
