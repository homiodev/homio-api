package org.touchhome.bundle.api.entity.workspace;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.touchhome.bundle.api.entity.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@Getter
@Accessors(chain = true)
public final class WorkspaceStandaloneVariableEntity extends BaseEntity<WorkspaceStandaloneVariableEntity> {

    public static final String PREFIX = "wssv_";

    @Setter
    @Column(nullable = false)
    private float value;

    @Override
    public String getTitle() {
        return "Variable: " + getName();
    }

    @Override
    public void merge(WorkspaceStandaloneVariableEntity entity) {
        super.merge(entity);
        this.value = entity.getValue();
    }

    @Override
    public String getEntityPrefix() {
        return PREFIX;
    }
}
