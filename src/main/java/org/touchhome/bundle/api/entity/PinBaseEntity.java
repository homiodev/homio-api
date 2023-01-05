package org.touchhome.bundle.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;
import org.json.JSONObject;
import org.touchhome.bundle.api.converter.JSONConverter;
import org.touchhome.bundle.api.model.JSON;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.UIFieldIgnore;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class PinBaseEntity<T extends PinBaseEntity<T, O>, O extends DeviceBaseEntity<O>>
        extends BaseEntity<T> implements HasJsonData {
    public static final String PREFIX = "pin_";

    @Override
    public String getDefaultName() {
        return null;
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = DeviceBaseEntity.class)
    private O owner;

    @JsonIgnore
    public O getOwnerTarget() {
        if (owner instanceof HibernateProxy) {
            if (((HibernateProxy) owner).getHibernateLazyInitializer().isUninitialized()) {
                return null;
            }
            return ((O) ((HibernateProxy) owner).getHibernateLazyInitializer().getImplementation());
        } else {
            return (O) owner;
        }
    }

    @UIField(order = 20, hideInEdit = true)
    private int address;

    @UIField(order = 100, hideInView = true)
    private String description;

    @Getter
    @Setter
    @Column(length = 1000)
    @Convert(converter = JSONConverter.class)
    private JSON jsonData = new JSON();

    private int position;

    @Override
    public String getEntityPrefix() {
        return PREFIX;
    }

    @Override
    public void getAllRelatedEntities(Set<BaseEntity> set) {
        set.add(owner);
    }

    @Override
    @JsonIgnore
    @UIFieldIgnore
    public Date getCreationTime() {
        return super.getCreationTime();
    }

    @Override
    @JsonIgnore
    @UIFieldIgnore
    public Date getUpdateTime() {
        return super.getUpdateTime();
    }
}
