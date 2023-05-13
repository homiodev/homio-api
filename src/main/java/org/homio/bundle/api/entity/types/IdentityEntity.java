package org.homio.bundle.api.entity.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import org.homio.bundle.api.entity.DeviceBaseEntity;
import org.homio.bundle.api.exception.ProhibitedExecution;
import org.homio.bundle.api.ui.UISidebarMenu;
import org.homio.bundle.api.ui.UISidebarMenu.TopSidebarMenu;
import org.homio.bundle.api.ui.field.UIFieldIgnore;

/**
 * Common class for entities which respond for users/ssh/tls/etc...
 */
@Entity
@UISidebarMenu(icon = "fas fa-address-card", parent = TopSidebarMenu.ITEMS, bg = "#9BA127",
               overridePath = "identity", allowCreateNewItems = true)
public abstract class IdentityEntity<T extends IdentityEntity> extends DeviceBaseEntity<T> {

    @Override
    @JsonIgnore
    @UIFieldIgnore
    public String getPlace() {
        throw new ProhibitedExecution();
    }
}
