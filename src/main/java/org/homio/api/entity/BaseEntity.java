package org.homio.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.NotImplementedException;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.homio.api.AddonEntrypoint;
import org.homio.api.Context;
import org.homio.api.model.ActionResponseModel;
import org.homio.api.model.OptionModel;
import org.homio.api.model.Status;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldGroup;
import org.homio.api.ui.field.condition.UIFieldShowOnCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

@Log4j2
@MappedSuperclass
public abstract class BaseEntity implements
    BaseEntityIdentifier,
    Comparable<BaseEntity> {

    @JsonIgnore
    @Transient
    @Setter
    private Context context;

    @Id
    @Getter
    @JsonIgnore // serialized by Bean2MixIn
    @Column(length = 128, nullable = false, unique = true)
    @GeneratedValue(generator = "id-generator")
    @GenericGenerator(name = "id-generator", strategy = "org.homio.app.repository.generator.HomioIdGenerator")
    private String entityID;

    @Getter
    @Version
    @JsonIgnore
    private int version;

    @Getter
    @UIField(order = 10, inlineEdit = true)
    @UIFieldGroup(value = "GENERAL", order = 10)
    @UIFieldShowOnCondition("return !context.get('compactMode')")
    private String name;

    @Getter
    @JsonIgnore
    @Column(nullable = false)
    @CreationTimestamp
    private Date creationTime;

    @Getter
    @JsonIgnore
    @Column(nullable = false)
    @UpdateTimestamp
    private Date updateTime;

    /**
     * Configure OptionModel for show it in select box
     *
     * @param optionModel model to configure
     */
    public void configureOptionModel(@NotNull OptionModel optionModel) {
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Override
    public final boolean equals(@Nullable Object o) {
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
    @Nullable
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

    public String setEntityID(@NotNull String entityID) {
        String prefix = getEntityPrefix();
        if (entityID.startsWith(prefix)) {
            this.entityID = entityID;
        } else {
            this.entityID = prefix + entityID;
        }
        return this.entityID;
    }

    /**
     * Accumulate list of related entities which has to be refreshed in cache after entity updated
     *
     * @param set -
     */
    public void getAllRelatedEntities(@NotNull Set<BaseEntity> set) {
    }

    public @Nullable String getAddonID() {
        return AddonEntrypoint.getAddonID(getClass());
    }

    @Override
    public int compareTo(@NotNull BaseEntity o) {
        if (o instanceof HasOrder orderedEntity) {
            int value = Integer.compare(((HasOrder) this).getOrder(), orderedEntity.getOrder());
            if (value != 0) {
                return value;
            }
        }
        if (o instanceof HasStatusAndMsg other) {
            Status leftStatus = ((HasStatusAndMsg) this).getStatus();
            Status rightStatus = other.getStatus();
            int value = leftStatus.compareTo(rightStatus);
            if (value != 0) {
                return value;
            }
        }
        return this.getTitle().compareTo(o.getTitle());
    }

    @JsonIgnore
    public Date getEntityCreated() {
        return creationTime;
    }

    @JsonIgnore
    public Date getEntityUpdated() {
        return updateTime;
    }

    @JsonIgnore
    public long getEntityHashCode() {
        long result = entityID != null ? entityID.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result + getChildEntityHashCode();
    }

    /**
     * Calls only in case if field return 'string' value that has link with attributes name="link"
     *
     * @param field    target field
     * @param metadata has all attached values 'data-xxx="value"'
     * @return response
     */
    public @Nullable ActionResponseModel handleTextFieldAction(
        @NotNull String field,
        @NotNull JSONObject metadata) {
        throw new NotImplementedException("Method 'handleTextFieldAction' must be implemented in class: " + getClass().getSimpleName()
            + " if calls by UI. Field: " + field + ". Meta: " + metadata);
    }

    @JsonIgnore
    protected abstract long getChildEntityHashCode();

    public Context context() {
        return context;
    }
}
