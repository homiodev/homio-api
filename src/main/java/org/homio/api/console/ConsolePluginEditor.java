package org.homio.api.console;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.EntityContext;
import org.homio.api.model.ActionResponseModel;
import org.homio.api.model.FileContentType;
import org.homio.api.model.FileModel;
import org.homio.api.setting.console.header.ConsoleHeaderSettingPlugin;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import static org.homio.api.util.JsonUtils.OBJECT_MAPPER;

public interface ConsolePluginEditor extends ConsolePlugin<FileModel> {

    @Override
    default @NotNull RenderType getRenderType() {
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
     * @return Uses for uploading files. If null - no upload button visible
     */
    String accept();

    @Override
    default JSONObject getOptions() {
        return new JSONObject().put("contentType", getContentType()).put("accept", accept()).putOpt("glyph", getGlyphAction());
    }

    default Class<? extends ConsoleHeaderSettingPlugin<?>> getFileNameHeaderAction() {
        return null;
    }

    @Override
    default ActionResponseModel executeAction(@NotNull String entityID, @NotNull JSONObject metadata) {
        if (metadata.has("glyph")) {
            return this.glyphClicked(metadata.getString("glyph"));
        }
        if (StringUtils.isNotEmpty(entityID) && metadata.has("content")) {
            return save(new FileModel(entityID, metadata.getString("content"), null, false));
        }
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
