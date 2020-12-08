package org.touchhome.bundle.api.console;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.touchhome.bundle.api.json.ActionResponse;

public interface ConsolePluginEditor extends ConsolePlugin<ConsolePluginEditor.EditorContent> {

    @Override
    default RenderType getRenderType() {
        return RenderType.editor;
    }

    ActionResponse save(EditorContent content);

    default boolean hasRefreshIntervalSetting() {
        return false;
    }

    ContentType getContentType();

    /**
     * Uses for uploading files. If null - no upload button visible
     */
    String accept();

    @Override
    default JSONObject getOptions() {
        return new JSONObject().put("contentType", getContentType()).put("accept", accept());
    }

    enum ContentType {
        json, javascript, typescript, cpp, yaml, css, dockerfile, sql, shell, html, markdown, plaintext
    }

    @Getter
    @RequiredArgsConstructor
    class EditorContent {
        private final String name;
        private final String content;
    }
}
