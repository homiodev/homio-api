package org.touchhome.bundle.api.entity.workspace;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.entity.widget.HasBarChartSeries;
import org.touchhome.bundle.api.entity.widget.HasDisplaySeries;
import org.touchhome.bundle.api.entity.widget.HasGaugeSeries;
import org.touchhome.bundle.api.entity.widget.HasSliderSeries;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@Getter
@Accessors(chain = true)
public final class WorkspaceStandaloneVariableEntity extends BaseEntity<WorkspaceStandaloneVariableEntity>
        implements HasBarChartSeries, HasSliderSeries, HasDisplaySeries, HasGaugeSeries {

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
    public double getBarValue(EntityContext entityContext) {
        return value;
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
