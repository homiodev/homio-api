package org.homio.api.ui;

import org.jetbrains.annotations.Nullable;

public final class UI {

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
            if (color == null) {return null;}
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
