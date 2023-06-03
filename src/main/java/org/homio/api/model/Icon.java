package org.homio.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
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
}
