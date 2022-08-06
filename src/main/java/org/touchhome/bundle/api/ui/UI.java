package org.touchhome.bundle.api.ui;

public final class UI {

    public static final class Color {
        private static final String[] RANDOM_COLORS = new String[]{"#57A4D1", "#D18456",
                "#D1C155", "#A2D154", "#36461C", "#D054A1", "#D05362", "#AE7F84",
                "#7F83AE", "#7FAEAA"};
        public static final String PRIMARY_COLOR = "#E65100";
        public static final String RED = "#BD3500";
        public static final String GREEN = "#17A328";
        public static final String WHITE = "#999999";

        public static String random() {
            return RANDOM_COLORS[(int) (System.currentTimeMillis() % 10)];
        }
    }
}
