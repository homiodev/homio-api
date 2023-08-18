package org.homio.api.audio;

import org.homio.api.model.OptionModel;

import java.util.Collection;

public interface SelfContainedAudioSourceContainer {
    Collection<OptionModel> getAudioSource();

    String getLabel();

    void play(String entityID) throws Exception;
}
