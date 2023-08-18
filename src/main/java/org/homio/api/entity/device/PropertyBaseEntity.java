package org.homio.api.entity.device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;
import org.homio.api.converter.JSONConverter;
import org.homio.api.entity.BaseEntity;
import org.homio.api.entity.HasJsonData;
import org.homio.api.model.JSON;
import org.homio.api.ui.field.UIField;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class PropertyBaseEntity<O extends DeviceBaseEntity>
        extends BaseEntity implements HasJsonData {

    public static final String PREFIX = "prop_";
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = DeviceBaseEntity.class)
    private O owner;
    @UIField(order = 20, hideInEdit = true, hideOnEmpty = true)
    private Integer address;
    @UIField(order = 1)
    private String description;
    @Getter
    @Setter
    @Column(length = 10_000)
    @Convert(converter = JSONConverter.class)
    private JSON jsonData = new JSON();
    private int position;

    @Override
    public String getDefaultName() {
        return null;
    }

    @JsonIgnore
    public O getOwnerTarget() {
        if (owner instanceof HibernateProxy) {
            if (((HibernateProxy) owner).getHibernateLazyInitializer().isUninitialized()) {
                return null;
            }
            return ((O) ((HibernateProxy) owner).getHibernateLazyInitializer().getImplementation());
        } else {
            return owner;
        }
    }

    @Override
    public final @NotNull String getEntityPrefix() {
        return PREFIX + getPropertyPrefix() + "_";
    }

    public abstract @NotNull String getPropertyPrefix();

    @Override
    public void getAllRelatedEntities(@NotNull Set<BaseEntity> set) {
        set.add(owner);
    }
}
