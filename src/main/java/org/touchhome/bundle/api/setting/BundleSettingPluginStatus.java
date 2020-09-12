package org.touchhome.bundle.api.setting;

import lombok.AllArgsConstructor;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.json.NotificationEntityJSON;
import org.touchhome.bundle.api.model.Status;
import org.touchhome.bundle.api.util.NotificationType;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public interface BundleSettingPluginStatus extends BundleSettingPlugin<BundleSettingPluginStatus.BundleStatusInfo> {

    BundleStatusInfo ONLINE = new BundleStatusInfo(Status.ONLINE, null);
    BundleStatusInfo UNKNOWN = new BundleStatusInfo(Status.UNKNOWN, null);

    @Override
    default SettingType getSettingType() {
        return SettingType.Info;
    }

    @Override
    default String getDefaultValue() {
        return Status.UNKNOWN.name();
    }

    @Override
    default BundleStatusInfo parseValue(EntityContext entityContext, String value) {
        String[] split = value.split("#~#", -1);
        return split.length == 0 ? UNKNOWN : new BundleStatusInfo(Status.valueOf(split[0]), split.length > 1 ? split[1] : null);
    }

    @Override
    default String writeValue(BundleStatusInfo value) {
        return value == null ? "" : value.status.name() + "#~#" + defaultIfEmpty(value.message, "");
    }

    static BundleStatusInfo of(Status status, String message) {
        return new BundleStatusInfo(status, message);
    }

    static BundleStatusInfo error(String message) {
        return new BundleStatusInfo(Status.ERROR, message);
    }

    static BundleStatusInfo error(Throwable th) {
        return new BundleStatusInfo(Status.ERROR, TouchHomeUtils.getErrorMessage(th));
    }

    @AllArgsConstructor
    class BundleStatusInfo {
        private final Status status;
        private final String message;

        public boolean isOnline() {
            return status == Status.ONLINE;
        }

        public Status getStatus() {
            return status;
        }

        public NotificationEntityJSON toNotification(String bundleId) {
            return new NotificationEntityJSON(bundleId + "-status").setNotificationType(getNotificationType())
                    .setName(bundleId).setDescription(defaultIfEmpty(message, status.name()));
        }

        @Override
        public String toString() {
            return status.name() + (isEmpty(message) ? "" : " - " + message);
        }

        private NotificationType getNotificationType() {
            switch (status) {
                case OFFLINE:
                case UNKNOWN:
                    return NotificationType.warning;
                case ONLINE:
                    return NotificationType.success;
                default:
                    return NotificationType.danger;
            }
        }
    }
}
