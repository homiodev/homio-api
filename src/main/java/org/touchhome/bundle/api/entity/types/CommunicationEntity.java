package org.touchhome.bundle.api.entity.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.touchhome.bundle.api.entity.DeviceBaseEntity;
import org.touchhome.bundle.api.exception.ProhibitedExecution;
import org.touchhome.bundle.api.ui.UISidebarMenu;
import org.touchhome.bundle.api.ui.field.UIFieldIgnore;

import javax.persistence.Entity;

/**
 * Common class for entities which respond for communications. i.e. telegram
 */
@Entity
@UISidebarMenu(icon = "fab fa-facebook-messenger", order = 200, bg = "#A16427", allowCreateNewItems = true, overridePath = "comm")
public abstract class CommunicationEntity<T extends CommunicationEntity> extends DeviceBaseEntity<T> {

    @Override
    @JsonIgnore
    @UIFieldIgnore
    public String getPlace() {
        throw new ProhibitedExecution();
    }
}
