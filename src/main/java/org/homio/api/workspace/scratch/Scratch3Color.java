package org.homio.api.workspace.scratch;

import lombok.Getter;
import org.homio.api.ui.UI;

@Getter
public class Scratch3Color {

    private final String color1;
    private final String color2;
    private final String color3;

    public Scratch3Color(String color) {
        this.color1 = color;
        this.color2 = UI.Color.darker(color, 0.7f);
        this.color3 = UI.Color.darker(color, 0.9f);
    }
}
