package org.homio.api.model;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@RequiredArgsConstructor
@AllArgsConstructor
public class Icon {

    private @Nullable String icon;
    private @Nullable String color;

    public Icon(@Nullable String icon) {
        this.icon = icon;
    }

    public static @Nullable String defaultOrIcon(@Nullable String defaultIcon, @Nullable Icon icon) {
        return StringUtils.defaultIfEmpty(defaultIcon, Optional.ofNullable(icon).map(c -> icon.icon).orElse(null));
    }

    public static @Nullable String iconOrDefault(@Nullable Icon icon, @Nullable String defaultIcon) {
        return StringUtils.defaultIfEmpty(Optional.ofNullable(icon).map(c -> icon.icon).orElse(null), defaultIcon);
    }

    public static @Nullable String colorOrDefault(@Nullable Icon icon, @Nullable String defaultColor) {
        return StringUtils.defaultIfEmpty(Optional.ofNullable(icon).map(c -> icon.color).orElse(null), defaultColor);
    }
}
