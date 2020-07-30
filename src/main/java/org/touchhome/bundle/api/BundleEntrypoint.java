package org.touchhome.bundle.api;

import lombok.SneakyThrows;
import org.touchhome.bundle.api.json.NotificationEntityJSON;

import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public interface BundleEntrypoint extends Comparable<BundleEntrypoint> {
    String BUNDLE_PREFIX = "org.touchhome.bundle.";

    static String getBundleName(Class clazz) {
        String name = clazz.getName();
        if (name.startsWith(BUNDLE_PREFIX)) {
            return name.substring(BUNDLE_PREFIX.length(), name.indexOf('.', BUNDLE_PREFIX.length()));
        }
        return null;
    }

    void init();

    default void destroy() {

    }

    default String getBundleImage() {
        return "image.png";
    }

    @SneakyThrows
    default URL getBundleImageURL() {
        return getResource(getBundleImage());
    }

    String getBundleId();

    int order();

    default BundleImageColorIndex getBundleImageColorIndex() {
        return BundleImageColorIndex.ZERO;
    }

    @Override
    default int compareTo(@NotNull BundleEntrypoint o) {
        return Integer.compare(this.order(), o.order());
    }

    default Set<NotificationEntityJSON> getNotifications() {
        return null;
    }

    @SneakyThrows
    default URL getResource(String resource) {
        URL imageUrl = null;
        ArrayList<URL> urls = Collections.list(getClass().getClassLoader().getResources(resource));
        if (urls.size() == 1) {
            imageUrl = urls.get(0);
        } else if (urls.size() > 1) {
            imageUrl = urls.stream().filter(url -> url.getFile().contains(getBundleId())).findAny().orElse(null);
        }
        return imageUrl;
    }

    enum BundleImageColorIndex {
        ZERO, ONE, TWO, THREE, FOUR
    }
}
