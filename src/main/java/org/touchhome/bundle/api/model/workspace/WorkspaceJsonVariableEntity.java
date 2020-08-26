package org.touchhome.bundle.api.model.workspace;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.json.JSONObject;
import org.touchhome.bundle.api.converter.JSONObjectConverter;
import org.touchhome.bundle.api.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
@Getter
@Accessors(chain = true)
public class WorkspaceJsonVariableEntity extends BaseEntity<WorkspaceJsonVariableEntity> {

    public static final String PREFIX = "wjv_";

    @Lob
    @Setter
    @Column(length = 1048576)
    @Convert(converter = JSONObjectConverter.class)
    private JSONObject value;

    @Override
    public String getTitle() {
        return "JSON Variable: " + getName();
    }
}
