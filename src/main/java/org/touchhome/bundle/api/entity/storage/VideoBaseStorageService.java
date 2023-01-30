package org.touchhome.bundle.api.entity.storage;

import org.touchhome.bundle.api.entity.DeviceBaseEntity;
import org.touchhome.bundle.api.entity.types.StorageEntity;

public abstract class VideoBaseStorageService<T extends VideoBaseStorageService> extends StorageEntity<T> {
    public abstract void startRecord(String id, String output, String profile, DeviceBaseEntity videoEntity);

    public abstract void stopRecord(String id, String output, DeviceBaseEntity videoEntity);
}
