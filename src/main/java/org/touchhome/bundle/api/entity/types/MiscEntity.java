package org.touchhome.bundle.api.entity.types;

import org.touchhome.bundle.api.entity.DeviceBaseEntity;
import org.touchhome.bundle.api.entity.PlaceEntity;
import org.touchhome.bundle.api.ui.UISidebarMenu;
import org.touchhome.bundle.api.ui.field.UIFieldIgnore;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@UISidebarMenu(icon = "fas fa-puzzle-piece", order = 100, bg = "#939E18", allowCreateNewItems = true, overridePath = "misc")
public abstract class MiscEntity<T extends MiscEntity> extends DeviceBaseEntity<T> {
    @Override
    @UIFieldIgnore
    public PlaceEntity getOwnerPlace() {
        return super.getOwnerPlace();
    }
}
