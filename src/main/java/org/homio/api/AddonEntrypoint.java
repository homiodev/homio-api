package org.homio.api;

import java.net.URL;
import lombok.SneakyThrows;
import org.homio.api.util.CommonUtils;
import org.jetbrains.annotations.NotNull;

public interface AddonEntrypoint extends Comparable<AddonEntrypoint> {

    String ADDON_PREFIX = "org.homio.addon.";

    static String getAddonName(Class clazz) {
        String name = clazz.getName();
        if (name.startsWith(ADDON_PREFIX)) {
            return name.substring(ADDON_PREFIX.length(), name.indexOf('.', ADDON_PREFIX.length()));
        }
        return null;
    }

    // run once app started
    void init();

    // run when app started and every time when added/removed new addons
    default void onContextRefresh() {

    }

    default void destroy() {

    }

    default String getSettingDescription() {
        return null;
    }

    @SneakyThrows
    default URL getAddonImageURL() {
        return getResource("images/image.png");
    }

    // a-z or at most one '-' and nothing else
    default String getAddonId() {
        return AddonEntrypoint.getAddonName(getClass());
    }

    int order();

    default AddonImageColorIndex getAddonImageColorIndex() {
        return AddonImageColorIndex.ZERO;
    }

    @Override
    default int compareTo(@NotNull AddonEntrypoint o) {
        return Integer.compare(this.order(), o.order());
    }

    @SneakyThrows
    default URL getResource(String resource) {
        return CommonUtils.getResource(getAddonId(), resource);
    }

    enum AddonImageColorIndex {
        ZERO, ONE, TWO, THREE, FOUR
    }
}
