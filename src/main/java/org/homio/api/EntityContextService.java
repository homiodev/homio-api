package org.homio.api;

import org.homio.api.entity.BaseEntity;
import org.homio.api.model.HasEntityIdentifier;
import org.homio.api.util.SecureString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static java.lang.String.format;

public interface EntityContextService {

    String MQTT_SERVICE = "MQTT";

    @NotNull EntityContext getEntityContext();

    void registerEntityTypeForSelection(@NotNull Class<? extends HasEntityIdentifier> entityClass, @NotNull String type);

    default MQTTEntityService getMQTTEntityService(String entityID) {
        return getService(entityID, MQTTEntityService.class);
    }

    @Nullable
    private <T> T getService(String entityID, Class<T> serviceClass) {
        BaseEntity entity = getEntityContext().getEntity(entityID);
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

        void addListener(String topic, String discriminator, Consumer<Object> listener);

        void removeListener(String topic, String discriminator);
    }
}
