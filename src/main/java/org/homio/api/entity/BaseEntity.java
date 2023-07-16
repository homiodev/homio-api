package org.homio.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.homio.api.AddonEntrypoint;
import org.homio.api.EntityContext;
import org.homio.api.model.Icon;
import org.homio.api.model.OptionModel;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldGroup;
import org.homio.api.util.ApplicationContextHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONPropertyIgnore;

@Log4j2
@MappedSuperclass
public abstract class BaseEntity<T extends BaseEntity> implements
    BaseEntityIdentifier<T>,
    Comparable<BaseEntity> {

    @Id
    @Getter
    @Column(length = 64, nullable = false, unique = true)
    @GeneratedValue(generator = "id-generator")
    @GenericGenerator(name = "id-generator", strategy = "org.homio.app.repository.HomioIdGenerator")
    private String entityID;

    @Version
    private Integer version;

    @Getter
    @UIField(order = 10, inlineEdit = true)
    @UIFieldGroup(value = "GENERAL", order = 10)
    private String name;

    @Getter
    @JsonIgnore
    @Column(nullable = false)
    @UIFieldGroup("GENERAL")
    private Date creationTime;

    @Getter
    @JsonIgnore
    @Column(nullable = false)
    @UIFieldGroup("GENERAL")
    private Date updateTime;

    /**
     * Configure OptionModel for show it in select box
     *
     * @param optionModel model to configure
     */
    public void configureOptionModel(@NotNull OptionModel optionModel) {
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
        return Objects.equals(entityID, that.entityID);
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

    public void beforeDelete(@NotNull EntityContext entityContext) {

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
    public void getAllRelatedEntities(@NotNull Set<BaseEntity> set) {
    }

    public void copy() {
        entityID = null;
        creationTime = null;
        updateTime = null;

        getEntityContext().getBean(EntityManager.class).detach(this);
    }

    @JsonIgnore
    @JSONPropertyIgnore
    public @NotNull EntityContext getEntityContext() {
        return ApplicationContextHolder.getBean(EntityContext.class);
    }

    public @Nullable String getAddonID() {
        return AddonEntrypoint.getAddonID(getClass());
    }

    @Override
    public int compareTo(@NotNull BaseEntity o) {
        return this.getTitle().compareTo(o.getTitle());
    }

    /**
     * Specify entity font awesome icon for UI purposes
     */
    public @Nullable Icon getEntityIcon() {
        return null;
    }
}
