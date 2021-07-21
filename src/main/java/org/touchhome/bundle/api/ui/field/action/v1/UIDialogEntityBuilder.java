package org.touchhome.bundle.api.ui.field.action.v1;

import org.touchhome.bundle.api.ui.field.action.v1.item.*;

public interface UIDialogEntityBuilder {

    TextInputBuilder<UIDialogEntityBuilder> addTextInput(String name, String defaultValue);

    CheckboxBuilder<UIDialogEntityBuilder> addCheckbox(String name, String defaultValue);

    InfoBuilder<UIDialogEntityBuilder> addInfo(String name);

    IpBuilder<UIDialogEntityBuilder> addIp(String name, String defaultIpAddress);

    TextAreaBuilder<UIDialogEntityBuilder> addTextArea(String name, String value);
}
