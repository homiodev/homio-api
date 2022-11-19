package org.touchhome.bundle.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.JSONObject;
import org.touchhome.bundle.api.converter.JSONObjectConverter;
import org.touchhome.bundle.api.ui.field.UIField;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NoArgsConstructor
public abstract class PinBaseEntity<T extends DeviceBaseEntity<T>> extends BaseEntity<PinBaseEntity<T>> implements HasJsonData {
    public static final String PREFIX = "pin_";

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = DeviceBaseEntity.class)
    private T owner;

    @UIField(order = 20, readOnly = true)
    private int address;

    @UIField(order = 100, onlyEdit = true)
    private String description;

    @Getter
    @Column(length = 1000)
    @Convert(converter = JSONObjectConverter.class)
    private JSONObject jsonData = new JSONObject();

    private int position;

    @Override
    public String getEntityPrefix() {
        return PREFIX;
    }

    @Override
    public void getAllRelatedEntities(Set<BaseEntity> set) {
        set.add(owner);
    }
}
