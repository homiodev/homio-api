package org.homio.bundle.api.entity.zigbee;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.homio.bundle.api.entity.DeviceBaseEntity;
import org.homio.bundle.api.ui.UISidebarMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Log4j2
@UISidebarMenu(icon = "fas fa-bezier-curve", parent = UISidebarMenu.TopSidebarMenu.HARDWARE,
               bg = "#de9ed7", order = 5, overridePath = "zigbee",
               sort = {
                   "name:fas fa-arrow-up-a-z:fas fa-arrow-down-z-a",
                   "description:fas fa-circle-arrow-up:fas fa-circle-arrow-down",
                   "model:fas fa-arrow-up-short-wide:fas fa-arrow-down-wide-short",
                   "manufacturer:fas fa-arrows-down-to-line:fas fa-arrows-down-to-line fa-rotate-180",
                   "update:fas fa-arrow-up-1-9:fas fa-arrow-down-9-1",
                   "status:fas fa-turn-up:fas fa-turn-down"
               })
public abstract class ZigBeeDeviceBaseEntity<T extends ZigBeeDeviceBaseEntity> extends DeviceBaseEntity<T> {

    public abstract @Nullable String getIcon();

    public abstract @Nullable String getIconColor();

    public abstract @Nullable String getDescription();

    @JsonIgnore
    public abstract @NotNull String getDeviceFullName();

    @JsonIgnore
    public abstract @NotNull Map<String, ZigBeeProperty> getProperties();

    public @Nullable ZigBeeProperty getProperty(@NotNull String property) {
        return getProperties().get(property);
    }
}
