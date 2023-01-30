package org.touchhome.bundle.api.console;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.ActionResponseModel;
import org.touchhome.bundle.api.model.FileContentType;
import org.touchhome.bundle.api.model.FileModel;
import org.touchhome.bundle.api.setting.console.header.ConsoleHeaderSettingPlugin;

import static org.touchhome.common.util.CommonUtils.OBJECT_MAPPER;

public interface ConsolePluginEditor extends ConsolePlugin<FileModel> {

    @Override
    default RenderType getRenderType() {
        return RenderType.editor;
    }

    ActionResponseModel save(FileModel content);

    default void sendValueToConsoleEditor(EntityContext entityContext) {
        entityContext.ui().sendNotification("-editor-" + getEntityID(), OBJECT_MAPPER.valueToTree(getValue()));
    }

    default MonacoGlyphAction getGlyphAction() {
        return null;
    }

    default ActionResponseModel glyphClicked(String line) {
        return null;
    }

    default boolean hasRefreshIntervalSetting() {
        return false;
    }

    FileContentType getContentType();

    /**
     * Uses for uploading files. If null - no upload button visible
     */
    String accept();

    @Override
    default JSONObject getOptions() {
        return new JSONObject().put("contentType", getContentType()).put("accept", accept()).putOpt("glyph", getGlyphAction());
    }

    default Class<? extends ConsoleHeaderSettingPlugin<?>> getFileNameHeaderAction() {
        return null;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    class MonacoGlyphAction {
        private String icon;
        private String color;
        private String pattern;
    }
}
