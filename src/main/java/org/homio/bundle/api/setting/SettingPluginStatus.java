package org.homio.bundle.api.setting;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.model.Status;
import org.homio.bundle.api.ui.field.UIFieldType;
import org.homio.bundle.api.ui.field.action.v1.UIInputBuilder;
import org.homio.bundle.api.util.NotificationLevel;
import org.homio.bundle.api.util.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SettingPluginStatus extends SettingPlugin<SettingPluginStatus.BundleStatusInfo> {

    BundleStatusInfo ONLINE = new BundleStatusInfo(Status.ONLINE, null);
    BundleStatusInfo OFFLINE = new BundleStatusInfo(Status.OFFLINE, null);
    BundleStatusInfo UNKNOWN = new BundleStatusInfo(Status.UNKNOWN, null);

    static BundleStatusInfo of(Status status, String message) {
        return new BundleStatusInfo(status, message);
    }

    static BundleStatusInfo error(String message) {
        return new BundleStatusInfo(Status.ERROR, message);
    }

    static BundleStatusInfo error(Throwable th) {
        return new BundleStatusInfo(Status.ERROR, CommonUtils.getErrorMessage(th));
    }

    @Override
    default UIFieldType getSettingType() {
        return UIFieldType.Text;
    }

    @Override
    default Class<SettingPluginStatus.BundleStatusInfo> getType() {
        return SettingPluginStatus.BundleStatusInfo.class;
    }

    @Override
    default BundleStatusInfo parseValue(EntityContext entityContext, String value) {
        String[] split = value.split("#~#", -1);
        try {
            return split.length == 0 ? null : new BundleStatusInfo(Status.valueOf(split[0]), split.length > 1 ? split[1] : null);
        } catch (Exception ex) {
            return null;
        }
    }

    default void setActions(UIInputBuilder actionSupplier) {

    }

    @Override
    default String writeValue(BundleStatusInfo value) {
        return value == null ? "" : value.status.name() + "#~#" + defaultIfEmpty(value.message, "");
    }

    @RequiredArgsConstructor
    @Accessors(chain = true)
    class BundleStatusInfo {
        @NotNull
        private final Status status;

        @Getter
        @Nullable
        private final String message;

        @Setter
        @Getter
        private Consumer<UIInputBuilder> actionHandler;

        public boolean isOnline() {
            return status == Status.ONLINE;
        }

        public Status getStatus() {
            return status;
        }

        @Override
        public String toString() {
            return status.name() + (isEmpty(message) ? "" : " - " + message);
        }

        public NotificationLevel getLevel() {
            switch (status) {
                case OFFLINE:
                case UNKNOWN:
                    return NotificationLevel.warning;
                case ONLINE:
                    return NotificationLevel.success;
                default:
                    return NotificationLevel.error;
            }
        }
    }
}