package org.homio.api.entity.device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.homio.api.converter.JSONConverter;
import org.homio.api.entity.BaseEntity;
import org.homio.api.entity.HasJsonData;
import org.homio.api.model.JSON;
import org.homio.api.ui.field.selection.dynamic.HasDynamicParameterFields;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Setter
@Getter
@Accessors(chain = true)
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "device_series")
public abstract class DeviceSeriesEntity<T extends DeviceEntityAndSeries> extends BaseEntity
        implements HasDynamicParameterFields, HasJsonData {

    private static final String PREFIX = "devser_";
    private int priority;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = DeviceEntityAndSeries.class)
    private T deviceEntity;

    @Column(length = 65535)
    @Convert(converter = JSONConverter.class)
    private JSON jsonData = new JSON();

    @Override
    public final @NotNull String getEntityPrefix() {
        return PREFIX + getSeriesPrefix() + "_";
    }

    protected abstract String getSeriesPrefix();

    @Override
    public void getAllRelatedEntities(Set<BaseEntity> set) {
        set.add(deviceEntity);
    }

    @Override
    public int compareTo(@NotNull BaseEntity o) {
        if (o instanceof DeviceSeriesEntity) {
            return Integer.compare(this.priority, ((DeviceSeriesEntity<?>) o).priority);
        }
        return super.compareTo(o);
    }

    @Override
    protected long getChildEntityHashCode() {
        long result = priority;
        result = 31 * result + jsonData.toString().hashCode();
        return result;
    }
}
