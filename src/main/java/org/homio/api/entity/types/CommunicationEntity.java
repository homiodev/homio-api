package org.homio.api.entity.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import org.homio.api.entity.device.DeviceBaseEntity;
import org.homio.api.exception.ProhibitedExecution;
import org.homio.api.ui.UISidebarMenu;
import org.homio.api.ui.field.UIFieldIgnore;

/**
 * Common class for entities which respond for communications. i.e. telegram
 */
@Entity
@UISidebarMenu(icon = "fab fa-facebook-messenger", order = 200, bg = "#A16427", allowCreateNewItems = true, overridePath = "comm")
public abstract class CommunicationEntity extends DeviceBaseEntity {

    @Override
    @JsonIgnore
    @UIFieldIgnore
    public String getPlace() {
        throw new ProhibitedExecution();
    }
}
