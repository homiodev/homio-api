package org.touchhome.bundle.api.entity.workspace.var;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.entity.widget.HasDisplaySeries;
import org.touchhome.bundle.api.entity.widget.HasGaugeSeries;
import org.touchhome.bundle.api.entity.widget.HasSliderSeries;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Accessors(chain = true)
public final class WorkspaceVariableEntity extends BaseEntity<WorkspaceVariableEntity> implements HasSliderSeries,
        HasDisplaySeries, HasGaugeSeries {

    public static final String PREFIX = "wsv_";

    @Setter
    @Column(nullable = false)
    private float value;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    private WorkspaceVariableGroupEntity workspaceVariableGroupEntity;

    @Setter
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "workspaceVariableEntity", cascade = CascadeType.REMOVE)
    private Set<WorkspaceVariableBackupValueCrudEntity> values;

    @Override
    public String getTitle() {
        return "Var: " + getName() + " / group [" + workspaceVariableGroupEntity.getName() + "]";
    }

    @Override
    public void merge(WorkspaceVariableEntity entity) {
        super.merge(entity);
        this.value = entity.getValue();
    }

    @Override
    public String getEntityPrefix() {
        return PREFIX;
    }

    @Override
    public float getSliderValue() {
        return getValue();
    }

    @Override
    public void setSliderValue(float value) {
        setValue(value);
    }

    @Override
    public Object getDisplayValue() {
        return getValue();
    }

    @Override
    public float getGaugeValue() {
        return getValue();
    }
}
