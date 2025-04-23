package org.homio.api.model;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

import java.util.Objects;
import java.util.Optional;
import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Icon {

    private @Nullable String icon;
    private @Nullable String color;

    public Icon(@Nullable String icon) {
        this.icon = icon;
    }

    public static @Nullable String defaultOrIcon(@Nullable String defaultIcon, @Nullable Icon icon) {
        return defaultIfEmpty(defaultIcon, Optional.ofNullable(icon).map(c -> icon.icon).orElse(null));
    }

    public static @Nullable String iconOrDefault(@Nullable Icon icon, @Nullable String defaultIcon) {
        return defaultIfEmpty(Optional.ofNullable(icon).map(c -> icon.icon).orElse(null), defaultIcon);
    }

    public static @Nullable String colorOrDefault(@Nullable Icon icon, @Nullable String defaultColor) {
        return defaultIfEmpty(Optional.ofNullable(icon).map(c -> icon.color).orElse(null), defaultColor);
    }

    public Icon merge(@Nullable Icon iconToMerge) {
        if (iconToMerge != null) {
            icon = Objects.toString(iconToMerge.icon, icon);
            color = Objects.toString(iconToMerge.color, color);
        }
        return this;
    }
}
