package org.touchhome.bundle.api.video;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.BadCredentialsException;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.RestartHandlerOnChange;
import org.touchhome.bundle.api.model.ActionResponseModel;
import org.touchhome.bundle.api.model.Status;
import org.touchhome.bundle.api.netty.HasBootstrapServer;
import org.touchhome.bundle.api.netty.NettyUtils;
import org.touchhome.bundle.api.state.State;
import org.touchhome.bundle.api.ui.field.*;
import org.touchhome.bundle.api.ui.field.action.UIActionButton;
import org.touchhome.bundle.api.ui.field.action.UIActionInput;
import org.touchhome.bundle.api.ui.field.action.UIContextMenuAction;
import org.touchhome.bundle.api.ui.field.action.v1.UIInputBuilder;
import org.touchhome.bundle.api.ui.field.image.UIFieldImage;
import org.touchhome.bundle.api.util.SecureString;
import org.touchhome.bundle.api.util.TouchHomeUtils;
import org.touchhome.common.exception.ServerException;

import javax.persistence.Transient;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Log4j2
public abstract class BaseFFMPEGVideoStreamEntity<T extends BaseFFMPEGVideoStreamEntity,
        H extends BaseFFMPEGVideoStreamHandler> extends BaseVideoStreamEntity<T> {

    @Getter
    @Transient
    @JsonIgnore
    private H videoHandler;

    public abstract String getFolderName();

    @UIField(order = 500, readOnly = true, type = UIFieldType.Duration)
    public long getLastAnswerFromVideo() {
        return videoHandler == null ? 0 : videoHandler.getLastAnswerFromVideo();
    }

    @Override
    protected void fireUpdateSnapshot(EntityContext entityContext, JSONObject params) {
        if (!isStart()) {
            throw new ServerException("Video <" + getTitle() + "> not started");
        }
        if (videoHandler != null) {
            videoHandler.startSnapshot();
        }
    }

    @Override
    public String getTitle() {
        return StringUtils.defaultIfBlank(getName(), StringUtils.defaultIfBlank(getDefaultName(), getEntityID()));
    }

    @UIField(order = 15, inlineEdit = true)
    public boolean isStart() {
        return getJsonData("start", false);
    }

    @UIContextMenuAction(value = "VIDEO.RECORD_MP4", icon = "fas fa-file-video", inputs = {
            @UIActionInput(name = "fileName", value = "record_${timestamp}", min = 4, max = 30),
            @UIActionInput(name = "secondsToRecord", type = UIActionInput.Type.number, value = "10", min = 5, max = 100)
    })
    public ActionResponseModel recordMP4(JSONObject params) {
        checkVideoOnline();
        String filename = getFileNameToRecord(params);
        int secondsToRecord = params.getInt("secondsToRecord");
        log.debug("Recording {}.mp4 for {} seconds.", filename, secondsToRecord);
        videoHandler.recordMp4(filename, null, secondsToRecord);
        return ActionResponseModel.showSuccess("SUCCESS");
    }

    @UIContextMenuAction(value = "VIDEO.RECORD_GIF", icon = "fas fa-magic", inputs = {
            @UIActionInput(name = "fileName", value = "record_${timestamp}", min = 4, max = 30),
            @UIActionInput(name = "secondsToRecord", type = UIActionInput.Type.number, value = "3", min = 1, max = 10)
    })
    public ActionResponseModel recordGif(JSONObject params) {
        checkVideoOnline();
        String filename = getFileNameToRecord(params);
        int secondsToRecord = params.getInt("secondsToRecord");
        log.debug("Recording {}.gif for {} seconds.", filename, secondsToRecord);
        videoHandler.recordGif(filename, null, secondsToRecord);
        return ActionResponseModel.showSuccess("SUCCESS");
    }

    protected void checkVideoOnline() {
        if (videoHandler == null) {
            throw new ServerException("Video handler is empty");
        }
        if (!videoHandler.isVideoOnline()) {
            throw new ServerException("VIDEO.OFFLINE");
        }
    }

    private String getFileNameToRecord(JSONObject params) {
        String fileName = params.getString("fileName");
        // hacky
        fileName = fileName.replace("${timestamp}", System.currentTimeMillis() + "");
        return fileName;
    }

    @UIField(order = 200, readOnly = true)
    @UIFieldCodeEditor(editorType = UIFieldCodeEditor.CodeEditorType.json, autoFormat = true)
    public Map<String, State> getAttributes() {
        return videoHandler == null ? null : videoHandler.getAttributes();
    }

    public abstract H createVideoHandler(EntityContext entityContext);

    @Override
    public UIInputBuilder assembleActions() {
        return videoHandler == null ? null : videoHandler.assembleActions();
    }

    public BaseFFMPEGVideoStreamEntity setStart(boolean start) {
        setJsonData("start", start);
        return this;
    }

    @UIField(order = 16, inlineEdit = true)
    public boolean isAutoStart() {
        return getJsonData("autoStart", true);
    }

    public BaseFFMPEGVideoStreamEntity setAutoStart(boolean start) {
        setJsonData("autoStart", start);
        return this;
    }

    @UIField(order = 250, onlyEdit = true, advanced = true)
    @UIFieldNumber(min = 1025, max = 65535)
    @RestartHandlerOnChange
    public Integer getServerPort() {
        return getJsonData("serverPort", 9000);
    }

    public void setServerPort(int value) {
        setJsonData("serverPort", value);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @UIField(order = 500, readOnly = true)
    @UIFieldImage
    @UIActionButton(name = "refresh", icon = "fas fa-sync",
            actionHandler = BaseVideoStreamEntity.UpdateSnapshotActionHandler.class)
    @UIFieldIgnoreGetDefault
    public byte[] getLastSnapshot() {
        return videoHandler == null ? null : videoHandler.getLatestSnapshot();
    }

    // not all entity has user name
    public String getUser() {
        return getJsonData("user", "");
    }

    public T setUser(String value) {
        return setJsonData("user", value);
    }

    // not all entity has password
    public SecureString getPassword() {
        return new SecureString(getJsonData("password", ""));
    }

    public T setPassword(String value) {
        return setJsonData("password", value);
    }

    public String getAlarmInputUrl() {
        return getJsonData("alarmInputUrl", "");
    }

    public void setAlarmInputUrl(String value) {
        setJsonData("alarmInputUrl", value);
    }

    @Override
    @UIFieldIgnore
    public String getIeeeAddress() {
        return super.getIeeeAddress();
    }

    @UIField(order = 125, onlyEdit = true, advanced = true, type = UIFieldType.Chips)
    @RestartHandlerOnChange
    public List<String> getGifOutOptions() {
        return getJsonDataList("gifOutOptions");
    }

    public void setGifOutOptions(String value) {
        setJsonData("gifOutOptions", value);
    }

    @UIField(order = 130, onlyEdit = true, advanced = true, type = UIFieldType.Chips)
    @RestartHandlerOnChange
    public List<String> getMjpegOutOptions() {
        return getJsonDataList("mjpegOutOptions");
    }

    public void setMjpegOutOptions(String value) {
        setJsonData("mjpegOutOptions", value);
    }

    @UIField(order = 135, onlyEdit = true, advanced = true, type = UIFieldType.Chips)
    @RestartHandlerOnChange
    public List<String> getSnapshotOutOptions() {
        return getJsonDataList("imgOutOptions");
    }

    public void setSnapshotOutOptions(String value) {
        setJsonData("imgOutOptions", value);
    }

    @JsonIgnore
    public String getSnapshotOutOptionsAsString() {
        return String.join(" ", getSnapshotOutOptions());
    }

    @UIField(order = 140, onlyEdit = true, advanced = true, type = UIFieldType.Chips)
    @RestartHandlerOnChange
    public List<String> getMotionOptions() {
        return getJsonDataList("motionOptions");
    }

    public void setMotionOptions(String value) {
        setJsonData("motionOptions", value);
    }

    @UIField(order = 110)
    @UIFieldSlider(min = 1, max = 100)
    public int getMotionThreshold() {
        return getJsonData("motionThreshold", 40);
    }

    public void setMotionThreshold(int value) {
        setJsonData("motionThreshold", value);
    }

    @UIField(order = 112)
    @UIFieldSlider(min = 1, max = 100)
    public int getAudioThreshold() {
        return getJsonData("audioThreshold", 40);
    }

    @UIField(order = 200)
    @UIFieldSlider(min = 1, max = 30)
    public int getSnapshotPollInterval() {
        return getJsonData("spi", 5);
    }

    public void setSnapshotPollInterval(int value) {
        setJsonData("spi", value);
    }

    public void setAudioThreshold(int value) {
        setJsonData("audioThreshold", value);
    }

    @UIField(order = 160, onlyEdit = true, advanced = true, type = UIFieldType.Chips)
    @RestartHandlerOnChange
    public List<String> getMp4OutOptions() {
        return getJsonDataList("mp4OutOptions");
    }

    public void setMp4OutOptions(String value) {
        setJsonData("mp4OutOptions", value);
    }

    @Override
    protected void beforePersist() {
        setMp4OutOptions("-c:v copy~~~-c:a copy");
        setMjpegOutOptions("-q:v 5~~~-r 2~~~-vf scale=640:-2~~~-update 1");
        setSnapshotOutOptions("-vsync vfr~~~-q:v 2~~~-update 1~~~-frames:v 1");
        setGifOutOptions(
                "-r 2~~~-filter_complex scale=-2:360:flags=lanczos,setpts=0.5*PTS,split[o1][o2];[o1]palettegen[p];[o2]fifo[o3];" +
                        "[o3][p]paletteuse");
        setServerPort(NettyUtils.findFreeBootstrapServerPort());
    }

    @Override
    protected void beforeUpdate() {
        super.beforeUpdate();
        HasBootstrapServer server = NettyUtils.getServerByPort(getEntityID(), getServerPort());
        if (server != null) {
            throw new RuntimeException(
                    "Unable to save video entity: " + getTitle() + ". Server port: " + getServerPort() + " already in use by: " +
                            server.getName());
        }
    }

    @JsonIgnore
    public String getHlsStreamUrl() {
        return "http://" + TouchHomeUtils.MACHINE_IP_ADDRESS + ":" + getVideoHandler().getServerPort() + "/ipvideo.m3u8";
    }

    @JsonIgnore
    public String getSnapshotsMjpegUrl() {
        return "http://" + TouchHomeUtils.MACHINE_IP_ADDRESS + ":" + getVideoHandler().getServerPort() + "/snapshots.mjpeg";
    }

    @JsonIgnore
    public String getAutofpsMjpegUrl() {
        return "http://" + TouchHomeUtils.MACHINE_IP_ADDRESS + ":" + getVideoHandler().getServerPort() + "/autofps.mjpeg";
    }

    @JsonIgnore
    public String getImageUrl() {
        return "http://" + TouchHomeUtils.MACHINE_IP_ADDRESS + ":" + getVideoHandler().getServerPort() + "/ipvideo.jpg";
    }

    @JsonIgnore
    public String getIpVideoMjpeg() {
        return "http://" + TouchHomeUtils.MACHINE_IP_ADDRESS + ":" + getVideoHandler().getServerPort() + "/ipvideo.mjpeg";
    }

    @Override
    public Collection<Pair<String, String>> getVideoSources() {
        return Arrays.asList(
                Pair.of("autofps.mjpeg", "autofps.mjpeg"),
                Pair.of("snapshots.mjpeg", "snapshots.mjpeg"),
                Pair.of("ipvideo.mjpeg", "ipvideo.mjpeg"),
                Pair.of("HLS", "HLS"));
    }

    @Override
    public String getStreamUrl(String source) {
        switch (source) {
            case "autofps.mjpeg":
                return getAutofpsMjpegUrl();
            case "snapshots.mjpeg":
                return getSnapshotsMjpegUrl();
            case "HLS":
                return getHlsStreamUrl();
            case "ipvideo.mjpeg":
                return getIpVideoMjpeg();
            case "image.jpg":
                return getImageUrl();
        }
        return null;
    }

    @Override
    public void afterFetch(EntityContext entityContext) {
        videoHandler = (H) NettyUtils.putBootstrapServer(getEntityID(),
                (Supplier<HasBootstrapServer>) () -> createVideoHandler(entityContext));

        if (getStatus() == Status.UNKNOWN) {
            try {
                getVideoHandler().testOnline();
                setStatusOnline();
            } catch (BadCredentialsException ex) {
                setStatus(Status.REQUIRE_AUTH, ex.getMessage());
            } catch (Exception ex) {
                setStatusError(ex);
            }
        }
    }
}
