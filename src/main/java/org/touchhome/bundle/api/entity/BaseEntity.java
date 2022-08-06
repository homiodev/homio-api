package org.touchhome.bundle.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.NaturalId;
import org.json.JSONPropertyIgnore;
import org.touchhome.bundle.api.BundleEntryPoint;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.service.EntityService;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.util.ApplicationContextHolder;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;
import java.util.function.Supplier;

import static org.touchhome.bundle.api.ui.field.UIFieldType.StaticDate;

@Log4j2
@MappedSuperclass
public abstract class BaseEntity<T extends BaseEntity> implements BaseEntityIdentifier<T> {

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
    @UIField(order = 20, readOnly = true, type = StaticDate)
    @Getter
    private Date creationTime;

    @UIField(order = 30, readOnly = true, type = StaticDate)
    @Getter
    private Date updateTime;

    @Transient
    private String entityIDSupplierStr;

    public static BaseEntity fakeEntity(String entityID) {
        return new BaseEntity() {
            @Override
            public String getEntityPrefix() {
                return "";
            }
        }.setEntityID(entityID);
    }

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

    @Override
    public final boolean equals(Object o) {
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

    @PrePersist
    private void prePersist() {
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

    @PreUpdate
    private void preUpdate() {
        this.updateTime = new Date();
        this.beforeUpdate();
        this.validate();
    }

    @PostUpdate
    private void postUpdate() {
        this.afterUpdate(ApplicationContextHolder.getBean(EntityContext.class));
    }

    @PreRemove
    private void preDelete() {
        TouchHomeUtils.STATUS_MAP.remove(getEntityID());
        getEntityContext().unRegisterConsolePlugin(getEntityID());
        if (this instanceof EntityService) {
            try {
                ((EntityService<?, ?>) this).destroyService();
            } catch (Exception ex) {
                log.warn("Unable to destroy service for entity: {}", getTitle());
            }
        }
        this.beforeDelete();
    }

    // fires before persist/update
    protected void validate() {

    }

    //fires before persist
    protected void beforePersist() {

    }

    protected void beforeUpdate() {

    }

    protected void beforeDelete() {

    }

    /**
     * Entry point for entity on first load after server starts
     */
    public void onInit() {

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
            String sn = getClass().getSimpleName();

            String simpleId = entityIDSupplierStr;
            if (simpleId == null) {
                String name = sn;
                if (name.endsWith("DeviceEntity")) {
                    name = name.substring(0, name.length() - "DeviceEntity".length());
                }
                simpleId = name + "_" + System.currentTimeMillis();
            }
            this.entityID = simpleId.startsWith(getEntityPrefix()) ? simpleId : getEntityPrefix() + simpleId;
            /*if (this.entityID.length() > 100) {
                if (this.entityID.contains(sn)) {
                    int diff = this.entityID.length() - 100;
                    this.entityID = this.entityID.replace(sn, sn.substring(0, sn.length() - diff));
                } else {
                    this.entityID = getEntityPrefix() + System.currentTimeMillis();
                }
            }*/
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

    public String getBundle() {
        return BundleEntryPoint.getBundleName(getClass());
    }
}
