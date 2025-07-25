package org.homio.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.*;

import static org.homio.api.ContextSetting.setMemValue;

@Log4j2
@MappedSuperclass
public abstract class BaseEntity implements BaseEntityIdentifier, Comparable<BaseEntity> {

    @JsonIgnore
    @Transient
    @Setter
    private Context context;

    @Id
    @Getter
    @JsonIgnore // serialized by Bean2MixIn
    @Column(length = 128, nullable = false, unique = true)
    @GeneratedValue(generator = "id-generator")
    @GenericGenerator(
            name = "id-generator",
            strategy = "org.homio.app.repository.generator.HomioIdGenerator")
    private String entityID;

    @Getter
    @Version
    @JsonIgnore
    private int version;
    @Getter
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

    public void setInMemoryKV(@NotNull String key, @Nullable Object value) {
        setMemValue(this, key, "", value);
    }

    /**
     * Configure OptionModel for show it in select box
     *
     * @param optionModel model to configure
     * @param context
     */
    public void configureOptionModel(@NotNull OptionModel optionModel, @NotNull Context context) {
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

    public boolean isDisableView() {
        if (context == null) {
            return false;
        }
        UserEntity user = context.user().getLoggedInUser();
        if (user != null && !user.isAdmin()) {
            try {
                user.assertViewAccess(this);
            } catch (Exception ignore) {
                return true;
            }
        }
        return false;
    }

    public boolean isDisableEdit() {
        if (context == null) {
            return false;
        }
        UserEntity user = context.user().getLoggedInUser();
        if (user != null && !user.isAdmin()) {
            try {
                user.assertEditAccess(this);
            } catch (Exception ignore) {
                return true;
            }
        }
        return false;
    }

    public boolean isDisableDelete() {
        if (context == null) {
            return false;
        }
        UserEntity user = context.user().getLoggedInUser();
        if (user != null && !user.isAdmin()) {
            try {
                user.assertDeleteAccess(this);
            } catch (Exception ignore) {
                return true;
            }
        }
        return false;
    }

    public String setEntityID(@Nullable String entityID) {
        if (entityID == null) {
            this.entityID = null;
        } else {
            String prefix = getEntityPrefix();
            if (entityID.startsWith(prefix)) {
                this.entityID = entityID;
            } else {
                this.entityID = prefix + entityID;
            }
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
        return Comparator.nullsFirst(String::compareTo).compare(this.getTitle(), o.getTitle());
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
            @NotNull String field, @NotNull JSONObject metadata) {
        throw new NotImplementedException(
                "Method 'handleTextFieldAction' must be implemented in class: "
                + getClass().getSimpleName()
                + " if calls by UI. Field: "
                + field
                + ". Meta: "
                + metadata);
    }

    @JsonIgnore
    protected abstract long getChildEntityHashCode();

    public @NotNull Context context() {
        return context;
    }

    @JsonIgnore
    @Transient
    public final @NotNull Set<String> getMissingMandatoryFields() {
        Set<String> set = new HashSet<>();
        assembleMissingMandatoryFields(set);
        return set;
    }

    /**
     * List of mandatory fields for entity
     */
    protected void assembleMissingMandatoryFields(@NotNull Set<String> fields) {
    }

    public boolean tryUpdateEntity(Runnable handler) {
        long entityHashCode = getEntityHashCode();
        handler.run();
        return getEntityHashCode() != entityHashCode;
    }
}
