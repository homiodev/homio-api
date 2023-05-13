package org.homio.bundle.api.service;

import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.entity.HasJsonData;
import org.homio.bundle.api.entity.HasStatusAndMsg;
import org.homio.bundle.api.model.HasEntityIdentifier;
import org.homio.bundle.api.model.Status;
import org.homio.bundle.api.service.CloudProviderService.SshCloud;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CloudProviderService<T extends SshCloud> {

    /**
     * Method calls before start/stop
     *
     * @param sshEntity - current 'primary' entity
     */
    void setCurrentEntity(@NotNull T sshEntity);

    // Method should wait forever until exception
    void start() throws Exception;

    void stop() throws Exception;

    void updateNotificationBlock(@Nullable Exception ex);

    default void updateNotificationBlock() {
        updateNotificationBlock(null);
    }

    interface SshCloud<T extends SshCloud> extends HasEntityIdentifier, HasStatusAndMsg<T>, HasJsonData {

        /**
         * Does this cloud is primary. Only one entity may be primary. Primary entity uses for cloud provider as tunnel
         *
         * @return is entity primary
         */
        boolean isPrimary();

        long getChangesHashCode();

        boolean isRestartOnFailure();

        @Nullable CloudProviderService<T> getCloudProviderService(@NotNull EntityContext entityContext);
    }
}
