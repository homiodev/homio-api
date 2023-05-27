package org.homio.api.audio;

import java.util.Collection;
import org.homio.api.model.OptionModel;

public interface SelfContainedAudioSourceContainer {
    Collection<OptionModel> getAudioSource();

    String getLabel();

    void play(String entityID) throws Exception;
}
