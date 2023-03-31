package org.homio.bundle.api.video;

import java.util.List;
import org.homio.bundle.api.entity.HasJsonData;
import org.homio.bundle.api.entity.RestartHandlerOnChange;
import org.homio.bundle.api.ui.field.UIField;
import org.homio.bundle.api.ui.field.UIFieldGroup;
import org.homio.bundle.api.ui.field.UIFieldType;

public interface AbilityToStreamHLSOverFFMPEG<T> extends HasJsonData {
    @UIField(order = 1000, hideInView = true, type = UIFieldType.Chips)
    @UIFieldGroup("hls_group")
    @RestartHandlerOnChange
    default List<String> getExtraOptions() {
        return getJsonDataList("extraOpts");
    }

    default void setExtraOptions(String value) {
        setJsonData("extraOpts", value);
    }

    @UIField(order = 320, hideInView = true)
    @UIFieldGroup("hls_group")
    @RestartHandlerOnChange
    default int getHlsListSize() {
        return getJsonData("hlsListSize", 5);
    }

    default T setHlsListSize(int value) {
        setJsonData("hlsListSize", value);
        return (T) this;
    }

    @UIField(order = 400, hideInView = true)
    @RestartHandlerOnChange
    @UIFieldGroup("hls_group")
    default String getVideoCodec() {
        return getJsonData("vcodec", "copy");
    }

    default T setVideoCodec(String value) {
        setJsonData("vcodec", value);
        return (T) this;
    }

    @UIField(order = 410, hideInView = true)
    @RestartHandlerOnChange
    @UIFieldGroup("hls_group")
    default String getAudioCodec() {
        return getJsonData("acodec", "aac");
    }

    default T setAudioCodec(String value) {
        setJsonData("acodec", value);
        return (T) this;
    }

    @UIField(order = 320, hideInView = true)
    @RestartHandlerOnChange
    @UIFieldGroup("hls_group")
    default String getHlsScale() {
        return getJsonData("hls_scale");
    }

    default T setHlsScale(String value) {
        setJsonData("hls_scale", value);
        return (T) this;
    }
}
