package org.homio.api.entity.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import org.apache.commons.lang3.NotImplementedException;
import org.homio.api.entity.device.DeviceBaseEntity;
import org.homio.api.ui.UISidebarMenu;
import org.homio.api.ui.UISidebarMenu.TopSidebarMenu;
import org.homio.api.ui.field.UIFieldIgnore;

/**
 * Common class for entities which respond for users/ssh/tls/etc...
 */
@Entity
@UISidebarMenu(icon = "fas fa-address-card", parent = TopSidebarMenu.ITEMS, bg = "#9BA127",
               overridePath = "identity", allowCreateNewItems = true)
public abstract class IdentityEntity extends DeviceBaseEntity {

    @Override
    @JsonIgnore
    @UIFieldIgnore
    public String getPlace() {
        throw new NotImplementedException();
    }
}
