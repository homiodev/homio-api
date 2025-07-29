package org.homio.api;

import static java.lang.String.format;
import static org.homio.api.entity.HasJsonData.LIST_DELIMITER;
import static org.homio.api.util.CommonUtils.getErrorMessage;
import static org.homio.api.util.JsonUtils.OBJECT_MAPPER;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pivovarit.function.ThrowingBiConsumer;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.homio.api.entity.BaseEntity;
import org.homio.api.model.HasEntityIdentifier;
import org.homio.api.service.BaseService;
import org.homio.api.service.EntityService;
import org.homio.api.util.SecureString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ContextService {

  // Require to select mqtt service on UI from addons
  String MQTT_SERVICE = "MQTT";

  @NotNull
  Context context();

  @NotNull
  String getPrimaryMqttEntity();

  void registerEntityTypeForSelection(
      @NotNull Class<? extends HasEntityIdentifier> entityClass, @NotNull String type);

  default @Nullable MQTTEntityService getMQTTEntityService(@NotNull String entityID) {
    return getService(entityID, MQTTEntityService.class);
  }

  @Nullable
  EntityService.ServiceInstance getEntityService(@NotNull String entityID);

  default boolean isHasService(@NotNull String entityID) {
    return getEntityService(entityID) != null;
  }

  void addService(@NotNull String entityID, @NotNull BaseService service);

  // return new url to uses as proxy
  @NotNull
  String registerUrlProxy(
      @NotNull String entityID, @NotNull String url, @NotNull Consumer<RouteProxyBuilder> builder);

  boolean unRegisterUrlProxy(@NotNull String entityID);

  @Nullable
  BaseService removeService(@NotNull String entityID);

  private <T> @Nullable T getService(@NotNull String entityID, @NotNull Class<T> serviceClass) {
    BaseEntity entity = context().db().get(entityID);
    if (entity != null && !serviceClass.isAssignableFrom(entity.getClass())) {
      throw new IllegalStateException(
          format(
              "Entity: '%s' has type: '%s' but require: '%s'",
              entityID, entity.getType(), serviceClass.getSimpleName()));
    }
    return (T) entity;
  }

  interface MQTTEntityService extends HasEntityIdentifier {

    static @NotNull String buildMqttListenEvent(
        @NotNull String mqttEntityID, @NotNull String topic) {
      return mqttEntityID + LIST_DELIMITER + topic;
    }

    @Nullable
    String getLastValue(@NotNull String topic);

    @NotNull
    String getUser();

    @NotNull
    SecureString getPassword();

    @NotNull
    String getHostname();

    int getPort();

    default void publish(@NotNull String topic) {
      publish(topic, new byte[0], 0, false);
    }

    default void publish(@NotNull String topic, byte[] payload) {
      publish(topic, payload, 0, false);
    }

    void publish(@NotNull String topic, byte[] payload, int qos, boolean retained);

    default void addListener(
        @NotNull String topic, @NotNull String discriminator, @NotNull Consumer<String> listener) {
      addListener(topic, discriminator, (s, value) -> listener.accept(value));
    }

    void addListener(
        @NotNull String topic,
        @NotNull String discriminator,
        @NotNull BiConsumer<String, String> listener);

    // topic i.e.: +/tele/# or tele/#
    default void addListener(
        @NotNull Set<String> topics,
        @NotNull String discriminator,
        @NotNull BiConsumer<String, String> listener) {
      for (String topic : topics) {
        addListener(topic, discriminator, listener);
      }
    }

    void removeListener(@Nullable String topic, @NotNull String discriminator);

    default void removeListener(@NotNull String discriminator) {
      removeListener(null, discriminator);
    }

    default void addPayloadListener(
        @NotNull Set<String> topic,
        @NotNull String discriminator,
        @NotNull String entityID,
        @NotNull Logger log,
        @NotNull ThrowingBiConsumer<String, ObjectNode, Exception> handler) {
      addPayloadListener(topic, discriminator, entityID, log, Level.INFO, handler);
    }

    default void addPayloadListener(
        @NotNull Set<String> topic,
        @NotNull String discriminator,
        @NotNull String entityID,
        @NotNull Logger log,
        @NotNull Level logLevel,
        @NotNull ThrowingBiConsumer<String, ObjectNode, Exception> handler) {
      addListener(
          topic,
          discriminator,
          (realTopic, value) -> {
            log.log(logLevel, "[{}]: {} {}: {}", discriminator, entityID, realTopic, value);
            String payload = value == null ? "" : value;
            if (!payload.isEmpty()) {
              ObjectNode node;
              try {
                node = OBJECT_MAPPER.readValue(payload, ObjectNode.class);
              } catch (Exception ex) {
                node = OBJECT_MAPPER.createObjectNode().put("raw", payload);
              }
              try {
                handler.accept(realTopic, node);
              } catch (Exception ex) {
                log.error(
                    "[{}]: Unable to handle mqtt payload: {}. Msg: {}",
                    entityID,
                    payload,
                    getErrorMessage(ex));
              }
            }
          });
    }
  }

  interface RouteProxyBuilder {

    void setUrlProducer(Function<HttpServletRequest, ProxyUrl> urlProducer);

    void setResponseHeaders(Function<ProxyUrl, Map<String, String>> responseHeaderBuilder);

    record ProxyUrl(@NotNull String url, @Nullable Map<String, List<String>> headers) {}
  }
}
