package org.touchhome.bundle.api.console;

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.util.Collection;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

public interface ConsolePluginComplexLines extends ConsolePlugin<Collection<String>> {

    @Override
    default RenderType getRenderType() {
        return RenderType.string;
    }

    Collection<ComplexString> getComplexValue();

    @Override
    default Collection<String> getValue() {
        Collection<ComplexString> complexValue = getComplexValue();
        return complexValue == null
                ? null
                : complexValue.stream().map(ComplexString::toString).collect(Collectors.toList());
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    @RequiredArgsConstructor
    class ComplexString {
        private final String value;
        private Long date;
        private String color;
        private Boolean positionToRight;

        public static ComplexString of(String value, Long date) {
            return new ComplexString(value).setDate(date);
        }

        public static ComplexString of(
                String value, Long date, String color, Boolean positionToRight) {
            return new ComplexString(value)
                    .setDate(date)
                    .setColor(color)
                    .setPositionToRight(positionToRight);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(value);
            if (date != null || color != null || positionToRight != null) {
                sb.append("~~~")
                        .append(date == null ? "" : date)
                        .append("~~~")
                        .append(defaultString(color))
                        .append("~~~")
                        .append(positionToRight == null ? "" : positionToRight);
            }
            return sb.toString();
        }
    }
}
