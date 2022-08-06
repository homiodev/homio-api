package org.touchhome.bundle.api.entity.workspace.var;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.entity.widget.ability.HasGetStatusValue;
import org.touchhome.bundle.api.entity.widget.ability.HasSetStatusValue;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.function.Consumer;

@Entity
@Getter
@Accessors(chain = true)
public final class WorkspaceStandaloneVariableEntity extends BaseEntity<WorkspaceStandaloneVariableEntity>
        implements HasSetStatusValue, HasGetStatusValue {

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

    @Override
    public Object getStatusValue(GetStatusValueRequest request) {
        if(request.getDynamicParameters().has("lastValue")) {
            return request.getDynamicParameters().get("lastValue");
        }
        return value;
    }

    @Override
    public void setStatusValue(SetStatusValueRequest request) {
        this.value = request.floatValue(0F);
        request.getEntityContext().save(this);
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
    public String getSetStatusDescription() {
        return "Set value";
    }
}
