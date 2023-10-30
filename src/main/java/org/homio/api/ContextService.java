package org.homio.api;

import static java.lang.String.format;
import static org.homio.api.entity.HasJsonData.LIST_DELIMITER;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.homio.api.entity.BaseEntity;
import org.homio.api.model.HasEntityIdentifier;
import org.homio.api.service.EntityService;
import org.homio.api.service.EntityService.ServiceInstance;
import org.homio.api.util.SecureString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ContextService {

    String MQTT_SERVICE = "MQTT";

    @NotNull Context context();

    @NotNull String getPrimaryMqttEntity();

    void registerEntityTypeForSelection(@NotNull Class<? extends HasEntityIdentifier> entityClass, @NotNull String type);

    void registerUserRoleResource(@NotNull String resource);

    default @Nullable MQTTEntityService getMQTTEntityService(@NotNull String entityID) {
        return getService(entityID, MQTTEntityService.class);
    }

    @Nullable EntityService.ServiceInstance getEntityService(@NotNull String entityID);

    default boolean isHasEntityService(@NotNull String entityID) {
        return getEntityService(entityID) != null;
    }

    void addEntityService(@NotNull String entityID, @NotNull EntityService.ServiceInstance service);

    @Nullable ServiceInstance removeEntityService(@NotNull String entityID);

    private <T> @Nullable T getService(@NotNull String entityID, @NotNull Class<T> serviceClass) {
        BaseEntity entity = context().db().getEntity(entityID);
        if (entity != null && !serviceClass.isAssignableFrom(entity.getClass())) {
            throw new IllegalStateException(format("Entity: '%s' has type: '%s' but require: '%s'", entityID, entity.getType(), serviceClass.getSimpleName()));
        }
        return (T) entity;
    }

    interface MQTTEntityService extends HasEntityIdentifier {

        @NotNull String getUser();

        @NotNull SecureString getPassword();

        @NotNull String getHostname();

        int getPort();

        default void publish(@NotNull String topic) {
            publish(topic, new byte[0], 0, false);
        }

        default void publish(@NotNull String topic, byte[] payload) {
            publish(topic, payload, 0, false);
        }

        void publish(@NotNull String topic, byte[] payload, int qos, boolean retained);

        default void addListener(@NotNull String topic, @NotNull String discriminator, @NotNull Consumer<String> listener) {
            addListener(topic, discriminator, (s, value) -> listener.accept(value));
        }

        void addListener(@NotNull String topic, @NotNull String discriminator, @NotNull BiConsumer<String, String> listener);

        // topic i.e.: +/tele/# or tele/#
        default void addListener(@NotNull Set<String> topics, @NotNull String discriminator, @NotNull BiConsumer<String, String> listener) {
            for (String topic : topics) {
                addListener(topic, discriminator, listener);
            }
        }

        void removeListener(@NotNull String topic, @NotNull String discriminator);

        static @NotNull String buildMqttListenEvent(@NotNull String mqttEntityID, @NotNull String topic) {
            return mqttEntityID + LIST_DELIMITER + topic;
        }
    }
}
