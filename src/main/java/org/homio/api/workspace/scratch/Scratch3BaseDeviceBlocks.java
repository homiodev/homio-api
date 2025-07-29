package org.homio.api.workspace.scratch;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.NotImplementedException;
import org.homio.api.AddonEntrypoint;
import org.homio.api.Context;
import org.homio.api.entity.device.DeviceBaseEntity;
import org.homio.api.entity.device.DeviceEndpointsBehaviourContract;
import org.homio.api.exception.NotFoundException;
import org.homio.api.model.endpoint.DeviceEndpoint;
import org.homio.api.workspace.WorkspaceBlock;
import org.homio.api.workspace.scratch.MenuBlock.ServerMenuBlock;
import org.homio.api.workspace.scratch.MenuBlock.StaticMenuBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Base parent for all devices that exposes some endpoints */
@SuppressWarnings("unused")
@Log4j2
@Getter
public abstract class Scratch3BaseDeviceBlocks<D extends DeviceBaseEntity>
    extends Scratch3ExtensionBlocks {

  public static final String DEVICE = "DEVICE";
  public static final String ENDPOINT = "ENDPOINT";
  public static final String DEVICE__BASE_URL = "rest/device";

  @NotNull protected final ServerMenuBlock deviceMenu;
  private final String devicePrefix;
  private ServerMenuBlock deviceReadMenu;
  private ServerMenuBlock deviceWriteMenu;
  private ServerMenuBlock endpointMenu;
  private ServerMenuBlock readEndpointMenu;
  private ServerMenuBlock writeEndpointMenu;
  private StaticMenuBlock<OnOff> onOffMenu;
  private ServerMenuBlock deviceWriteBoolMenu;
  private ServerMenuBlock writeBoolEndpointMenu;

  public Scratch3BaseDeviceBlocks(
      @Nullable String color,
      @NotNull Context context,
      @Nullable AddonEntrypoint addonEntrypoint,
      @NotNull String devicePrefix) {
    super(color, context, addonEntrypoint);
    setParent(ScratchParent.devices);
    this.devicePrefix = devicePrefix;
    this.deviceMenu =
        menuServer(
            "deviceMenu", "%s?prefix=%s".formatted(DEVICE__BASE_URL, devicePrefix), "Device", "-");
  }

  protected @NotNull DeviceEndpoint getDeviceEndpoint(
      @NotNull WorkspaceBlock workspaceBlock,
      @NotNull ServerMenuBlock deviceMenu,
      @NotNull ServerMenuBlock endpointMenu) {
    String endpointID = workspaceBlock.getMenuValue(ENDPOINT, endpointMenu);
    return getDeviceEndpoint(workspaceBlock, deviceMenu, endpointID);
  }

  protected @NotNull DeviceEndpoint getDeviceEndpoint(@NotNull WorkspaceBlock workspaceBlock) {
    return getDeviceEndpoint(workspaceBlock, deviceMenu, endpointMenu);
  }

  protected @NotNull DeviceEndpoint getDeviceEndpoint(
      @NotNull WorkspaceBlock workspaceBlock,
      @NotNull ServerMenuBlock deviceMenu,
      @NotNull String endpointID) {
    String ieeeAddress = workspaceBlock.getMenuValue(DEVICE, deviceMenu);
    DeviceEndpoint endpoint = getDeviceEndpoint(ieeeAddress, endpointID);

    if (endpoint == null) {
      workspaceBlock.logErrorAndThrow("Unable to find endpoint: {}/{}", ieeeAddress, endpointID);
      throw new NotImplementedException();
    }
    return endpoint;

    /*if (endpoint == null) {
      // wait for endpoint to be online at most 10 minutes
      Lock onlineStatus = workspaceBlock.getLockManager().getLock(workspaceBlock,
        format("endpoint-%s-%s", ieeeAddress, endpointID), Status.ONLINE);
      if (onlineStatus.await(workspaceBlock, 10, TimeUnit.MINUTES)) {
        endpoint = getDeviceEndpoint(ieeeAddress, endpointID);
      }
    }
    if (endpoint != null) {
      return endpoint;
    }

    workspaceBlock.logErrorAndThrow("Unable to find endpoint: {}/{}", ieeeAddress, endpointID);
    throw new NotImplementedException();*/
  }

  protected @Nullable DeviceEndpoint getDeviceEndpoint(
      @NotNull String ieeeAddress, @NotNull String endpointID) {
    DeviceBaseEntity entity = getDevice(ieeeAddress);
    return ((DeviceEndpointsBehaviourContract) entity).getDeviceEndpoint(endpointID);
  }

  protected @NotNull DeviceBaseEntity getDevice(@NotNull String ieeeAddress) {
    List<DeviceBaseEntity> entities = context.db().getDeviceEntity(ieeeAddress, devicePrefix);
    if (entities.isEmpty()) {
      throw new NotFoundException("Unable to find entity: " + ieeeAddress);
    }
    if (entities.size() > 1) {
      throw new NotFoundException("Found multiple entities with id: " + ieeeAddress);
    }
    return entities.getFirst();
  }

  @SneakyThrows
  protected void executeWhenDeviceReady(
      @NotNull WorkspaceBlock workspaceBlock, @NotNull Consumer<D> consumer) {
    executeWhenDeviceReady(
        workspaceBlock,
        (Function<D, Void>)
            entity -> {
              consumer.accept(entity);
              return null;
            });
  }

  @SneakyThrows
  protected <T> T executeWhenDeviceReady(
      @NotNull WorkspaceBlock workspaceBlock, @NotNull Function<D, T> consumer) {
    String deviceId = workspaceBlock.getMenuValue(DEVICE, deviceMenu);
    D entity = context.db().getRequire(deviceId);
    if (!entity.getStatus().isOnline()) {
      var readyLock =
          workspaceBlock
              .getLockManager()
              .createLock(workspaceBlock, "device-ready-" + entity.getIeeeAddress());
      if (readyLock.await(workspaceBlock)) {
        entity = context.db().getRequire(deviceId);
        if (entity.getStatus().isOnline()) {
          return consumer.apply(entity);
        } else {
          log.error(
              "Unable to execute step for device: <{}>. Waited for ready status but got: <{}>",
              entity.getTitle(),
              entity.getStatus());
        }
      }
    } else {
      if (entity.getStatus().isOnline()) {
        return consumer.apply(entity);
      }
    }
    return null;
  }

  public ServerMenuBlock getDeviceReadMenu() {
    if (deviceReadMenu == null) {
      deviceReadMenu =
          menuServer(
              "deviceReadMenu",
              "%s?prefix=%s&access=read".formatted(DEVICE__BASE_URL, devicePrefix),
              "Device",
              "-");
    }
    return deviceReadMenu;
  }

  public ServerMenuBlock getDeviceWriteMenu() {
    if (deviceWriteMenu == null) {
      deviceWriteMenu =
          menuServer(
              "deviceWriteMenu",
              "%s?prefix=%s&access=write".formatted(DEVICE__BASE_URL, devicePrefix),
              "Device",
              "-");
    }
    return deviceWriteMenu;
  }

  public ServerMenuBlock getEndpointMenu() {
    if (endpointMenu == null) {
      endpointMenu =
          menuServer("endpointMenu", "%s/endpoints".formatted(DEVICE__BASE_URL), "Endpoint", "-")
              .setDependency(deviceMenu);
    }
    return endpointMenu;
  }

  public ServerMenuBlock getReadEndpointMenu() {
    if (readEndpointMenu == null) {
      readEndpointMenu =
          menuServer(
                  "readEndpointMenu",
                  "%s/endpoints?access=read".formatted(DEVICE__BASE_URL),
                  "Endpoint",
                  "-")
              .setDependency(getDeviceReadMenu());
    }
    return readEndpointMenu;
  }

  public ServerMenuBlock getWriteEndpointMenu() {
    if (writeEndpointMenu == null) {
      writeEndpointMenu =
          menuServer(
                  "writeEndpointMenu",
                  "%s/endpoints?access=write".formatted(DEVICE__BASE_URL),
                  "Endpoint",
                  "-")
              .setDependency(getDeviceWriteMenu());
    }
    return writeEndpointMenu;
  }

  public ServerMenuBlock getDeviceWriteBoolMenu() {
    if (deviceWriteBoolMenu == null) {
      deviceWriteBoolMenu =
          menuServer(
              "deviceWriteBoolMenu",
              "%s?prefix=%s&access=write&type=bool".formatted(DEVICE__BASE_URL, devicePrefix),
              "Device",
              "-");
    }
    return deviceWriteBoolMenu;
  }

  public ServerMenuBlock getWriteBoolEndpointMenu() {
    if (writeBoolEndpointMenu == null) {
      writeBoolEndpointMenu =
          menuServer(
                  "writeBoolEndpointMenu",
                  "%s/endpoints?access=write&type=bool".formatted(DEVICE__BASE_URL),
                  "Endpoint",
                  "-")
              .setDependency(getDeviceWriteMenu());
    }
    return writeBoolEndpointMenu;
  }

  public StaticMenuBlock<OnOff> getOnOffMenu() {
    if (onOffMenu == null) {
      onOffMenu = menuStatic("onOffMenu", OnOff.class, OnOff.off);
    }
    return onOffMenu;
  }

  public enum OnOff {
    on,
    off
  }
}
