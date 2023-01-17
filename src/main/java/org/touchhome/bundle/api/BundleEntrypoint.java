package org.touchhome.bundle.api;

import java.net.URL;
import javax.validation.constraints.NotNull;
import lombok.SneakyThrows;
import org.touchhome.bundle.api.setting.SettingPluginStatus;
import org.touchhome.bundle.api.ui.builder.BellNotificationBuilder;
import org.touchhome.common.util.CommonUtils;

public interface BundleEntrypoint extends Comparable<BundleEntrypoint> {
    String BUNDLE_PREFIX = "org.touchhome.bundle.";

    static String getBundleName(Class clazz) {
        String name = clazz.getName();
        if (name.startsWith(BUNDLE_PREFIX) && !name.startsWith(BUNDLE_PREFIX + ".api.")) {
            return name.substring(
                    BUNDLE_PREFIX.length(), name.indexOf('.', BUNDLE_PREFIX.length()));
        }
        return null;
    }

    // run once app started
    void init();

    // run when app started and every time when added/removed new bundles
    default void onContextRefresh() {}

    default void destroy() {}

    default String getSettingDescription() {
        return null;
    }

    default String getBundleImage() {
        return "image.png";
    }

    @SneakyThrows
    default URL getBundleImageURL() {
        return getResource(getBundleImage());
    }

    // a-z or at most one '-' and nothing else
    default String getBundleId() {
        return BundleEntrypoint.getBundleName(getClass());
    }

    int order();

    default BundleImageColorIndex getBundleImageColorIndex() {
        return BundleImageColorIndex.ZERO;
    }

    @Override
    default int compareTo(@NotNull BundleEntrypoint o) {
        return Integer.compare(this.order(), o.order());
    }

    /** Notifications that visible in ui header */
    default void assembleBellNotifications(BellNotificationBuilder bellNotificationBuilder) {}

    /** Get main bundle status setting. Will be shown on header ui */
    default Class<? extends SettingPluginStatus> getBundleStatusSetting() {
        return null;
    }

    @SneakyThrows
    default URL getResource(String resource) {
        return CommonUtils.getResource(getBundleId(), resource);
    }

    enum BundleImageColorIndex {
        ZERO,
        ONE,
        TWO,
        THREE,
        FOUR
    }
}
