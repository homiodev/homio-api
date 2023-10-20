package org.homio.api.workspace.scratch;

import static java.lang.String.format;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.AddonEntrypoint;
import org.homio.api.EntityContext;
import org.homio.api.EntityContextBGP.ThreadContext;
import org.homio.api.entity.device.DeviceBaseEntity;
import org.homio.api.entity.device.DeviceEndpointsBehaviourContract;
import org.homio.api.model.Status;
import org.homio.api.model.endpoint.DeviceEndpoint;
import org.homio.api.state.DecimalType;
import org.homio.api.state.OnOffType;
import org.homio.api.state.State;
import org.homio.api.workspace.Lock;
import org.homio.api.workspace.WorkspaceBlock;
import org.homio.api.workspace.scratch.MenuBlock.ServerMenuBlock;
import org.homio.api.workspace.scratch.MenuBlock.StaticMenuBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class Scratch3BaseDeviceBlocks extends Scratch3ExtensionBlocks {

    private static final String DEVICE = "DEVICE";
    private static final String ENDPOINT = "ENDPOINT";
    private static final String DEVICE__BASE_URL = "rest/device";

    private final @NotNull ServerMenuBlock deviceMenu;
    private final @NotNull ServerMenuBlock deviceReadMenu;
    private final @NotNull ServerMenuBlock deviceWriteMenu;
    private final @NotNull ServerMenuBlock endpointMenu;
    private final @NotNull ServerMenuBlock temperatureDeviceMenu;
    private final @NotNull ServerMenuBlock humidityDeviceMenu;
    private final @NotNull ServerMenuBlock readEndpointMenu;
    private final @NotNull ServerMenuBlock writeEndpointMenu;
    private final @NotNull StaticMenuBlock<OnOff> onOffMenu;
    private final @NotNull ServerMenuBlock deviceWriteBoolMenu;
    private final @NotNull ServerMenuBlock writeBoolEndpointMenu;

    public Scratch3BaseDeviceBlocks(String color, EntityContext entityContext, AddonEntrypoint addonEntrypoint) {
        super(color, entityContext, addonEntrypoint);
        setParent("devices");

        this.onOffMenu = menuStatic("onOffMenu", OnOff.class, OnOff.off);
        this.deviceMenu = menuServer("deviceMenu", DEVICE__BASE_URL, "Device", "-");
        this.deviceReadMenu = menuServer("deviceReadMenu", DEVICE__BASE_URL + "?access=read", "Device", "-");
        this.deviceWriteBoolMenu = menuServer("deviceWriteBoolMenu", DEVICE__BASE_URL + "?access=write&type=bool", "Device", "-");
        this.deviceWriteMenu = menuServer("deviceWriteMenu", DEVICE__BASE_URL + "?access=write", "Device", "-");
        this.temperatureDeviceMenu = menuServer("temperatureDeviceMenu", DEVICE__BASE_URL + "/temperature", "Device", "-");
        this.humidityDeviceMenu = menuServer("humidityDeviceMenu", DEVICE__BASE_URL + "/humidity", "Device", "-");
        this.endpointMenu = menuServer("endpointMenu", DEVICE__BASE_URL + "/endpoints", "Endpoint", "-")
            .setDependency(this.deviceMenu);
        this.readEndpointMenu = menuServer("readEndpointMenu", DEVICE__BASE_URL + "/endpoints?access=read", "Endpoint", "-")
            .setDependency(this.deviceReadMenu);
        this.writeEndpointMenu = menuServer("writeEndpointMenu", DEVICE__BASE_URL + "/endpoints?access=write", "Endpoint", "-")
            .setDependency(this.deviceWriteMenu);
        this.writeBoolEndpointMenu = menuServer("writeBoolEndpointMenu", DEVICE__BASE_URL + "/endpoints?access=write&type=bool", "Endpoint", "-")
            .setDependency(this.deviceWriteMenu);
        // reporter blocks
        blockReporter(50, "time_since_last_event", "time since last event [ENDPOINT] of [DEVICE]",
            workspaceBlock -> {
                Duration timeSinceLastEvent = getDeviceEndpoint(workspaceBlock).getTimeSinceLastEvent();
                return new DecimalType(timeSinceLastEvent.toSeconds()).setUnit("sec");
            }, block -> {
                block.addArgument(ENDPOINT, this.endpointMenu);
                block.addArgument(DEVICE, this.deviceMenu);
                block.appendSpace();
            });

        blockReporter(51, "value", "[ENDPOINT] of [DEVICE]", this::getDeviceEndpointState, block -> {
            block.addArgument(ENDPOINT, this.endpointMenu);
            block.addArgument(DEVICE, this.deviceMenu);
            block.overrideColor("#84185c");
        });

        blockReporter(52, "temperature", "temperature [DEVICE]",
            workspaceBlock -> getDeviceEndpoint(workspaceBlock, deviceMenu, "temperature").getLastValue(),
            block -> {
                block.addArgument(DEVICE, this.temperatureDeviceMenu);
                block.overrideColor("#307596");
            });

        blockReporter(53, "humidity", "humidity [DEVICE]",
            workspaceBlock -> getDeviceEndpoint(workspaceBlock, deviceMenu, "humidity").getLastValue(),
            block -> {
                block.addArgument(DEVICE, humidityDeviceMenu);
                block.overrideColor("#3B8774");
            });

        // command blocks
        blockCommand(80, "read_edp", "Read [ENDPOINT] value of [DEVICE]", workspaceBlock ->
                getDeviceEndpoint(workspaceBlock, deviceReadMenu, readEndpointMenu).readValue(),
            block -> {
                block.addArgument(ENDPOINT, readEndpointMenu);
                block.addArgument(DEVICE, deviceReadMenu);
            });

        blockCommand(81, "write_edp", "Write [ENDPOINT] value [VALUE] of [DEVICE]", workspaceBlock -> {
            Object value = workspaceBlock.getInput(VALUE, true);
            getDeviceEndpoint(workspaceBlock, deviceWriteMenu, writeEndpointMenu).writeValue(State.of(value));
        }, block -> {
            block.addArgument(ENDPOINT, writeEndpointMenu);
            block.addArgument(DEVICE, deviceWriteMenu);
            block.addArgument(VALUE, "-");
        });

        blockCommand(81, "write_bool", "Set [ENDPOINT] [VALUE] of [DEVICE]", workspaceBlock -> {
            OnOff value = workspaceBlock.getMenuValue(VALUE, this.onOffMenu);
            getDeviceEndpoint(workspaceBlock, deviceWriteBoolMenu, writeBoolEndpointMenu).writeValue(OnOffType.of(value == OnOff.on));
        }, block -> {
            block.addArgument(ENDPOINT, writeBoolEndpointMenu);
            block.addArgument(DEVICE, deviceWriteBoolMenu);
            block.addArgument(VALUE, onOffMenu);
        });

        // hat blocks
        blockHat(90, "when_value_change", "When [ENDPOINT] of [DEVICE] changed", this::whenValueChange, block -> {
            block.addArgument(ENDPOINT, endpointMenu);
            block.addArgument(DEVICE, deviceMenu);
        });

        blockHat(91, "when_value_change_to", "When [ENDPOINT] of [DEVICE] changed to [VALUE]", this::whenValueChangeTo, block -> {
            block.addArgument(ENDPOINT, endpointMenu);
            block.addArgument(DEVICE, deviceMenu);
            block.addArgument(VALUE, "-");
        });

        blockHat(92, "when_no_value_change", "No changes [ENDPOINT] of [DEVICE] during [DURATION]sec.",
            this::whenNoValueChangeSince, block -> {
                block.addArgument(ENDPOINT, endpointMenu);
                block.addArgument(DEVICE, deviceMenu);
                block.addArgument("DURATION", 60);
            });
    }

    private @NotNull DeviceEndpoint getDeviceEndpoint(
        @NotNull WorkspaceBlock workspaceBlock,
        @NotNull ServerMenuBlock deviceMenu,
        @NotNull ServerMenuBlock endpointMenu) {
        String endpointID = workspaceBlock.getMenuValue(ENDPOINT, endpointMenu);
        return getDeviceEndpoint(workspaceBlock, deviceMenu, endpointID);
    }

    private void whenValueChangeTo(@NotNull WorkspaceBlock workspaceBlock) {
        workspaceBlock.handleNext(next -> {
            DeviceEndpoint endpoint = getDeviceEndpoint(workspaceBlock);
            String value = workspaceBlock.getInputString(VALUE);
            if (StringUtils.isEmpty(value)) {
                workspaceBlock.logErrorAndThrow("Value must be not empty");
            }
            Lock lock = workspaceBlock.getLockManager().getLock(workspaceBlock);

            endpoint.addChangeListener(workspaceBlock.getId(), state -> {
                if (state.stringValue().equals(value)) {
                    lock.signalAll();
                }
            });
            workspaceBlock.onRelease(() -> endpoint.removeChangeListener(workspaceBlock.getId()));
            workspaceBlock.subscribeToLock(lock, next::handle);
        });
    }

    /**
     * Handler to wait specific seconds after some event and fire event after that
     */
    private void whenNoValueChangeSince(@NotNull WorkspaceBlock workspaceBlock) {
        workspaceBlock.handleNext(next -> {
            Integer secondsToWait = workspaceBlock.getInputInteger("DURATION");
            if (secondsToWait < 1) {
                workspaceBlock.logErrorAndThrow("Duration must be greater than 1 seconds. Value: {}", secondsToWait);
            }
            DeviceEndpoint endpoint = getDeviceEndpoint(workspaceBlock);
            Lock eventOccurredLock = workspaceBlock.getLockManager().getLock(workspaceBlock);

            // add listener on target endpoint for any changes and wake up lock
            endpoint.addChangeListener(workspaceBlock.getId(), state -> eventOccurredLock.signalAll());

            // thread context that will be started when endpoint's listener fire event
            ThreadContext<Void> delayThread = entityContext.bgp().builder("when-no-val-" + workspaceBlock.getId())
                                                           .delay(Duration.ofSeconds(secondsToWait))
                                                           .tap(workspaceBlock::setThreadContext)
                                                           .execute(next::handle, false);
            // remove listener from endpoint. ThreadContext will be cancelled automatically
            workspaceBlock.onRelease(() ->
                endpoint.removeChangeListener(workspaceBlock.getId()));
            // subscribe to lock that will restart delay thread after event
            workspaceBlock.subscribeToLock(eventOccurredLock, delayThread::reset);
        });
    }

    private void whenValueChange(@NotNull WorkspaceBlock workspaceBlock) {
        workspaceBlock.handleNext(next -> {
            DeviceEndpoint endpoint = getDeviceEndpoint(workspaceBlock);
            Lock lock = workspaceBlock.getLockManager().getLock(workspaceBlock);

            endpoint.addChangeListener(workspaceBlock.getId(), state -> lock.signalAll());
            workspaceBlock.onRelease(() -> endpoint.removeChangeListener(workspaceBlock.getId()));
            workspaceBlock.subscribeToLock(lock, next::handle);
        });
    }

    private @NotNull State getDeviceEndpointState(@NotNull WorkspaceBlock workspaceBlock) {
        return getDeviceEndpoint(workspaceBlock).getLastValue();
    }

    private @NotNull DeviceEndpoint getDeviceEndpoint(@NotNull WorkspaceBlock workspaceBlock) {
        return getDeviceEndpoint(workspaceBlock, deviceMenu, endpointMenu);
    }

    private @NotNull DeviceEndpoint getDeviceEndpoint(
        @NotNull WorkspaceBlock workspaceBlock,
        @NotNull ServerMenuBlock deviceMenu,
        @NotNull String endpointID) {
        String ieeeAddress = workspaceBlock.getMenuValue(DEVICE, deviceMenu);
        DeviceEndpoint endpoint = getDeviceEndpoint(ieeeAddress, endpointID);

        if (endpoint == null) {
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
        throw new NotImplementedException();
    }

    private @Nullable DeviceEndpoint getDeviceEndpoint(@NotNull String ieeeAddress, @NotNull String endpointID) {
        for (DeviceBaseEntity deviceBaseEntity : entityContext.findAll(DeviceBaseEntity.class)) {
            if (deviceBaseEntity instanceof DeviceEndpointsBehaviourContract deviceEntity) {
                if (ieeeAddress.equals(deviceEntity.getIeeeAddress())) {
                    return deviceEntity.getDeviceEndpoint(endpointID);
                }
            }
        }
        return null;
    }

    private enum OnOff {
        on, off
    }
}
