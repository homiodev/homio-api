package org.homio.bundle.api.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import lombok.Getter;
import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.model.HasEntityIdentifier;
import org.homio.bundle.api.ui.field.UIField;
import org.homio.bundle.api.ui.field.UIFieldType;
import org.homio.bundle.api.util.ApplicationContextHolder;
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
