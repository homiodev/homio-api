package org.touchhome.bundle.api.ui.dialog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pivovarit.function.ThrowingConsumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.touchhome.bundle.api.EntityContextUI;
import org.touchhome.bundle.api.ui.field.action.ActionInputParameter;

import java.util.*;

@Getter
@RequiredArgsConstructor
public class DialogModel {
    private final String entityID;
    private final String title;
    @JsonIgnore
    private final EntityContextUI.DialogRequestHandler actionHandler;
    private final List<DialogGroup> groups = new ArrayList<>();
    private final List<DialogButton> buttons = new ArrayList<>();
    @JsonIgnore
    private final Date creationTime = new Date();
    @JsonIgnore
    private int maxTimeoutInSec = 0;
    private String headerButtonAttachTo;
    private String icon;
    private String iconColor;
    private String dialogColor;

    @SneakyThrows
    public DialogModel group(String name, ThrowingConsumer<DialogGroup, Exception> consumer) {
        DialogGroup dialogGroup = new DialogGroup(name);
        groups.add(dialogGroup);
        consumer.accept(dialogGroup);
        return this;
    }

    public DialogModel group(String name, Collection<ActionInputParameter> inputs) {
        DialogGroup dialogGroup = new DialogGroup(name);
        groups.add(dialogGroup);
        dialogGroup.inputs(inputs);
        return this;
    }

    public DialogModel submitButton(String title, ThrowingConsumer<DialogButton, Exception> consumer) {
        return button(null, title, DialogButton.ButtonType.submit, consumer);
    }

    public DialogModel submitButton(String title) {
        return button(null, title, DialogButton.ButtonType.submit, dialogButton -> {
        });
    }

    public DialogModel cancelButton(String title, ThrowingConsumer<DialogButton, Exception> consumer) {
        return button(null, title, DialogButton.ButtonType.cancel, consumer);
    }

    public DialogModel cancelButton(String title) {
        return button(null, title, DialogButton.ButtonType.cancel, dialogButton -> {
        });
    }

    public DialogModel extraButton(String entityID, String title, ThrowingConsumer<DialogButton, Exception> consumer) {
        return button(entityID, title, DialogButton.ButtonType.extra, consumer);
    }

    public DialogModel extraButton(String entityID, String title) {
        return button(entityID, title, DialogButton.ButtonType.extra, dialogButton -> {
        });
    }

    @SneakyThrows
    private DialogModel button(String entityID, String title, DialogButton.ButtonType buttonType,
                               ThrowingConsumer<DialogButton, Exception> consumer) {
        DialogButton dialogButton = new DialogButton(entityID, title, buttonType);
        consumer.accept(dialogButton);
        buttons.add(dialogButton);
        return this;
    }

    public DialogModel maxTimeoutInSec(int maxTimeoutInSec) {
        this.maxTimeoutInSec = maxTimeoutInSec > 0 && maxTimeoutInSec < 3600 ? maxTimeoutInSec : 0;
        return this;
    }

    public DialogModel headerButtonAttachTo(String headerButtonAttachTo) {
        this.headerButtonAttachTo = headerButtonAttachTo;
        return this;
    }

    public DialogModel appearance(String icon, String iconColor, String dialogColor) {
        this.icon = icon;
        this.iconColor = iconColor;
        this.dialogColor = dialogColor;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DialogModel that = (DialogModel) o;
        return entityID.equals(that.entityID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityID);
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @RequiredArgsConstructor
    public static class DialogButton {
        private final String entityID;
        private final String title;
        private final ButtonType type;

        private String icon;

        private enum ButtonType {
            submit, cancel, extra
        }
    }
}
