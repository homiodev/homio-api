package org.touchhome.bundle.api.entity.workspace.var;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.entity.widget.AggregationType;
import org.touchhome.bundle.api.entity.widget.ChartRequest;
import org.touchhome.bundle.api.entity.widget.HasAggregateValueFromSeries;
import org.touchhome.bundle.api.entity.widget.HasSliderSeries;

import javax.persistence.*;
import java.util.Set;
import java.util.function.Consumer;

@Entity
@Getter
@Accessors(chain = true)
public final class WorkspaceVariableEntity extends BaseEntity<WorkspaceVariableEntity> implements HasSliderSeries,
        HasAggregateValueFromSeries {

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
    public float getSliderValue(EntityContext entityContext, JSONObject dynamicParameters) {
        return getValue();
    }

    @Override
    public void setSliderValue(float value, EntityContext entityContext, JSONObject dynamicParameters) {
        setValue(value);
        entityContext.save(this);
    }

    @Override
    public Float getAggregateValueFromSeries(ChartRequest request, AggregationType aggregationType) {
        return value;
    }

    @Override
    public void addUpdateValueListener(EntityContext entityContext, String key, JSONObject dynamicParameters,
                                       Consumer<Object> listener) {
        entityContext.event().addEventListener(getEntityID(), key, listener);
    }

    @Override
    public void afterUpdate(EntityContext entityContext) {
        entityContext.event().fireEvent(getEntityID(), value);
    }
}
