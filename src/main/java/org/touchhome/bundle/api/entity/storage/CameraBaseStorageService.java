package org.touchhome.bundle.api.entity.storage;

import org.touchhome.bundle.api.entity.DeviceBaseEntity;

public abstract class CameraBaseStorageService<T extends CameraBaseStorageService> extends StorageEntity<T> {
    public abstract void startRecord(String id, String output, String profile, DeviceBaseEntity cameraEntity);

    public abstract void stopRecord(String id, String output, DeviceBaseEntity cameraEntity);
}
