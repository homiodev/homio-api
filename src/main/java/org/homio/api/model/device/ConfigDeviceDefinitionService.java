package org.homio.api.model.device;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.io.file.PathUtils;
import org.homio.api.exception.ServerException;
import org.homio.api.util.CommonUtils;
import org.homio.api.widget.template.WidgetDefinition;
import org.homio.hquery.Curl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.homio.api.ContextSetting.IS_DEV_ENVIRONMENT;
import static org.homio.api.util.CommonUtils.getErrorMessage;
import static org.homio.api.util.JsonUtils.OBJECT_MAPPER;

@Log4j2
public class ConfigDeviceDefinitionService {

  private final String fileName;
  private final Path localFilePath;
  private final @NotNull Map<String, ModelDevices> modelIdToDevices = new HashMap<>();
  private final @NotNull ReentrantLock midLock = new ReentrantLock();
  private long localConfigFileHashCode;
  @Setter
  private String serverFilePath;
  @Getter
  private boolean equalServerConfig = true;
  /**
   * Contains model/icon/iconColor/some setting config i.e. occupancy_timeout min..max values
   */
  @Getter
  private @NotNull Map<String, ConfigDeviceDefinition> deviceDefinitions = Collections.emptyMap();
  @Getter
  private @NotNull Map<EndpointMatch, List<ConfigDeviceDefinition>> endpointDeviceDefinitions = Collections.emptyMap();
  /**
   * Endpoints market with defined color, icon, etc...
   */
  @Getter
  private @NotNull Map<String, ConfigDeviceEndpoint> deviceEndpoints = Collections.emptyMap();
  @Getter
  private @NotNull Map<String, ConfigDeviceEndpoint> deviceAliasEndpoints = Collections.emptyMap();
  private @NotNull Set<String> ignoreEndpoints = Collections.emptySet();
  private @NotNull Set<String> hiddenEndpoints = Collections.emptySet();

  /**
   * Create config service instance
   *
   * @param fileName - configuration file name
   */
  @SneakyThrows
  public ConfigDeviceDefinitionService(@NotNull String fileName) {
    this.fileName = fileName;
    this.localFilePath = CommonUtils.getConfigPath().resolve(fileName);
    this.serverFilePath = "https://raw.githubusercontent.com/homiodev/static-files/master/" + fileName;

    URL localZdFile = getClass().getClassLoader().getResource(fileName);
    if (localZdFile == null) {
      throw new ServerException("Config resource: " + fileName + " not found");
    }
    Path configFileLocation = localFilePath;
    if (isRequireCopyJarFileToFileSystem(configFileLocation, localZdFile)) {
      log.info("Copy file: {} to {}", fileName, configFileLocation.toAbsolutePath());
      PathUtils.copy(localZdFile::openStream, configFileLocation, StandardCopyOption.REPLACE_EXISTING);
    }
    localConfigFileHashCode = Files.size(configFileLocation);
    readDeviceDefinitions();
  }

  @SneakyThrows
  private static boolean isRequireCopyJarFileToFileSystem(Path configFileLocation, URL localZdFile) {
    if (!Files.exists(configFileLocation) || IS_DEV_ENVIRONMENT) {
      return true;
    }
    ObjectNode jarFileNode = OBJECT_MAPPER.readValue(localZdFile, ObjectNode.class);
    ObjectNode localFileNode = OBJECT_MAPPER.readValue(configFileLocation.toFile(), ObjectNode.class);
    return jarFileNode.get("version").asInt() > localFileNode.get("version").asInt();
  }

  private static void addDeviceDefinition(HashMap<String, ConfigDeviceDefinition> definitions, ConfigDeviceDefinition node, String model) {
    if (definitions.put(model, node) != null) {
      throw new IllegalArgumentException("Unable to handle few config device definitions with same name");
    }
  }

  public boolean isIgnoreEndpoint(@NotNull String endpoint) {
    return ignoreEndpoints.contains(endpoint);
  }

  public boolean isHideEndpoint(@NotNull String endpoint) {
    return hiddenEndpoints.contains(endpoint);
  }

  public @NotNull Map<Integer, ConfigDeviceDefinition.Pin> findDevicePin(@NotNull String model) {
    ConfigDeviceDefinition device = deviceDefinitions.get(model);
    if (device != null && device.getPins() != null) {
      return device.getPins().stream().collect(Collectors.toMap(ConfigDeviceDefinition.Pin::getIndex, Function.identity()));
    }
    return Map.of();
  }

  public @NotNull List<ConfigDeviceDefinition> findDeviceDefinitionModels(
    @Nullable String model,
    @NotNull Set<String> endpoints) {
    int endpointHash = endpoints.hashCode();
    ModelDevices modelDevices = modelIdToDevices.get(model);
    if (modelDevices == null || modelDevices.hashCode != endpointHash) {
      try {
        midLock.lock();
        modelDevices = modelIdToDevices.get(model);
        if (modelDevices == null || modelDevices.hashCode != endpointHash) {
          modelDevices = new ModelDevices(endpointHash, findDeviceDefinitionModelsInternal(model, endpoints));
          modelIdToDevices.put(model, modelDevices);
        }
      } finally {
        midLock.unlock();
      }
    }
    return modelDevices.devices;
  }

