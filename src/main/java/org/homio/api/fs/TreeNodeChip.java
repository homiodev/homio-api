package org.homio.api.fs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.homio.api.model.Icon;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

/**
 * Represent [Chip] block element on right side of tree. Must specify at least icon or text to be visible on UI.
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiredArgsConstructor
public class TreeNodeChip {

    @Nullable
    private final Icon icon;
    @Nullable
    private final String text;
    @Nullable
    private String bgColor;

    private boolean clickable; // if Chip not only info but communicate with server
    private JSONObject metadata; // require if clickable and need handle Chip on server side
}
