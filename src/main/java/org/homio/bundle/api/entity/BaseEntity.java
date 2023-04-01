package org.homio.bundle.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Version;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.NaturalId;
import org.homio.bundle.api.BundleEntrypoint;
import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.ui.field.UIField;
import org.homio.bundle.api.ui.field.UIFieldType;
import org.homio.bundle.api.util.ApplicationContextHolder;
import org.jetbrains.annotations.NotNull;
import org.json.JSONPropertyIgnore;

@Log4j2
@MappedSuperclass
public abstract class BaseEntity<T extends BaseEntity> implements BaseEntityIdentifier<T>, Comparable<BaseEntity> {

    @Id
    @GeneratedValue
    @Getter
    private Integer id;

    @Version
    private Integer version;

    @NaturalId
    @Getter
    @Column(name = "entityID", unique = true, nullable = false)
    private String entityID;

    @UIField(order = 10, inlineEdit = true)
    @Getter
    private String name;

    @Column(nullable = false)
    @UIField(order = 20, hideInEdit = true, type = UIFieldType.StaticDate)
    @Getter
    private Date creationTime;

    @UIField(order = 30, hideInEdit = true, type = UIFieldType.StaticDate)
    @Getter
    private Date updateTime;

    public static BaseEntity fakeEntity(String entityID) {
        return new BaseEntity() {
            @Override
            public String getDefaultName() {
                return null;
            }

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

    /**
     * Method return default name to store when persist entity
     */
    @JsonIgnore
    public abstract String getDefaultName();

    @Override
    public int hashCode() {
        return entityID != null ? entityID.hashCode() : 0;
    }

    @Override
    public String toString() {
        return getTitle();
    }

    /**
     * @return Disable edit entity on UI
     */
    public boolean isDisableEdit() {
        return false;
    }

    /**
     * @return Disable delete entity on UI
     */
    public boolean isDisableDelete() {
        return false;
    }

    @PrePersist
    private void prePersist() {
        if (this.creationTime == null) {
            this.creationTime = new Date();
        }
        this.updateTime = new Date();
        if (StringUtils.isEmpty(getName())) {
            setName(refreshName());
        }
        this.beforePersist();
        if (this.entityID == null) {
            this.entityID = getEntityPrefix() + System.currentTimeMillis();
        }
        this.validate();
    }

    @PreUpdate
    private void preUpdate() {
        this.updateTime = new Date();
        this.beforeUpdate();
        this.validate();
    }

    // fires before persist/update
    protected void validate() {

    }

    protected void beforePersist() {

    }

    protected void beforeUpdate() {

    }

    public void beforeDelete(EntityContext entityContext) {

    }

    public T setEntityID(@NotNull String entityID) {
        String prefix = getEntityPrefix();
        if (entityID.startsWith(prefix)) {
            this.entityID = entityID;
        } else {
            this.entityID = prefix + entityID;
        }
        return (T) this;
    }

    /**
     * Accumulate list of related entities which has to be refreshed in cache after entity updated
     * @param set -
     */
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

    @JsonIgnore
    @JSONPropertyIgnore
    public EntityContext getEntityContext() {
        return ApplicationContextHolder.getBean(EntityContext.class);
    }

    public String getBundle() {
        return BundleEntrypoint.getBundleName(getClass());
    }

    @Override
    public int compareTo(@NotNull BaseEntity o) {
        return this.getTitle().compareTo(o.getTitle());
    }
}
