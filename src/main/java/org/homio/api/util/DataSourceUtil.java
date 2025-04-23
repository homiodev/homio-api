package org.homio.api.util;

import static org.homio.api.entity.HasJsonData.LEVEL_DELIMITER;
import static org.homio.api.util.JsonUtils.OBJECT_MAPPER;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Path;
import java.util.Objects;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.Context;
import org.homio.api.entity.BaseEntity;
import org.jetbrains.annotations.NotNull;

public class DataSourceUtil {

    public static Path getPath(Path basePath, String value) {
        String[] items = value.split(LEVEL_DELIMITER);
        return basePath.resolve(Path.of("", items));
    }

    public static SelectionSource getSelection(String value) {
        String[] items = value.split("###");
        return new SelectionSource(items[0], items.length > 1 ? items[1] : null);
    }

    @Getter
    public static class SelectionSource {

        private static final JsonNode EMPTY_METADATA = OBJECT_MAPPER.createObjectNode();

        private final @NotNull String value;
        private @NotNull JsonNode metadata = EMPTY_METADATA;

        public SelectionSource(String value, String metadata) {
            this.value = Objects.toString(value, "");
            if (StringUtils.isNotEmpty(metadata)) {
                try {
                    this.metadata = OBJECT_MAPPER.readValue(metadata, JsonNode.class);
                } catch (Exception ignore) {
                }
            }
        }

        public String getValue(String defaultValue) {
            return StringUtils.defaultIfEmpty(value, defaultValue);
        }

        public String getEntityValue() {
            String[] items = value.split(LEVEL_DELIMITER);
            return items[items.length - 1];
        }

        public String getEntityID() {
            return this.value.split("-->")[0];
        }

        public <T extends BaseEntity> T getValue(Context context) {
            String[] items = value.split(LEVEL_DELIMITER);
            return context.db().get(items[items.length - 1]);
        }
    }
}
