package org.touchhome.bundle.api.audio;

import org.touchhome.bundle.api.model.OptionModel;

import java.util.Collection;

public interface SelfContainedAudioSourceContainer {
    Collection<OptionModel> getAudioSource();

    String getLabel();

    void play(String entityID) throws Exception;
}
