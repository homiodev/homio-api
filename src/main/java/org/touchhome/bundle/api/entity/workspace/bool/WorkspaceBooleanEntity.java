package org.touchhome.bundle.api.entity.workspace.bool;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.touchhome.bundle.api.entity.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Accessors(chain = true)
public final class WorkspaceBooleanEntity extends BaseEntity<WorkspaceBooleanEntity> {

    public static final String PREFIX = "wsbo_";

    @Setter
    @Column(nullable = false)
    private Boolean value = Boolean.FALSE;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    private WorkspaceBooleanGroupEntity workspaceBooleanGroupEntity;

    @Override
    public String getTitle() {
        return "Var: " + getName() + " / group [" + workspaceBooleanGroupEntity.getName() + "]";
    }

    @Override
    public void merge(WorkspaceBooleanEntity entity) {
        super.merge(entity);
        this.value = entity.value;
    }

    @Override
    public String getEntityPrefix() {
        return PREFIX;
    }

    public WorkspaceBooleanEntity inverseValue() {
        this.value = !this.value;
        return this;
    }
}
