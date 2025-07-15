package org.homio.api.ui.dialog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.pivovarit.function.ThrowingConsumer;
import java.util.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.homio.api.ContextUI.DialogRequestHandler;
import org.homio.api.model.Icon;
import org.homio.api.ui.field.action.ActionInputParameter;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DialogModel {

    private final String entityID;
    private final String title;
    @JsonIgnore
    private final DialogRequestHandler actionHandler;
    private final List<DialogGroup> groups = new ArrayList<>();
    private final List<DialogButton> buttons = new ArrayList<>();
    @JsonIgnore
    private final Date creationTime = new Date();
    private int maxTimeoutInSec = 0;
    private String headerButtonAttachTo;
    private Icon icon;
    private String dialogColor;
    private boolean keepOnUi = true;
    private Boolean sendCancelOnLeaveDialog;
    private Boolean disableCloseDialogOutsideArea;
    @JsonIgnore
    private Object entityInstance;

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

    public DialogModel generateFieldsFromInstance(Object entityInstance) {
        this.entityInstance = entityInstance;
        return this;
    }

    public String getActionReferenceV2() {
        return entityInstance == null ? null : entityInstance.getClass().getSimpleName();
    }

    /**
     * Do not keep dialog on ui on refresh page and attach to special header button
     */
    public DialogModel disableKeepOnUi() {
        this.keepOnUi = false;
        return this;
    }

    /**
     * Send cancel action to server if user close dialog on press x or click outside dialog
     */
    public DialogModel sendCancelOnLeaveDialog() {
        this.sendCancelOnLeaveDialog = true;
        return this;
    }

    /**
     * Disable close dialog if click outside dialog area
     */
    public DialogModel disableCloseDialogOutsideArea() {
        this.disableCloseDialogOutsideArea = true;
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

    public DialogModel maxTimeoutInSec(int maxTimeoutInSec) {
        this.maxTimeoutInSec = maxTimeoutInSec > 0 && maxTimeoutInSec < 3600 ? maxTimeoutInSec : 0;
        return this;
    }

    public DialogModel headerButtonAttachTo(String headerButtonAttachTo) {
        this.headerButtonAttachTo = headerButtonAttachTo;
        return this;
    }

    public DialogModel appearance(@Nullable Icon icon, @Nullable String dialogColor) {
        this.icon = icon;
        this.dialogColor = dialogColor;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DialogModel that = (DialogModel) o;
        return entityID.equals(that.entityID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityID);
    }

    @SneakyThrows
    private DialogModel button(String entityID, String title, DialogButton.ButtonType buttonType,
                               ThrowingConsumer<DialogButton, Exception> consumer) {
        DialogButton dialogButton = new DialogButton(entityID, title, buttonType);
        consumer.accept(dialogButton);
        buttons.add(dialogButton);
        return this;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @RequiredArgsConstructor
    public static class DialogButton {

        private final String entityID;
        private final String title;
        private final ButtonType type;

        private Icon icon;

        public void setIcon(String icon) {
            this.icon = new Icon(icon);
        }

        private enum ButtonType {
            submit, cancel, extra
        }
    }
}
