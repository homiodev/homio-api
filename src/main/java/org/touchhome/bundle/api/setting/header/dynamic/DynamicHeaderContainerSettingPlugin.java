package org.touchhome.bundle.api.setting.header.dynamic;

import org.json.JSONObject;
import org.touchhome.bundle.api.setting.header.HeaderSettingPlugin;

public interface DynamicHeaderContainerSettingPlugin extends HeaderSettingPlugin<JSONObject> {

    @Override
    default Class<JSONObject> getType() {
        return JSONObject.class;
    }

    @Override
    default SettingType getSettingType() {
        return SettingType.SelectBoxDynamic;
    }
}
