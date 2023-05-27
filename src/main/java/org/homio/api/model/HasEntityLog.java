package org.homio.api.model;

import org.homio.api.entity.HasJsonData;

public interface HasEntityLog extends HasJsonData {
    default boolean isDebug() {
        return getJsonData("log_debug", true);
    }

    default void setDebug(boolean value) {
        setJsonData("log_debug", value);
    }

    void logBuilder(EntityLogBuilder entityLogBuilder);

    interface EntityLogBuilder {
        default void addTopic(Class<?> topicClass) {
            addTopic(topicClass, null);
        }

        void addTopic(Class<?> topicClass, String filterByField);

        default void addTopic(String topic) {
            addTopic(topic, null);
        }

        void addTopic(String topic, String filterByField);

        default void addTopicFilterByEntityID(String topic) {
            addTopic(topic, "entityID");
        }
    }
}

