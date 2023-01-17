package org.touchhome.bundle.api.entity;

import static org.touchhome.bundle.api.ui.field.UIFieldType.StaticDate;

import java.util.Date;
import javax.persistence.*;
import lombok.Getter;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.HasEntityIdentifier;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.util.ApplicationContextHolder;

@Getter
@MappedSuperclass
public class CrudEntity<T> implements HasEntityIdentifier {
    @Id @GeneratedValue private Integer id;

    @UIField(order = 4, hideInEdit = true, type = StaticDate)
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

    protected void afterPersist(EntityContext entityContext) {}

    protected void beforePersist() {}

    @Override
    public String getEntityID() {
        return String.valueOf(id);
    }
}
