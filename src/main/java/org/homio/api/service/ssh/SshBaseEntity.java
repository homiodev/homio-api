package org.homio.api.service.ssh;

import org.homio.api.entity.device.DeviceBaseEntity;
import org.homio.api.service.EntityService;
import org.homio.api.ui.route.UIRouteIdentity;

/**
 * Base class for all ssh entities to allow to connect to it
 *
 * @param <T> - actual entity
 * @param <S> - service
 */
@SuppressWarnings({"rawtypes"})
@UIRouteIdentity
public abstract class SshBaseEntity<
        T extends SshBaseEntity, S extends EntityService.ServiceInstance & SshProviderService<T>>
        extends DeviceBaseEntity implements EntityService<S> {
}
