package org.homio.api.entity;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import org.homio.api.EntityContext;
import org.homio.api.model.HasEntityIdentifier;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldType;
import org.homio.api.util.ApplicationContextHolder;
import org.jetbrains.annotations.NotNull;

@Getter
@MappedSuperclass
public class CrudEntity<T> implements HasEntityIdentifier {
    @Id
    @GeneratedValue
    private Integer id;

    @UIField(order = 4, hideInEdit = true, type = UIFieldType.StaticDate)
    @Column(nullable = false)
    private Date creationTime;

    public T setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
        return (T) this;
    }

    @PrePersist
    public final void prePersist() {
        if (this.creationTime == null) {
            this.creationTime = new Date();
        }
        beforePersist();
    }

    @PostPersist
    public final void postPersist() {
        afterPersist(ApplicationContextHolder.getBean(EntityContext.class));
    }

    protected void afterPersist(EntityContext entityContext) {

    }

    protected void beforePersist() {

    }

    @Override
    public @NotNull String getEntityID() {
        return String.valueOf(id);
    }
}
