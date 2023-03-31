package org.homio.bundle.api.audio;

import java.util.Collection;
import org.homio.bundle.api.model.OptionModel;

public interface SelfContainedAudioSourceContainer {
    Collection<OptionModel> getAudioSource();

    String getLabel();

    void play(String entityID) throws Exception;
}
