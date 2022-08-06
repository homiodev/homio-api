package org.touchhome.bundle.api.entity.workspace.var;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.entity.widget.AggregationType;
import org.touchhome.bundle.api.entity.widget.ChartRequest;
import org.touchhome.bundle.api.entity.widget.ability.HasAggregateValueFromSeries;
import org.touchhome.bundle.api.entity.widget.ability.HasGetStatusValue;
import org.touchhome.bundle.api.entity.widget.ability.HasSetStatusValue;
import org.touchhome.bundle.api.ui.field.selection.UIFieldSelectionParent;

import javax.persistence.*;
import java.util.Set;
import java.util.function.Consumer;

@Entity
@Getter
@Accessors(chain = true)
@UIFieldSelectionParent(value = "selection.variable", icon = "fas fa-memory", iconColor = "#CCA61F")
public final class WorkspaceVariableEntity extends BaseEntity<WorkspaceVariableEntity> implements
        HasAggregateValueFromSeries, HasGetStatusValue, HasSetStatusValue {

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
    public Float getAggregateValueFromSeries(ChartRequest request, AggregationType aggregationType, boolean exactNumber) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public void setStatusValue(SetStatusValueRequest request) {
        this.value = request.floatValue(0F);
        request.getEntityContext().save(this);
    }

    @Override
    public Object getStatusValue(GetStatusValueRequest request) {
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

    @Override
    public String getGetStatusDescription() {
        return "Get last value";
    }

    @Override
    public String getAggregateValueDescription() {
        return "Variable series-aggregate value";
    }

    @Override
    public String getSetStatusDescription() {
        return "Set value";
    }
}
