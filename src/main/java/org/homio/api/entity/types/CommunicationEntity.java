package org.homio.api.entity.types;

import jakarta.persistence.Entity;
import org.homio.api.entity.device.DeviceBaseEntity;
import org.homio.api.ui.UISidebarMenu;

/**
 * Common class for entities which respond for communications. i.e. telegram
 */
@Entity
@UISidebarMenu(order = 200, icon = "fab fa-facebook-messenger", bg = "#A16427", allowCreateNewItems = true, overridePath = "comm")
public abstract class CommunicationEntity extends DeviceBaseEntity {

}
