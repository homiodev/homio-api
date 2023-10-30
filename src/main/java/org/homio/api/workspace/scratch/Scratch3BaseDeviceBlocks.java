package org.homio.api.workspace.scratch;

import static java.lang.String.format;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.AddonEntrypoint;
import org.homio.api.Context;
import org.homio.api.ContextBGP.ThreadContext;
import org.homio.api.entity.device.DeviceBaseEntity;
import org.homio.api.entity.device.DeviceEndpointsBehaviourContract;
import org.homio.api.exception.NotFoundException;
import org.homio.api.model.Status;
import org.homio.api.model.endpoint.DeviceEndpoint;
import org.homio.api.state.DecimalType;
import org.homio.api.state.OnOffType;
import org.homio.api.state.State;
import org.homio.api.ui.UI;
import org.homio.api.workspace.Lock;
import org.homio.api.workspace.WorkspaceBlock;
import org.homio.api.workspace.scratch.MenuBlock.ServerMenuBlock;
import org.homio.api.workspace.scratch.MenuBlock.StaticMenuBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base parent for all devices that exposes some endpoints
 */
@Getter
public abstract class Scratch3BaseDeviceBlocks extends Scratch3ExtensionBlocks {

    protected static final String DEVICE = "DEVICE";
    protected static final String ENDPOINT = "ENDPOINT";
    protected static final String DEVICE__BASE_URL = "rest/device";

    protected final @NotNull ServerMenuBlock deviceMenu;
    protected final @NotNull ServerMenuBlock deviceReadMenu;
    protected final @NotNull ServerMenuBlock deviceWriteMenu;
    protected final @NotNull ServerMenuBlock endpointMenu;
    protected final @NotNull ServerMenuBlock readEndpointMenu;
    protected final @NotNull ServerMenuBlock writeEndpointMenu;
    protected final @NotNull StaticMenuBlock<OnOff> onOffMenu;
    protected final @NotNull ServerMenuBlock deviceWriteBoolMenu;
    protected final @NotNull ServerMenuBlock writeBoolEndpointMenu;
    protected final String devicePrefix;

    public Scratch3BaseDeviceBlocks(
        @Nullable String color,
        @NotNull Context context,
        @Nullable AddonEntrypoint addonEntrypoint,
        @NotNull String devicePrefix) {
        super(color, context, addonEntrypoint);
        setParent(ScratchParent.devices);
        this.devicePrefix = devicePrefix;

        this.onOffMenu = menuStatic("onOffMenu", OnOff.class, OnOff.off);
        this.deviceMenu = menuServer("deviceMenu",
            "%s?prefix=%s".formatted(DEVICE__BASE_URL, devicePrefix),
            "Device", "-");
        this.deviceReadMenu = menuServer("deviceReadMenu",
            "%s?prefix=%s&access=read".formatted(DEVICE__BASE_URL, devicePrefix),
            "Device", "-");
        this.deviceWriteBoolMenu = menuServer("deviceWriteBoolMenu",
            "%s?prefix=%s&access=write&type=bool".formatted(DEVICE__BASE_URL, devicePrefix),
            "Device",
            "-");
        this.deviceWriteMenu = menuServer("deviceWriteMenu",
            "%s?prefix=%s&access=write".formatted(DEVICE__BASE_URL, devicePrefix),
            "Device", "-");
        this.endpointMenu = menuServer("endpointMenu",
            "%s/endpoints".formatted(DEVICE__BASE_URL),
            "Endpoint", "-")
            .setDependency(this.deviceMenu);
        this.readEndpointMenu = menuServer("readEndpointMenu",
            "%s/endpoints?access=read".formatted(DEVICE__BASE_URL),
            "Endpoint", "-")
            .setDependency(this.deviceReadMenu);
        this.writeEndpointMenu = menuServer("writeEndpointMenu",
            "%s/endpoints?access=write".formatted(DEVICE__BASE_URL),
            "Endpoint", "-")
            .setDependency(this.deviceWriteMenu);
        this.writeBoolEndpointMenu = menuServer("writeBoolEndpointMenu",
            "%s/endpoints?access=write&type=bool".formatted(DEVICE__BASE_URL),
            "Endpoint", "-")
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
                block.overrideColor(UI.Color.darker(getScratch3Color().getColor1(), 0.8f));
            });

        blockReporter(51, "value", "[ENDPOINT] of [DEVICE]", this::getDeviceEndpointState, block -> {
            block.addArgument(ENDPOINT, this.endpointMenu);
            block.addArgument(DEVICE, this.deviceMenu);
            block.overrideColor(UI.Color.darker(getScratch3Color().getColor1(), 0.8f));
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

    protected @NotNull DeviceEndpoint getDeviceEndpoint(
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

    protected @NotNull State getDeviceEndpointState(@NotNull WorkspaceBlock workspaceBlock) {
        return getDeviceEndpoint(workspaceBlock).getLastValue();
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

    protected @Nullable DeviceEndpoint getDeviceEndpoint(@NotNull String ieeeAddress, @NotNull String endpointID) {
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
        return entities.get(0);
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
            ThreadContext<Void> delayThread = context.bgp().builder("when-no-val-" + workspaceBlock.getId())
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

    public enum OnOff {
        on, off
    }
}
