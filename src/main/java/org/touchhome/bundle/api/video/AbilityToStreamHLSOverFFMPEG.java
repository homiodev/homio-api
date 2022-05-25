package org.touchhome.bundle.api.video;

import org.touchhome.bundle.api.entity.HasJsonData;
import org.touchhome.bundle.api.entity.RestartHandlerOnChange;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.UIFieldGroup;
import org.touchhome.bundle.api.ui.field.UIFieldType;

import java.util.List;

public interface AbilityToStreamHLSOverFFMPEG<T> extends HasJsonData<T> {
    @UIField(order = 1000, onlyEdit = true, advanced = true, type = UIFieldType.Chips)
    @UIFieldGroup("hls_group")
    @RestartHandlerOnChange
    default List<String> getExtraOptions() {
        return getJsonDataList("extraOpts");
    }

    default void setExtraOptions(String value) {
        setJsonData("extraOpts", value);
    }

    @UIField(order = 320, onlyEdit = true, advanced = true)
    @UIFieldGroup("hls_group")
    @RestartHandlerOnChange
    default int getHlsListSize() {
        return getJsonData("hlsListSize", 5);
    }

    default T setHlsListSize(int value) {
        setJsonData("hlsListSize", value);
        return (T) this;
    }

    @UIField(order = 400, onlyEdit = true, advanced = true)
    @RestartHandlerOnChange
    @UIFieldGroup("hls_group")
    default String getVideoCodec() {
        return getJsonData("vcodec", "copy");
    }

    default T setVideoCodec(String value) {
        setJsonData("vcodec", value);
        return (T) this;
    }

    @UIField(order = 410, onlyEdit = true, advanced = true)
    @RestartHandlerOnChange
    @UIFieldGroup("hls_group")
    default String getAudioCodec() {
        return getJsonData("acodec", "aac");
    }

    default T setAudioCodec(String value) {
        setJsonData("acodec", value);
        return (T) this;
    }

    @UIField(order = 320, onlyEdit = true, advanced = true)
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
