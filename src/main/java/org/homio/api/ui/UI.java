package org.homio.api.ui;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public final class UI {

  public static final class Image {

    @Getter
    public static class Snapshot {

      protected @NotNull ReentrantLock lockCurrentSnapshot = new ReentrantLock();
      protected byte[] latestSnapshot = new byte[0];
      private @Setter
      @NotNull Instant lastSnapshotRequest = Instant.now();
      private @NotNull Instant currentSnapshotTime = Instant.now();
      private @Setter int cacheSec;

      public Snapshot(int cacheSec) {
        this.cacheSec = cacheSec;
      }

      public boolean setSnapshot(byte[] data) {
        boolean equals = Arrays.equals(latestSnapshot, data);
        latestSnapshot = data;
        currentSnapshotTime = Instant.now();
        return equals;
      }

      public byte[] getSnapshot(Supplier<byte[]> fetchHandler) {
        long lastUpdatedMs = Duration.between(lastSnapshotRequest, Instant.now()).toMillis();
        if (lastUpdatedMs >= Duration.ofSeconds(cacheSec).toMillis()) {
          try {
            lastSnapshotRequest = Instant.now();
            byte[] data = fetchHandler.get();
            if (data != null) {
              setSnapshot(data);
            }
          } catch (Exception ex) {
            // Single gray pixel JPG to keep streams open when the camera goes offline, so they don't stop.
            return new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff, (byte) 0xe0, 0x00, 0x10, 0x4a, 0x46, 0x49, 0x46,
              0x00, 0x01, 0x01, 0x01, 0x00, 0x48, 0x00, 0x48, 0x00, 0x00, (byte) 0xff, (byte) 0xdb, 0x00, 0x43,
              0x00, 0x03, 0x02, 0x02, 0x02, 0x02, 0x02, 0x03, 0x02, 0x02, 0x02, 0x03, 0x03, 0x03, 0x03, 0x04,
              0x06, 0x04, 0x04, 0x04, 0x04, 0x04, 0x08, 0x06, 0x06, 0x05, 0x06, 0x09, 0x08, 0x0a, 0x0a, 0x09,
              0x08, 0x09, 0x09, 0x0a, 0x0c, 0x0f, 0x0c, 0x0a, 0x0b, 0x0e, 0x0b, 0x09, 0x09, 0x0d, 0x11, 0x0d,
              0x0e, 0x0f, 0x10, 0x10, 0x11, 0x10, 0x0a, 0x0c, 0x12, 0x13, 0x12, 0x10, 0x13, 0x0f, 0x10, 0x10,
              0x10, (byte) 0xff, (byte) 0xc9, 0x00, 0x0b, 0x08, 0x00, 0x01, 0x00, 0x01, 0x01, 0x01, 0x11, 0x00,
              (byte) 0xff, (byte) 0xcc, 0x00, 0x06, 0x00, 0x10, 0x10, 0x05, (byte) 0xff, (byte) 0xda, 0x00, 0x08,
              0x01, 0x01, 0x00, 0x00, 0x3f, 0x00, (byte) 0xd2, (byte) 0xcf, 0x20, (byte) 0xff, (byte) 0xd9};
          }
        }
        return latestSnapshot;
      }
    }
  }

  public static final class Color {

    public static final String ERROR_DIALOG = "#672E18";

    public static final String BLUE = "#2A97C9";
    public static final String WARNING = "#BBA814";
    public static final String PRIMARY_COLOR = "#E65100";
    public static final String RED = "#BD3500";
    public static final String GREEN = "#17A328";
    public static final String WHITE = "#999999";
    private static final String[] RANDOM_COLORS = new String[]{"#49738C", "#D18456",
      "#7f7635", "#D054A1", "#D05362", "#AE7F84",
      "#7F83AE", "#577674", "#009688", "#50216A", "#6A2121", "#215A6A", "#999999"};

    public static String random() {
      return RANDOM_COLORS[(int) (System.currentTimeMillis() % 10)];
    }

    public static String darker(@Nullable String color, float factor) {
      if (color == null) {
        return null;
      }
      java.awt.Color dc = java.awt.Color.decode(color);
      int red = (int) (dc.getRed() * factor);
      int green = (int) (dc.getGreen() * factor);
      int blue = (int) (dc.getBlue() * factor);

      red = Math.min(255, Math.max(0, red));
      green = Math.min(255, Math.max(0, green));
      blue = Math.min(255, Math.max(0, blue));
      return String.format("#%02x%02x%02x", red, green, blue);
    }
  }
}
