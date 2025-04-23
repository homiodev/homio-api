package org.homio.api.ui.field;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.homio.api.util.Lang;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.DecimalFormat;
import java.util.Map;

/** Progress bar. Must return int or UIFieldProgress.Progress Max value is 100! */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldProgress {

  @Nullable
  String color() default "";

  @Nullable
  UIFieldProgressColorChange[] colorChange() default {};

  @Target({ElementType.FIELD})
  @Retention(RetentionPolicy.RUNTIME)
  @interface UIFieldProgressColorChange {

    @NotNull
    String color();

    int whenMoreThan();
  }

  @Getter
  @RequiredArgsConstructor
  class Progress {

    private static final DecimalFormat FORMAT = new DecimalFormat("#.##");

    private final int value;
    private final int max;
    private final @Nullable String message;
    private final boolean showMessage;

    public static Progress of(int value, int maxValue, @Nullable String message) {
      return Progress.of(value, maxValue, message, false);
    }

    public static Progress of(
        int value, int maxValue, @Nullable String message, boolean showMessage) {
      return new Progress((int) Math.ceil(value * 100f / maxValue), maxValue, message, showMessage);
    }

    public static Progress of(int value, int maxValue) {
      return Progress.of(value, maxValue, false);
    }

    public static Progress of(int value, int maxValue, boolean showMessage) {
      return Progress.of(
          value,
          maxValue,
          Lang.getServerMessage(
              "USED_QUOTA",
              Map.of(
                  "PC",
                  FORMAT.format(value / (double) maxValue * 100),
                  "VAL",
                  value,
                  "MAX",
                  maxValue)),
          showMessage);
    }
  }
}
