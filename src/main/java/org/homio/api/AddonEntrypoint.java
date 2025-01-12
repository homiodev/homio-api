package org.homio.api;

import lombok.SneakyThrows;
import org.homio.api.util.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;

public interface AddonEntrypoint extends Comparable<AddonEntrypoint> {

  String ADDON_PREFIX = "org.homio.addon.";

  static @Nullable String getAddonID(Class clazz) {
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

  default @Nullable String getSettingDescription() {
    return null;
  }

  @SneakyThrows
  default @NotNull URL getAddonImageURL() {
    return getResource("images/image.png");
  }

  // a-z or at most one '-' and nothing else
  default @Nullable String getAddonID() {
    return AddonEntrypoint.getAddonID(getClass());
  }

  default @NotNull AddonImageColorIndex getAddonImageColorIndex() {
    return AddonImageColorIndex.ZERO;
  }

  @Override
  default int compareTo(@NotNull AddonEntrypoint o) {
    return this.getAddonID().compareTo(o.getAddonID());
  }

  @SneakyThrows
  default @Nullable URL getResource(String resource) {
    return CommonUtils.getResource(getAddonID(), resource);
  }

  enum AddonImageColorIndex {
    ZERO, ONE, TWO, THREE, FOUR
  }
}
