package org.touchhome.bundle.api.audio;

import java.util.Collection;
import org.touchhome.bundle.api.model.OptionModel;

public interface SelfContainedAudioSourceContainer {
    Collection<OptionModel> getAudioSource();

    String getLabel();

    void play(String entityID) throws Exception;
}
