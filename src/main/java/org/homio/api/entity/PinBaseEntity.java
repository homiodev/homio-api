package org.homio.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;
import org.homio.api.model.JSON;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldIgnore;
import org.homio.api.converter.JSONConverter;
import org.jetbrains.annotations.NotNull;

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
            return owner;
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
    public @NotNull String getEntityPrefix() {
        return PREFIX;
    }

    @Override
    public void getAllRelatedEntities(@NotNull Set<BaseEntity> set) {
        set.add(owner);
    }

    @Override
    @JsonIgnore
    @UIFieldIgnore
    public @NotNull Date getCreationTime() {
        return super.getCreationTime();
    }

    @Override
    @JsonIgnore
    @UIFieldIgnore
    public @NotNull Date getUpdateTime() {
        return super.getUpdateTime();
    }
}
