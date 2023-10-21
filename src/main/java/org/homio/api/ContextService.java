package org.homio.api;

import static java.lang.String.format;
import static org.homio.api.entity.HasJsonData.LIST_DELIMITER;

import java.util.function.Consumer;
import org.homio.api.entity.BaseEntity;
import org.homio.api.model.HasEntityIdentifier;
import org.homio.api.service.EntityService;
import org.homio.api.service.EntityService.ServiceInstance;
import org.homio.api.state.State;
import org.homio.api.util.SecureString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ContextService {

    String MQTT_SERVICE = "MQTT";

    @NotNull Context context();

    void registerEntityTypeForSelection(@NotNull Class<? extends HasEntityIdentifier> entityClass, @NotNull String type);

    void registerUserRoleResource(String resource);

    default MQTTEntityService getMQTTEntityService(String entityID) {
        return getService(entityID, MQTTEntityService.class);
    }

    EntityService.ServiceInstance getEntityService(String entityID);

    default boolean isHasEntityService(String entityID) {
        return getEntityService(entityID) != null;
    }

    void addEntityService(String entityID, EntityService.ServiceInstance service);

    ServiceInstance removeEntityService(String entityID);

    @Nullable
    private <T> T getService(String entityID, Class<T> serviceClass) {
        BaseEntity entity = context().db().getEntity(entityID);
        if (entity != null && !serviceClass.isAssignableFrom(entity.getClass())) {
            throw new IllegalStateException(format("Entity: '%s' has type: '%s' but require: '%s'", entityID, entity.getType(), serviceClass.getSimpleName()));
        }
        return (T) entity;
    }

    interface MQTTEntityService extends HasEntityIdentifier {

        String getUser();

        SecureString getPassword();

        String getHostname();

        int getPort();

        void publish(String topic, byte[] payload, int qos, boolean retained);

        void addListener(String topic, String discriminator, Consumer<State> listener);

        void removeListener(String topic, String discriminator);

        static String buildMqttListenEvent(String mqttEntityID, String topic) {
            return mqttEntityID + LIST_DELIMITER + topic;
        }
    }
}
