package org.touchhome.bundle.api.entity.workspace;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.touchhome.bundle.api.entity.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

@Getter
@Setter
@Entity
@Accessors(chain = true)
public class WorkspaceShareVariableEntity extends BaseEntity<WorkspaceShareVariableEntity> {

    public static final String PREFIX = "wssve_";
    public static final String NAME = "share_variables";

    @Lob
    @Column(length = 1048576)
    private String content = "";

    @Override
    public void merge(WorkspaceShareVariableEntity entity) {
        super.merge(entity);
        this.content = entity.getContent();
    }
}
