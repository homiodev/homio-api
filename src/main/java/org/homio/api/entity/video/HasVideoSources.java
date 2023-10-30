package org.homio.api.entity.video;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import org.homio.api.model.OptionModel;

public interface HasVideoSources {

    @JsonIgnore
    List<OptionModel> getVideoSources();
}
