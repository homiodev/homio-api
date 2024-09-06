package org.homio.api.entity.zigbee;

import lombok.extern.log4j.Log4j2;
import org.homio.api.entity.HasPlace;
import org.homio.api.entity.device.DeviceBaseEntity;
import org.homio.api.entity.device.DeviceEndpointsBehaviourContract;
import org.homio.api.entity.version.HasFirmwareVersion;
import org.homio.api.model.WebAddress;
import org.homio.api.ui.UISidebarMenu;
import org.homio.api.ui.UISidebarMenu.TopSidebarMenu;
import org.homio.api.ui.field.UIFieldLinkToEntity.FieldLinkToEntityTitleProvider;
import org.jetbrains.annotations.Nullable;

@Log4j2
@UISidebarMenu
        (icon = "fas fa-bezier-curve", parent = TopSidebarMenu.DEVICES,
                bg = "#de9ed7", order = 5, overridePath = "zigbee",
                filter = {"*:fas fa-filter:#8DBA73", "status:fas fa-heart-crack:#C452C4"},
                sort = {
                        "name~#FF9800:fas fa-arrow-up-a-z:fas fa-arrow-down-z-a",
                        "description~#FF9800:fas fa-circle-arrow-up:fas fa-circle-arrow-down",
                        "model~#28A4AD:fas fa-arrow-up-short-wide:fas fa-arrow-down-wide-short",
                        "manufacturer~#28A4AD:fas fa-arrows-down-to-line:fas fa-arrows-down-to-line fa-rotate-180",
                        "updated~#7EAD28:fas fa-clock-rotate-left:fas fa-clock-rotate-left fa-flip-horizontal",
                        "status~#7EAD28:fas fa-turn-up:fas fa-turn-down",
                        "place~#9C27B0:fas fa-location-dot:fas fa-location-dot fa-rotate-180"
                })
public abstract class ZigBeeDeviceBaseEntity extends DeviceBaseEntity
        implements
        HasPlace,
        HasFirmwareVersion,
        DeviceEndpointsBehaviourContract,
        FieldLinkToEntityTitleProvider {

    @Override
    public String getLinkTitle() {
        String ieeeAddress = getIeeeAddress();
        if (ieeeAddress == null) {
            return getTitle();
        }
        return ieeeAddress.toUpperCase().substring(2); // cut 0X;
    }

    public abstract @Nullable WebAddress getManufacturer();
}
