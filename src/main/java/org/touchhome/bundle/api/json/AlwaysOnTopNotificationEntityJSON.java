package org.touchhome.bundle.api.json;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.touchhome.bundle.api.util.NotificationType;

@Getter
@Accessors(chain = true)
public class AlwaysOnTopNotificationEntityJSON extends NotificationEntityJSON {

    private final Boolean alwaysOnTop = true;

    private String color;

    private Integer duration;

    private String icon;

    @Setter
    private String stopAction;

    @Setter
    private Boolean remove = false;

    public AlwaysOnTopNotificationEntityJSON(NotificationEntityJSON json, String color, Integer duration, String icon) {
        this(json);
        this.color = color;
        this.duration = duration;
        this.icon = icon;
    }

    public AlwaysOnTopNotificationEntityJSON(NotificationEntityJSON json) {
        super(json.getEntityID());
        setName(json.getName());
        setDescription(json.getDescription());
        setNotificationType(json.getNotificationType());
    }

    @Override
    public AlwaysOnTopNotificationEntityJSON setName(String name) {
        return (AlwaysOnTopNotificationEntityJSON) super.setName(name);
    }

    @Override
    public AlwaysOnTopNotificationEntityJSON setDescription(String description) {
        return (AlwaysOnTopNotificationEntityJSON) super.setDescription(description);
    }

    @Override
    public AlwaysOnTopNotificationEntityJSON setNotificationType(NotificationType notificationType) {
        return (AlwaysOnTopNotificationEntityJSON) super.setNotificationType(notificationType);
    }
}
