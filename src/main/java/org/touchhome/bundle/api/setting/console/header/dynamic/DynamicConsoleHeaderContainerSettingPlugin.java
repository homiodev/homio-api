package org.touchhome.bundle.api.setting.console.header.dynamic;

import org.json.JSONObject;
import org.touchhome.bundle.api.setting.console.header.ConsoleHeaderSettingPlugin;
import org.touchhome.bundle.api.ui.field.UIFieldType;

public interface DynamicConsoleHeaderContainerSettingPlugin extends ConsoleHeaderSettingPlugin<JSONObject> {

    @Override
    default Class<JSONObject> getType() {
        return JSONObject.class;
    }

    @Override
    default UIFieldType getSettingType() {
        return UIFieldType.SelectBoxDynamic;
    }
}