  @SneakyThrows
  public void checkServerConfiguration() {
    if (equalServerConfig) {
      long serverConfigHash = Curl.getFileSize(new URI(serverFilePath).toURL());
      if (serverConfigHash != localConfigFileHashCode) {
        equalServerConfig = false;
      }
    }
  }

  public void syncConfigurationFile() {
    if (!equalServerConfig) {
      try {
        log.info("Downloading new {} device configuration file", fileName);
        Curl.download(serverFilePath, localFilePath);
        // currently we use only hash to distinguish if file os newer
        localConfigFileHashCode = Files.size(localFilePath);
        equalServerConfig = true;
        readDeviceDefinitions();
        log.info("New {} device configuration file downloaded", fileName);
      } catch (Exception ex) {
        log.warn("Unable to reload {} device configuration file: {}", fileName, getErrorMessage(ex));
      }
    }
  }

  public @NotNull JsonNode getDeviceOptions(@NotNull List<ConfigDeviceDefinition> devices) {
    JsonNode jsonNode = null;
    if (!devices.isEmpty()) {
      jsonNode = devices.get(0).getOptions();
    }
    return jsonNode == null ? OBJECT_MAPPER.createObjectNode() : jsonNode;
  }

  public @NotNull List<WidgetDefinition> getDeviceWidgets(@NotNull List<ConfigDeviceDefinition> devices) {
    return devices.stream()
      .filter(d -> d.getWidgets() != null)
      .flatMap(d -> d.getWidgets().stream()).toList();
  }

  @SneakyThrows
  private void readDeviceDefinitions() {
    ConfigDeviceDefinitions deviceConfigurations = OBJECT_MAPPER.readValue(localFilePath.toFile(), ConfigDeviceDefinitions.class);

    var definitions = new HashMap<String, ConfigDeviceDefinition>();
    for (ConfigDeviceDefinition node : deviceConfigurations.getDevices()) {
      addDeviceDefinition(definitions, node, node.getName());
      if (node.getModels() != null) {
        for (String model : node.getModels()) {
          addDeviceDefinition(definitions, node, model);
        }
      }
    }
    var endpointDefinitions = new HashMap<EndpointMatch, List<ConfigDeviceDefinition>>();
    for (ConfigDeviceDefinition node : deviceConfigurations.getDevices()) {
      if (node.getEndpoints() != null) {
        for (String endpoint : node.getEndpoints()) {
          val endpointMatch = new EndpointMatch(Stream.of(endpoint.split("~")).collect(Collectors.toSet()));
          endpointDefinitions.putIfAbsent(endpointMatch, new ArrayList<>());
          endpointDefinitions.get(endpointMatch).add(node);
        }
      }
    }

    var aliasEndpoints = new HashMap<String, ConfigDeviceEndpoint>();
    for (ConfigDeviceEndpoint deviceEndpoint : deviceConfigurations.getEndpoints()) {
      if (deviceEndpoint.getAlias() != null) {
        for (String alias : deviceEndpoint.getAlias()) {
          aliasEndpoints.put(alias, deviceEndpoint);
        }
      }
    }

    if (deviceConfigurations.getIgnoreEndpoints() != null) {
      ignoreEndpoints = deviceConfigurations.getIgnoreEndpoints();
    }
    if (deviceConfigurations.getHiddenEndpoints() != null) {
      hiddenEndpoints = deviceConfigurations.getHiddenEndpoints();
    }

    endpointDeviceDefinitions = endpointDefinitions;
    deviceDefinitions = definitions;
    deviceEndpoints = deviceConfigurations
      .getEndpoints()
      .stream()
      .collect(Collectors.toMap(ConfigDeviceEndpoint::getName, Function.identity()));
    deviceAliasEndpoints = aliasEndpoints;
  }

  private @NotNull List<ConfigDeviceDefinition> findDeviceDefinitionModelsInternal(
    @Nullable String modelId,
    @NotNull Set<String> endpoints) {
    List<ConfigDeviceDefinition> devices = new ArrayList<>();
    ConfigDeviceDefinition device = deviceDefinitions.get(modelId);
    if (device != null) {
      devices.add(device);
    }
    for (Map.Entry<EndpointMatch, List<ConfigDeviceDefinition>> item : endpointDeviceDefinitions.entrySet()) {
      if (endpoints.containsAll(item.getKey().andEndpoints)) {
        devices.addAll(item.getValue());
      }
    }
    return devices;
  }

  /**
   * @param andEndpoints minimum of endpoints to match
   */
  private record EndpointMatch(@NotNull Set<String> andEndpoints) {

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      EndpointMatch that = (EndpointMatch) o;

      return andEndpoints.equals(that.andEndpoints);
    }
  }

  @AllArgsConstructor
  private static class ModelDevices {

    private final int hashCode;
    private final @NotNull List<ConfigDeviceDefinition> devices;
  }
}
