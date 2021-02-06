package org.touchhome.bundle.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.NaturalId;
import org.json.JSONPropertyIgnore;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.HasEntityIdentifier;
import org.touchhome.bundle.api.repository.AbstractRepository;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.util.ApplicationContextHolder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.function.Supplier;

import static org.touchhome.bundle.api.ui.field.UIFieldType.StaticDate;

@Log4j2
@MappedSuperclass
public abstract class BaseEntity<T extends BaseEntity> implements HasEntityIdentifier, Serializable {

    @Id
    @GeneratedValue
    @Getter
    private Integer id;

    @Version
    private Integer version;

    @NaturalId
    @Column(name = "entityID", unique = true, nullable = false)
    private String entityID;

    @UIField(order = 10, inlineEdit = true)
    @Getter
    private String name;

    @Column(nullable = false)
    @UIField(order = 16, readOnly = true, type = StaticDate)
    @Getter
    private Date creationTime;

    @UIField(order = 17, readOnly = true, type = StaticDate)
    @Getter
    private Date updateTime;

    @Transient
    private String entityIDSupplierStr;

    public T setId(Integer id) {
        this.id = id;
        return (T) this;
    }

    public T setName(String name) {
        this.name = name;
        return (T) this;
    }

    public T setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
        return (T) this;
    }

    public String getTitle() {
        String name = getName();
        return name == null || name.trim().length() == 0 ? getEntityID() : name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseEntity that = (BaseEntity) o;
        return entityID != null ? entityID.equals(that.entityID) : that.entityID == null;
    }

    @Override
    public int hashCode() {
        return entityID != null ? entityID.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "{'entityID':'" + getEntityID(false) + "'\'}";
    }

    public String getType() {
        return this.getClass().getSimpleName();
    }

    @PrePersist
    public final void prePersist() {
        if (this.creationTime == null) {
            this.creationTime = new Date();
        }
        this.updateTime = new Date();
        this.getEntityID(true);
        if (StringUtils.isEmpty(getName())) {
            setName(refreshName());
        }
        this.beforePersist();
        this.validate();
    }

    //firest before persist
    protected void beforePersist() {

    }

    // fires before persist/update
    protected void validate() {

    }

    protected void beforeUpdate() {

    }

    protected void beforeRemove() {

    }

    // fires after fetch from db/cache
    public void afterFetch(EntityContext entityContext) {

    }

    @PreUpdate
    private void preUpdate() {
        this.updateTime = new Date();
        this.beforeUpdate();
        this.validate();
    }

    @PreRemove
    private void preRemove() {
        this.beforeRemove();
    }

    public String refreshName() {
        return null;
    }

    public T computeEntityID(Supplier<String> entityIDSupplier) {
        entityIDSupplierStr = entityIDSupplier.get();
        if (this.name == null) {
            this.name = entityIDSupplierStr;
        }
        return (T) this;
    }

    public String getEntityID() {
        return getEntityID(false);
    }

    public T setEntityID(String entityID) {
        this.entityID = entityID;
        return (T) this;
    }

    public String getEntityID(boolean create) {
        if (create && this.entityID == null) {
            AbstractRepository repo = getEntityContext().getRepository(getClass());
            String simpleId = entityIDSupplierStr;
            if (simpleId == null) {
                String name = getClass().getSimpleName();
                if (name.endsWith("DeviceEntity")) {
                    name = name.substring(0, name.length() - "DeviceEntity".length());
                }
                simpleId = name + "_" + System.currentTimeMillis();
            }
            this.entityID = simpleId.startsWith(getEntityPrefix()) ? simpleId : getEntityPrefix() + simpleId;
        }
        return this.entityID;
    }

    public void getAllRelatedEntities(Set<BaseEntity> set) {
    }

    @Override
    public String getIdentifier() {
        return getEntityID() == null ? String.valueOf(getId()) : getEntityID();
    }

    public void copy() {
        id = null;
        entityID = null;
        creationTime = null;
        updateTime = null;

        getEntityContext().getBean(EntityManager.class).detach(this);
    }

    public void merge(T entity) {
        this.name = entity.getName();
    }

    @JsonIgnore
    @JSONPropertyIgnore
    public EntityContext getEntityContext() {
        return ApplicationContextHolder.getBean(EntityContext.class);
    }

    public abstract String getEntityPrefix();
}
