package org.homio.api.entity;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HasEntityLog extends HasJsonData {

    default boolean isDebug() {
        return getJsonData("log_debug", true);
    }

    default void setDebug(boolean value) {
        setJsonData("log_debug", value);
    }

    void logBuilder(@NotNull EntityLogBuilder entityLogBuilder);

    /**
     * Some entity/service would provide extra log files.
     *
     * @return path to file or null
     */
    default @Nullable Path getExtraLogFile() {
        return null;
    }

    interface EntityLogBuilder {

        default void addTopic(@NotNull Class<?> topicClass) {
            addTopic(topicClass, null);
        }

        void addTopic(@NotNull Class<?> topicClass, String filterByField);

        default void addTopic(@NotNull String topic) {
            addTopic(topic, null);
        }

        void addTopic(@NotNull String topic, @Nullable String filterByField);

        default void addTopicFilterByEntityID(@NotNull String topic) {
            addTopic(topic, "entityID");
        }
    }
}

