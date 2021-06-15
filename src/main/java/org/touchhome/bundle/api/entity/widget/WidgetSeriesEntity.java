package org.touchhome.bundle.api.entity.widget;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.touchhome.bundle.api.converter.JSONObjectConverter;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.entity.HasJsonData;

import javax.persistence.*;
import java.util.Set;

@Setter
@Getter
@Accessors(chain = true)
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class WidgetSeriesEntity<T extends WidgetBaseEntityAndSeries> extends BaseEntity<WidgetSeriesEntity>
        implements Comparable<WidgetSeriesEntity>, HasJsonData<T> {

    private int priority;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = WidgetBaseEntityAndSeries.class)
    private T widgetEntity;

    @Lob
    @Column(length = 1048576)
    @Convert(converter = JSONObjectConverter.class)
    private JSONObject jsonData = new JSONObject();

    public abstract String getDataSource();

    public Object setDataSource(String value) {
        setJsonData("ds", value);
        return this;
    }

    public Object setDynamicParameterFieldsHolder(JSONObject value) {
        setJsonData("dsp", value);
        return this;
    }

    public JSONObject getDynamicParameterFieldsHolder() {
        return getJsonData().optJSONObject("dsp");
    }

    @Override
    public void getAllRelatedEntities(Set<BaseEntity> set) {
        set.add(widgetEntity);
    }

    @Override
    public int compareTo(@NotNull WidgetSeriesEntity entity) {
        return Integer.compare(this.priority, entity.priority);
    }
}
