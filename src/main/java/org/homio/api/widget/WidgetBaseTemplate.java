package org.homio.api.widget;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.homio.api.model.Icon;
import org.jetbrains.annotations.NotNull;

public interface WidgetBaseTemplate {

    default @NotNull String getName() {
        return getClass().getSimpleName();
    }

    @NotNull Icon getIcon();

    default String toJavaScript() {
        return null;
    }

    default @NotNull ParentWidget getParent() {
        return ParentWidget.Misc;
    }

    @Getter
    @RequiredArgsConstructor
    enum ParentWidget {
        Weather("fas fa-sun", "#BD9929"),
        Device("fas fa-microchip", "#3E74C4"),
        Media("fas fa-compact-disc", ""),
        Misc("fas fa-puzzle-piece fas", "#C45483");

        private final @NotNull String icon;
        private final @NotNull String color;
    }
}
