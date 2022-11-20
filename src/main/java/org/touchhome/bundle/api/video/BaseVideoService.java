package org.touchhome.bundle.api.video;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.BadCredentialsException;
import org.touchhome.bundle.api.model.Status;
import org.touchhome.bundle.api.netty.HasBootstrapServer;
import org.touchhome.bundle.api.netty.NettyUtils;
import org.touchhome.bundle.api.service.EntityService;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.util.function.Supplier;

public abstract class BaseVideoService<H extends BaseFFMPEGVideoStreamHandler, T extends BaseFFMPEGVideoStreamEntity>
        implements EntityService.ServiceInstance {

    @Getter
    private H videoHandler;
    @Getter
    @Setter
    private T entity;

    protected abstract HasBootstrapServer createVideoHandler();

    public void fetchVideoHandler() {
        this.videoHandler = (H) NettyUtils.putBootstrapServer(entity.getEntityID(),
                (Supplier<HasBootstrapServer>) () -> createVideoHandler());
        if (entity.getStatus() == Status.UNKNOWN) {
            try {
                getVideoHandler().testOnline();
                entity.setStatusOnline();
            } catch (BadCredentialsException ex) {
                entity.setStatus(Status.REQUIRE_AUTH, ex.getMessage());
            } catch (Exception ex) {
                entity.setStatusError(ex);
            }
        }
    }

    @Override
    public void entityUpdated(EntityService entityService) {
        boolean requireRestart = false;
        T entity = (T) entityService;

        if (entity.isStart() &&
                (!videoHandler.isHandlerInitialized() || TouchHomeUtils.isRequireRestartHandler(entity, this.entity))) {
            requireRestart = true;
            // this.videoHandler.initialize();
        } else if (!this.entity.isStart() && this.videoHandler.isHandlerInitialized()) {
            this.videoHandler.disposeAndSetStatus(Status.OFFLINE, "Camera not started");
        }
        this.entity = entity;
        this.videoHandler.entityUpdated(this.entity);

        if (requireRestart) {
            this.videoHandler.restart("Entity updated", true);
        }
    }

    @Override
    public void destroy() {
        BaseFFMPEGVideoStreamHandler handler = NettyUtils.removeBootstrapServer(entity.getEntityID());
        if (handler != null) {
            handler.dispose();
            handler.deleteDirectories();
        }
    }
}
