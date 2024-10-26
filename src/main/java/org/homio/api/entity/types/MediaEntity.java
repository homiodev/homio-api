package org.homio.api.entity.types;

import jakarta.persistence.Entity;
import org.homio.api.entity.device.DeviceBaseEntity;
import org.homio.api.ui.UISidebarMenu;
import org.homio.api.ui.field.action.HasDynamicContextMenuActions;

@Entity
@UISidebarMenu(order = 100,
        icon = "fas fa-video",
        parent = UISidebarMenu.TopSidebarMenu.MEDIA,
        bg = "#5950A7",
        allowCreateNewItems = true,
        overridePath = "media",
        filter = {"*:fas fa-filter:#8DBA73", "status:fas fa-heart-crack:#C452C4"},
        sort = {
                "name~#FF9800:fas fa-arrow-up-a-z:fas fa-arrow-down-z-a",
                "model~#28A4AD:fas fa-arrow-up-short-wide:fas fa-arrow-down-wide-short",
                "manufacturer~#28A4AD:fas fa-arrows-down-to-line:fas fa-arrows-down-to-line fa-rotate-180",
                "updated~#7EAD28:fas fa-clock-rotate-left:fas fa-clock-rotate-left fa-flip-horizontal",
                "status~#7EAD28:fas fa-turn-up:fas fa-turn-down",
                "place~#9C27B0:fas fa-location-dot:fas fa-location-dot fa-rotate-180"
        })
public abstract class MediaEntity extends DeviceBaseEntity
        implements HasDynamicContextMenuActions {

}
