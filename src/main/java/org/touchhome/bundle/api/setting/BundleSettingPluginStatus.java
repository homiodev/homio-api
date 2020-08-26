package org.touchhome.bundle.api.setting;

import lombok.AllArgsConstructor;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.json.NotificationEntityJSON;
import org.touchhome.bundle.api.model.BundleStatus;
import org.touchhome.bundle.api.util.NotificationType;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public interface BundleSettingPluginStatus extends BundleSettingPlugin<BundleSettingPluginStatus.BundleStatusInfo> {

    BundleStatusInfo ONLINE = new BundleStatusInfo(BundleStatus.ONLINE, null);
    BundleStatusInfo UNKNOWN = new BundleStatusInfo(BundleStatus.UNKNOWN, null);

    @Override
    default SettingType getSettingType() {
        return SettingType.Info;
    }

    @Override
    default String getDefaultValue() {
        return BundleStatus.UNKNOWN.name();
    }

    @Override
    default BundleStatusInfo parseValue(EntityContext entityContext, String value) {
        String[] split = value.split("#~#", -1);
        return split.length == 0 ? UNKNOWN : new BundleStatusInfo(BundleStatus.valueOf(split[0]), split.length > 1 ? split[1] : null);
    }

    @Override
    default String writeValue(BundleStatusInfo value) {
        return value == null ? "" : value.bundleStatus.name() + "#~#" + defaultIfEmpty(value.message, "");
    }

    static BundleStatusInfo of(BundleStatus bundleStatus, String message) {
        return new BundleStatusInfo(bundleStatus, message);
    }

    static BundleStatusInfo error(String message) {
        return new BundleStatusInfo(BundleStatus.ERROR, message);
    }

    static BundleStatusInfo error(Throwable th) {
        return new BundleStatusInfo(BundleStatus.ERROR, TouchHomeUtils.getErrorMessage(th));
    }

    @AllArgsConstructor
    class BundleStatusInfo {
        private final BundleStatus bundleStatus;
        private final String message;

        public boolean isOnline() {
            return bundleStatus == BundleStatus.ONLINE;
        }

        public BundleStatus getStatus() {
            return bundleStatus;
        }

        public NotificationEntityJSON toNotification(String bundleId) {
            return new NotificationEntityJSON(bundleId + "-status").setNotificationType(getNotificationType())
                    .setName(bundleId).setDescription(defaultIfEmpty(message, bundleStatus.name()));
        }

        @Override
        public String toString() {
            return bundleStatus.name() + (isEmpty(message) ? "" : " - " + message);
        }

        private NotificationType getNotificationType() {
            switch (bundleStatus) {
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
