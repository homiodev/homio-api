package org.homio.api.setting.console.header.dynamic;

import org.homio.api.setting.console.header.ConsoleHeaderSettingPlugin;
import org.homio.api.ui.field.UIFieldType;
import org.json.JSONObject;

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
