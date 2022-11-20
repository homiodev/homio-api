package org.touchhome.bundle.api.video;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.MimeTypeUtils;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.EntityContextBGP;
import org.touchhome.bundle.api.entity.dependency.DependencyExecutableInstaller;
import org.touchhome.bundle.api.hardware.other.MachineHardwareRepository;
import org.touchhome.bundle.api.model.Status;
import org.touchhome.bundle.api.netty.HasBootstrapServer;
import org.touchhome.bundle.api.state.*;
import org.touchhome.bundle.api.ui.field.action.v1.UIInputBuilder;
import org.touchhome.bundle.api.util.TouchHomeUtils;
import org.touchhome.bundle.api.video.ffmpeg.FFMPEG;
import org.touchhome.bundle.api.video.ffmpeg.FFMPEGFormat;
import org.touchhome.bundle.api.video.ffmpeg.FfmpegInputDeviceHardwareRepository;
import org.touchhome.bundle.api.video.ui.UIVideoAction;
import org.touchhome.bundle.api.video.ui.UIVideoActionGetter;
import org.touchhome.common.util.CommonUtils;
import org.touchhome.common.util.FlowMap;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static org.touchhome.bundle.api.video.VideoConstants.*;

@Log4j2
public abstract class BaseFFMPEGVideoStreamHandler<T extends BaseFFMPEGVideoStreamEntity,
        S extends BaseVideoService<H, T>,
        H extends BaseFFMPEGVideoStreamHandler> implements HasBootstrapServer, VideoActionsContext<T>,
        FFMPEG.FFMPEGHandler {

    @Getter
    private static final String ffmpegLocation =
            SystemUtils.IS_OS_LINUX ? "ffmpeg" :
                    TouchHomeUtils.getInstallPath().resolve("ffmpeg").resolve("ffmpeg.exe").toString();

    @Getter
    protected final EntityContext entityContext;
    @Getter
    protected final int serverPort;
    protected final FfmpegInputDeviceHardwareRepository ffmpegInputDeviceHardwareRepository;
    @Getter
    private final Path ffmpegGifOutputPath;
    @Getter
    private final Path ffmpegMP4OutputPath;
    @Getter
    private final Path ffmpegHLSOutputPath;
    @Getter
    private final Path ffmpegImageOutputPath;
    @Getter
    private final S videoService;
    public ReentrantLock lockCurrentSnapshot = new ReentrantLock();
    public FFMPEG ffmpegHLS;
    @Getter
    protected byte[] latestSnapshot = new byte[0];
    @Getter
    protected String entityID;
    @Getter
    protected Map<String, State> attributes = new ConcurrentHashMap<>();
    @Getter
    protected Map<String, State> requestAttributes = new ConcurrentHashMap<>();
    @Getter
    protected long lastAnswerFromVideo;
    @Getter
    protected boolean motionDetected = false;
    protected FFMPEG ffmpegGIF;
    protected FFMPEG ffmpegSnapshot;
    protected FFMPEG ffmpegMjpeg;
    protected FFMPEG ffmpegMP4 = null;
    @Getter
    private boolean isVideoOnline = false; // Used so only 1 error is logged when a network issue occurs.
    @Getter
    private boolean isHandlerInitialized = false;
    private EntityContextBGP.ThreadContext<Void> videoConnectionJob;
    private EntityContextBGP.ThreadContext<Void> pollVideoJob;
    private Map<String, Consumer<Status>> stateListeners = new HashMap<>();
    // actions holder
    private UIInputBuilder uiInputBuilder;
    private ServerBootstrap serverBootstrap;
    private EventLoopGroup serversLoopGroup = new NioEventLoopGroup();

    private String snapshotSource;
    private String snapshotInputOptions;
    private String mp4OutOptions;
    private String gifOutOptions;
    private String mgpegOutOptions;

    private FFMpegRtspAlarm ffMpegRtspAlarm = new FFMpegRtspAlarm();

    public T getEntity() {
        return videoService.getEntity();
    }

    public BaseFFMPEGVideoStreamHandler(S videoService, EntityContext entityContext) {
        this.videoService = videoService;
        this.entityID = getEntity().getEntityID();

        this.entityContext = entityContext;
        this.ffmpegInputDeviceHardwareRepository = entityContext.getBean(FfmpegInputDeviceHardwareRepository.class);
        this.serverPort = getEntity().getServerPort();

        Path ffmpegOutputPath = TouchHomeUtils.getMediaPath().resolve(getEntity().getFolderName()).resolve(entityID);
        ffmpegImageOutputPath = CommonUtils.createDirectoriesIfNotExists(ffmpegOutputPath.resolve("images"));
        ffmpegGifOutputPath = CommonUtils.createDirectoriesIfNotExists(ffmpegOutputPath.resolve("gif"));
        ffmpegMP4OutputPath = CommonUtils.createDirectoriesIfNotExists(ffmpegOutputPath.resolve("mp4"));
        ffmpegHLSOutputPath = CommonUtils.createDirectoriesIfNotExists(ffmpegOutputPath.resolve("hls"));
        try {
            FileUtils.cleanDirectory(ffmpegHLSOutputPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Unable to clean path: " + ffmpegHLSOutputPath);
        }
        entityContext.event().runOnceOnInternetUp("test-ffmpeg", () -> {
            if (SystemUtils.IS_OS_LINUX) {
                MachineHardwareRepository repository = entityContext.getBean(MachineHardwareRepository.class);
                if (!repository.isSoftwareInstalled("ffmpeg")) {
                    log.info("Installing ffmpeg");
                    repository.installSoftware("ffmpeg", 600);
                }
            } else {
                if (!Files.exists(Paths.get(ffmpegLocation))) {
                    log.info("Installing ffmpeg");
                    DependencyExecutableInstaller.downloadAndExtract(
                            entityContext.getEnv("artifactoryFilesURL") + "/ffmpeg.7z",
                            "ffmpeg.7z", (progress, message) -> log.info("FFMPEG " + message + ". " + progress + "%"),
                            log);
                }
            }
        });
    }

    public abstract void entityUpdated(T entity);

    public final boolean initialize() {
        if (isHandlerInitialized) {
            return true;
        }
        log.info("[{}]: Initialize video: <{}>", entityID, getEntity());
        isHandlerInitialized = true;
        try {
            if (!getEntity().getEntityID().equals(entityID)) {
                throw new RuntimeException(
                        "Unable to init video <" + getEntity() + "> with different id than: " + entityID);
            }

            initialize0();
            videoConnectionJob = entityContext.bgp().builder("poll-video-connection-" + entityID)
                    .interval(Duration.ofSeconds(60)).execute(this::pollingVideoConnection);
            return true;
        } catch (Exception ex) {
            disposeAndSetStatus(Status.ERROR, CommonUtils.getErrorMessage(ex));
        }
        return false;
    }

    // synchronized to work properly with isHandlerInitialized
    public synchronized final void disposeAndSetStatus(Status status, String reason) {
        if (isHandlerInitialized) {
            // need here to avoid infinite loop
            isHandlerInitialized = false;
            // set it before to avoid recursively disposing from listeners
            log.warn("[{}]: Set video <{}> to status <{}>. Msg: <{}>", entityID, getEntity(), status, reason);

            getEntity().setStatus(status, reason);
            if (getEntity().isStart()) {
                getEntity().setStart(false);
                entityContext.save(getEntity());
            }
            this.stateListeners.values().forEach(h -> h.accept(status));
            if (status == Status.ERROR) {
                entityContext.ui().sendErrorMessage("DISPOSE_VIDEO",
                        FlowMap.of("TITLE", getEntity().getTitle(), "REASON", reason));
            }

            // need set to true to handle dispose !!!
            isHandlerInitialized = true;
            dispose();
        }
    }

    public final void dispose() {
        if (isHandlerInitialized) {
            log.info("[{}]: Dispose video: <{}>", entityID, getEntity());
            isHandlerInitialized = false;
            disposeVideoConnectionJob();
            disposePollVideoJob();
            try {
                dispose0();
            } catch (Exception ex) {
                log.error("[{}]: Error while dispose video: <{}>", entityID, getEntity(), ex);
            }
            isVideoOnline = false;
        }
    }

    public final void bringVideoOnline() {
        lastAnswerFromVideo = System.currentTimeMillis();
        if (!isVideoOnline && isHandlerInitialized) {
            isVideoOnline = true;
            updateStatus(Status.ONLINE, null);

            disposeVideoConnectionJob();
            pollVideoJob = entityContext.bgp().builder("poll-video-runnable-" + entityID)
                    .interval(Duration.ofSeconds(8)).execute(this::pollVideoRunnable);
        }
    }

    private void disposeVideoConnectionJob() {
        Optional.ofNullable(videoConnectionJob).ifPresent(EntityContextBGP.ThreadContext::cancel);
    }

    private void disposePollVideoJob() {
        Optional.ofNullable(pollVideoJob).ifPresent(EntityContextBGP.ThreadContext::cancel);
    }

    public final boolean restart(String reason, boolean force) {
        if (force && !this.isHandlerInitialized) {
            return initialize();
        } else if (isVideoOnline) { // if already offline dont try reconnecting in 6 seconds, we want 30sec wait.
            updateStatus(Status.OFFLINE, reason); // will try to reconnect again as video may be rebooting.
            dispose();
            return initialize();
        }
        return false;
    }

    protected final void updateStatus(Status status, String message) {
        getEntity().setStatus(status, message);
    }

    public UIInputBuilder assembleActions() {
        if (this.uiInputBuilder == null) {
            this.uiInputBuilder = entityContext.ui().inputBuilder();
            assembleAdditionalVideoActions(uiInputBuilder);
        }
        return uiInputBuilder;
    }

    protected void assembleAdditionalVideoActions(UIInputBuilder uiInputBuilder) {

    }

    @Override
    public State getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttributeRequest(String key, State state) {
        requestAttributes.put(key, state);
    }

    protected final void fireFfmpeg(FFMPEG ffmpeg, Consumer<FFMPEG> handler) {
        if (ffmpeg != null) {
            handler.accept(ffmpeg);
        }
    }

    public void deleteDirectories() {
        CommonUtils.deleteDirectory(ffmpegGifOutputPath);
        CommonUtils.deleteDirectory(ffmpegMP4OutputPath);
        CommonUtils.deleteDirectory(ffmpegImageOutputPath);
    }

    @Override
    public String getName() {
        return getEntity().getTitle();
    }

    public void addVideoChangeState(String key, Consumer<Status> handler) {
        this.stateListeners.put(key, handler);
    }

    public void removeVideoChangeState(String key) {
        this.stateListeners.remove(key);
    }

    public abstract void testOnline();

    public void startSnapshot() {
        fireFfmpeg(ffmpegSnapshot, FFMPEG::startConverting);
    }

    public void startMJPEGRecord() {
        fireFfmpeg(ffmpegMjpeg, FFMPEG::startConverting);
    }

    public abstract String getFFMPEGInputOptions(@Nullable String profile);

    public String getFFMPEGInputOptions() {
        return getFFMPEGInputOptions(null);
    }

    protected void pollingVideoConnection() {
        startSnapshot();
    }

    protected void pollVideoRunnable() {
        fireFfmpeg(ffmpegHLS, FFMPEG::stopProcessIfNoKeepAlive);

        long timePassed = System.currentTimeMillis() - lastAnswerFromVideo;
        if (timePassed > 1200000) { // more than 2 min passed
            disposeAndSetStatus(Status.OFFLINE, "Passed more that 2 min without answer from video");
        } else if (timePassed > 30000) {
            startSnapshot();
        }
    }

    protected void initialize0() {
        this.snapshotSource = initSnapshotInput();
        T videoStreamEntity = getEntity();
        this.snapshotInputOptions = getFFMPEGInputOptions() + " -threads 1 -skip_frame nokey -hide_banner -loglevel warning -an";
        this.mp4OutOptions = String.join(" ", videoStreamEntity.getMp4OutOptions());
        this.gifOutOptions = String.join(" ", videoStreamEntity.getGifOutOptions());
        this.mgpegOutOptions = String.join(" ", videoStreamEntity.getMjpegOutOptions());

        String rtspUri = getRtspUri(null);

        ffmpegMjpeg = new FFMPEG("FFMPEG_Mjpeg_" + entityID, "FFMPEG mjpeg", this, log,
                FFMPEGFormat.MJPEG, ffmpegLocation,
                getFFMPEGInputOptions() + " -hide_banner -loglevel warning", rtspUri,
                mgpegOutOptions, "http://127.0.0.1:" + serverPort + "/ipvideo.jpg",
                videoStreamEntity.getUser(), videoStreamEntity.getPassword().asString(), null);
        setAttribute("FFMPEG_MJPEG", new StringType(String.join(" ", ffmpegMjpeg.getCommandArrayList())));

        ffmpegSnapshot = new FFMPEG("FFMPEG_Snapshot_" + entityID, "FFMPEG snapshot", this, log,
                FFMPEGFormat.SNAPSHOT, ffmpegLocation, snapshotInputOptions, rtspUri,
                videoStreamEntity.getSnapshotOutOptionsAsString(),
                "http://127.0.0.1:" + serverPort + "/snapshot.jpg",
                videoStreamEntity.getUser(), videoStreamEntity.getPassword().asString(), () -> {
        });
        setAttribute("FFMPEG_SNAPSHOT", new StringType(String.join(" ", ffmpegSnapshot.getCommandArrayList())));

        if (videoStreamEntity instanceof AbilityToStreamHLSOverFFMPEG) {
            ffmpegHLS = new FFMPEG("FFMPEG_HLS_" + entityID, "FFMPEG HLS", this, log, FFMPEGFormat.HLS, ffmpegLocation,
                    "-hide_banner -loglevel warning " + getFFMPEGInputOptions(), createHlsRtspUri(),
                    buildHlsOptions(), getFfmpegHLSOutputPath().resolve("ipvideo.m3u8").toString(),
                    videoStreamEntity.getUser(), videoStreamEntity.getPassword().asString(),
                    () -> setAttribute(CHANNEL_START_STREAM, OnOffType.OFF));
            setAttribute("FFMPEG_HLS", new StringType(String.join(" ", ffmpegHLS.getCommandArrayList())));
        }

        startStreamServer();
    }

    protected String createHlsRtspUri() {
        return getRtspUri(null);
    }

    public abstract String getRtspUri(String profile);

    protected void dispose0() {
        log.info("[{}]: Dispose video: <{}>", getEntityID(), getEntity());

        fireFfmpeg(ffmpegHLS, FFMPEG::stopConverting);
        fireFfmpeg(ffmpegMP4, FFMPEG::stopConverting);
        fireFfmpeg(ffmpegGIF, FFMPEG::stopConverting);
        fireFfmpeg(ffmpegMjpeg, FFMPEG::stopConverting);
        fireFfmpeg(ffmpegSnapshot, FFMPEG::stopConverting);
        ffMpegRtspAlarm.stop();
        stopStreamServer();
    }

   /* @UIVideoActionGetter(CHANNEL_START_STREAM)
    public OnOffType getHKSStreamState() {
        return OnOffType.of(this.ffmpegHLSStarted);
    }*/

    //@UIVideoAction(name = CHANNEL_START_STREAM, icon = "fas fa-expand-arrows-alt")
    public void startStream(boolean on) {
        FFMPEG localHLS;
        // this.ffmpegHLSStarted = on;
        if (on) {
            localHLS = ffmpegHLS;
            fireFfmpeg(localHLS, ffmpeg -> {
                ffmpeg.setKeepAlive(-1);// Now will run till manually stopped.
                if (ffmpeg.startConverting()) {
                    setAttribute(CHANNEL_START_STREAM, OnOffType.ON);
                }
            });
        } else {
            // Still runs but will be able to auto stop when the HLS stream is no longer used.
            fireFfmpeg(ffmpegHLS, ffmpeg -> ffmpeg.setKeepAlive(1));
        }
    }

    public final void recordMp4(Path filePath, @Nullable String profile, int secondsToRecord) {
        String inputOptions = getFFMPEGInputOptions(profile);
        inputOptions = "-y -t " + secondsToRecord + " -hide_banner -loglevel warning " + inputOptions;
        ffmpegMP4 =
                new FFMPEG("FFMPEGRecordMP4", "FFMPEG record MP4", this, log, FFMPEGFormat.RECORD, ffmpegLocation, inputOptions,
                        getRtspUri(profile),
                        mp4OutOptions, filePath.toString(),
                        getEntity().getUser(), getEntity().getPassword().asString(), null);
        fireFfmpeg(ffmpegMP4, FFMPEG::startConverting);
    }

    public final void recordGif(Path filePath, @Nullable String profile, int secondsToRecord) {
        String gifInputOptions = "-y -t " + secondsToRecord + " -hide_banner -loglevel warning " + getFFMPEGInputOptions();
        ffmpegGIF = new FFMPEG("FFMPEG_GIF)" + entityID, "FFMPEG GIF", this, log, FFMPEGFormat.GIF, ffmpegLocation,
                gifInputOptions, getRtspUri(profile),
                gifOutOptions, filePath.toString(), this.getEntity().getUser(),
                this.getEntity().getPassword().asString(), null);
        fireFfmpeg(ffmpegGIF, FFMPEG::startConverting);
    }

    public void setAttribute(String key, State state) {
        attributes.put(key, state);
        entityContext.event().fireEventIfNotSame(key + ":" + entityID, state);

        if (key.equals(CHANNEL_AUDIO_THRESHOLD)) {
            entityContext.updateDelayed(getEntity(), e -> e.setAudioThreshold(state.intValue()));
        } else if (key.equals(CHANNEL_MOTION_THRESHOLD)) {
            entityContext.updateDelayed(getEntity(), e -> e.setMotionThreshold(state.intValue()));
        }
    }

    @Override
    public void motionDetected(boolean on, String key) {
        if (on) {
            setAttribute(CHANNEL_LAST_MOTION_TYPE, new StringType(key));
        }
        setAttribute(key, OnOffType.of(on));
        setAttribute(MOTION_ALARM, OnOffType.of(on));
        motionDetected = on;
    }

    @Override
    public void audioDetected(boolean on) {
        setAttribute(CHANNEL_AUDIO_ALARM, OnOffType.of(on));
    }

    public void processSnapshot(byte[] incomingSnapshot) {
        log.debug("[{}]: Gеt video snapshot: <{}>", getEntityID(),getEntity());
        lockCurrentSnapshot.lock();
        try {
            latestSnapshot = incomingSnapshot;
            // fires ui that snapshot was updated
            entityContext.ui().updateItem(getEntity());
        } finally {
            lockCurrentSnapshot.unlock();
        }
    }

    @SneakyThrows
    private void stopStreamServer() {
        serversLoopGroup.shutdownGracefully().sync();
        serverBootstrap = null;
    }

    public final void startStreamServer() {
        if (serverBootstrap == null) {
            try {
                serversLoopGroup = new NioEventLoopGroup();
                serverBootstrap = new ServerBootstrap();
                serverBootstrap.group(serversLoopGroup);
                serverBootstrap.channel(NioServerSocketChannel.class);
                // IP "0.0.0.0" will bind the server to all network connections//
                serverBootstrap.localAddress(new InetSocketAddress("0.0.0.0", serverPort));
                serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline().addLast("idleStateHandler",
                                new IdleStateHandler(0, 60, 0));
                        socketChannel.pipeline().addLast("HttpServerCodec", new HttpServerCodec());
                        socketChannel.pipeline().addLast("ChunkedWriteHandler", new ChunkedWriteHandler());
                        socketChannel.pipeline().addLast("streamServerHandler",
                                BaseFFMPEGVideoStreamHandler.this.createVideoStreamServerHandler());
                    }
                });
                ChannelFuture serverFuture = serverBootstrap.bind().sync();
                serverFuture.await(4000);
                log.info("[{}]: File server for video at {} has started on port {} for all NIC's.", getEntityID(), getEntity(), serverPort);
            } catch (Exception e) {
                disposeAndSetStatus(Status.ERROR,
                        "Exception when starting server. Try changing the Server Port to another number.");
            }
            this.streamServerStarted();
        }
    }

    protected abstract BaseVideoStreamServerHandler createVideoStreamServerHandler();

    protected abstract void streamServerStarted();

    public void ffmpegError(String error) {
        this.updateStatus(Status.ERROR, error);
    }

    @UIVideoActionGetter(CHANNEL_AUDIO_THRESHOLD)
    public DecimalType getAudioAlarmThreshold() {
        return new DecimalType(getEntity().getAudioThreshold());
    }

    protected void setAudioAlarmThreshold(int threshold) {
        setAttribute(CHANNEL_AUDIO_THRESHOLD, new StringType(threshold));
        if (threshold == 0) {
            audioDetected(false);
        }
    }

    @UIVideoAction(name = CHANNEL_AUDIO_THRESHOLD, order = 120, icon = "fas fa-volume-up", type = UIVideoAction.ActionType.Dimmer)
    public void setAudioThreshold(int threshold) {
        entityContext.updateDelayed(getEntity(), e -> e.setAudioThreshold(threshold));
        setAudioAlarmThreshold(threshold);
    }

    @UIVideoActionGetter(CHANNEL_MOTION_THRESHOLD)
    public DecimalType getMotionThreshold() {
        return new DecimalType(getEntity().getMotionThreshold());
    }

    @UIVideoAction(name = CHANNEL_MOTION_THRESHOLD, order = 110, icon = "fas fa-expand-arrows-alt",
            type = UIVideoAction.ActionType.Dimmer, max = 1000)
    public void setMotionThreshold(int threshold) {
        entityContext.updateDelayed(getEntity(), e -> e.setMotionThreshold(threshold));
        setMotionAlarmThreshold(threshold);
    }

    protected void setMotionAlarmThreshold(int threshold) {
        setAttribute(CHANNEL_MOTION_THRESHOLD, new StringType(threshold));
        if (threshold == 0) {
            motionDetected(false, CHANNEL_FFMPEG_MOTION_ALARM);
        }
    }

    protected boolean isAudioAlarmHandlesByVideo() {
        return false;
    }

    protected boolean isMotionAlarmHandlesByVideo() {
        return false;
    }

    public void startOrAddMotionAlarmListener(String listener) {
        if (!isMotionAlarmHandlesByVideo()) {
            ffMpegRtspAlarm.addMotionAlarmListener(listener);
        }
    }

    public void removeMotionAlarmListener(String listener) {
        if (!isMotionAlarmHandlesByVideo()) {
            ffMpegRtspAlarm.removeMotionAlarmListener(listener);
        }
    }

    @SneakyThrows
    public byte[] recordGifSync(String profile, int secondsToRecord) {
        String output = getFfmpegGifOutputPath().resolve("tmp_" + System.currentTimeMillis() + ".gif").toString();
        return fireFfmpegSync(profile, output, "-y -t " + secondsToRecord + " -hide_banner -loglevel warning",
                gifOutOptions, secondsToRecord + 20);
    }

    public byte[] recordMp4Sync(String profile, int secondsToRecord) {
        String output = getFfmpegMP4OutputPath().resolve("tmp_" + System.currentTimeMillis() + ".mp4").toString();
        return fireFfmpegSync(profile, output, "-y -t " + secondsToRecord + " -hide_banner -loglevel warning",
                mp4OutOptions, secondsToRecord + 20);
    }

    public RawType recordImageSync(String profile) {
        String output = getFfmpegImageOutputPath().resolve("tmp_" + System.currentTimeMillis() + ".jpg").toString();
        byte[] imageBytes =
                fireFfmpegSync(profile, output, snapshotInputOptions, getEntity().getSnapshotOutOptionsAsString(), 20);
        latestSnapshot = imageBytes;
        return new RawType(imageBytes, MimeTypeUtils.IMAGE_JPEG_VALUE);
    }

    @SneakyThrows
    private byte[] fireFfmpegSync(String profile, String output, String inputArguments, String outOptions, int maxTimeout) {
        try {
            Files.createFile(Paths.get(output));
            entityContext.getBean(FfmpegInputDeviceHardwareRepository.class).fireFfmpeg(
                    ffmpegLocation,
                    inputArguments + " " + getFFMPEGInputOptions(profile),
                    snapshotSource,
                    outOptions + " " + output,
                    maxTimeout);
            Path path = Paths.get(output);
            return IOUtils.toByteArray(Files.newInputStream(path));
        } finally {
            try {
                Files.delete(Paths.get(output));
            } catch (IOException ex) {
                log.error("[{}]: Unable to remove file: <{}>", getEntityID(), output, ex);
            }
        }
    }

    private String initSnapshotInput() {
        String rtspUri = getRtspUri(null);
        if (!getEntity().getPassword().isEmpty() && !rtspUri.contains("@") && rtspUri.contains("rtsp")) {
            String credentials = getEntity().getUser() + ":" + getEntity().getPassword().asString() + "@";
            return rtspUri.substring(0, 7) + credentials + rtspUri.substring(7);
        }
        return rtspUri;
    }

    private String buildHlsOptions() {
        AbilityToStreamHLSOverFFMPEG hlsOptions = (AbilityToStreamHLSOverFFMPEG) getEntity();
        List<String> options = new ArrayList<>();
        options.add("-strict -2");
        options.add("-c:v " + hlsOptions.getVideoCodec()); // video codec
        options.add("-hls_flags delete_segments"); // remove old segments
        options.add("-hls_init_time 1"); // build first ts ASAP
        options.add("-hls_time 2"); // ~ 2sec per file ?
        options.add("-hls_list_size " + hlsOptions.getHlsListSize()); // how many files
        if (StringUtils.isNotEmpty(hlsOptions.getHlsScale())) {
            options.add("-vf scale=" + hlsOptions.getHlsScale()); // scale result video
        }
        if (hasAudioStream()) {
            options.add("-c:a " + hlsOptions.getAudioCodec());
            options.add("-ac 2"); // audio channels (stereo)
            options.add("-ab 32k"); // audio bitrate in Kb/s
            options.add("-ar 44100"); // audio sampling rate
        }
        options.addAll(hlsOptions.getExtraOptions());
        return String.join(" ", options);
    }

    protected boolean hasAudioStream() {
        return getEntity().isHasAudioStream();
    }

    protected boolean isRunning(FFMPEG ffmpeg) {
        return ffmpeg != null && ffmpeg.getIsAlive();
    }

    private class FFMpegRtspAlarm {
        private FFMPEG ffmpegRtspHelper = null;
        private Set<String> motionAlarmObservers = new HashSet<>();

        private int motionThreshold;
        private int audioThreshold;

        public void addMotionAlarmListener(String listener) {
            motionAlarmObservers.add(listener);
            runFFMPEGRtspAlarmThread();
        }

        public void removeMotionAlarmListener(String listener) {
            motionAlarmObservers.remove(listener);
            if (motionAlarmObservers.isEmpty()) {
                stop();
            }
        }

        private boolean runFFMPEGRtspAlarmThread() {
            T videoStreamEntity = BaseFFMPEGVideoStreamHandler.this.getEntity();
            String inputOptions = BaseFFMPEGVideoStreamHandler.this.getFFMPEGInputOptions();

            if (ffmpegRtspHelper != null) {
                // stop stream if threshold - 0
                if (videoStreamEntity.getAudioThreshold() == 0 && videoStreamEntity.getMotionThreshold() == 0) {
                    ffmpegRtspHelper.stopConverting();
                    return false;
                }
                // if values that involved in precious run same as new - just skip restarting
                if (ffmpegRtspHelper.getIsAlive() && motionThreshold == videoStreamEntity.getMotionThreshold() &&
                        audioThreshold == videoStreamEntity.getAudioThreshold()) {
                    return true;
                }
                ffmpegRtspHelper.stopConverting();
            }
            this.motionThreshold = videoStreamEntity.getMotionThreshold();
            this.audioThreshold = videoStreamEntity.getAudioThreshold();
            String input = StringUtils.defaultIfEmpty(BaseFFMPEGVideoStreamHandler.this.getEntity().getAlarmInputUrl(),
                    getRtspUri(null));

            List<String> filterOptionsList = new ArrayList<>();
            filterOptionsList.add(this.audioThreshold > 0 ? "-af silencedetect=n=-" + audioThreshold + "dB:d=2" : "-an");
            if (this.motionThreshold > 0) {
                filterOptionsList.addAll(videoStreamEntity.getMotionOptions());
                filterOptionsList.add("-vf select='gte(scene," + (motionThreshold / 100F) + ")',metadata=print");
            } else {
                filterOptionsList.add("-vn");
            }
            ffmpegRtspHelper = new FFMPEG("FFMPEGRtspAlarm", "FFMPEG rtsp alarm",
                    BaseFFMPEGVideoStreamHandler.this, log, FFMPEGFormat.RTSP_ALARMS, ffmpegLocation, inputOptions, input,
                    String.join(" ", filterOptionsList), "-f null -",
                    BaseFFMPEGVideoStreamHandler.this.getEntity().getUser(),
                    BaseFFMPEGVideoStreamHandler.this.getEntity().getPassword().asString(), null);
            fireFfmpeg(ffmpegRtspHelper, FFMPEG::startConverting);
            setAttribute("FFMPEG_RTSP_ALARM", new StringType(String.join(" ", ffmpegRtspHelper.getCommandArrayList())));
            return true;
        }

        public void stop() {
            fireFfmpeg(ffmpegRtspHelper, FFMPEG::stopConverting);
        }
    }
}
