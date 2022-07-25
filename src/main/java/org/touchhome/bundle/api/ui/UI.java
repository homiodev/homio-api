package org.touchhome.bundle.api.ui;

public final class UI {

    public static final class Color {
        private static final String[] RANDOM_COLORS = new String[]{"#57a4d1", "#d18456",
                "#d1c155", "#a2d154", "#36461c", "#d054a1", "#d05362", "#ae7f84",
                "#7f83ae", "#7faeaa"};
        public static final String PRIMARY_COLOR = "#E65100";
        public static final String RED = "#BD3500";
        public static final String GREEN = "#17A328";

        public static String random() {
            return RANDOM_COLORS[(int) (System.currentTimeMillis() % 10)];
        }
    }
}
