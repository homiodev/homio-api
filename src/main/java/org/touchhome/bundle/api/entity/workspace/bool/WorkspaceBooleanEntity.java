package org.touchhome.bundle.api.entity.workspace.bool;

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
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.util.function.Consumer;

@Entity
@Getter
@Accessors(chain = true)
public final class WorkspaceBooleanEntity extends BaseEntity<WorkspaceBooleanEntity> implements
        HasGetStatusValue, HasSetStatusValue {

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
    public String getStatusValue(GetStatusValueRequest request) {
        return value.toString();
    }

    @Override
    public void setStatusValue(SetStatusValueRequest request) {
        this.value = request.booleanValue(false);
        request.getEntityContext().save(this);
    }

    @Override
    public String getSetStatusDescription() {
        return "Boolean set value";
    }

    @Override
    public String getGetStatusDescription() {
        return "Boolean get last value";
    }
}
