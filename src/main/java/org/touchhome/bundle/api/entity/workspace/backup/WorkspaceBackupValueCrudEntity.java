package org.touchhome.bundle.api.entity.workspace.backup;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.CrudEntity;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Accessors(chain = true)
@Table(indexes = {@Index(columnList = "creationTime")})
public final class WorkspaceBackupValueCrudEntity extends CrudEntity<WorkspaceBackupValueCrudEntity> {

    @Column(nullable = false)
    private float value;

    @ManyToOne
    private WorkspaceBackupEntity workspaceBackupEntity;

    @Override
    public String getIdentifier() {
        return workspaceBackupEntity.getEntityID() + getCreationTime().getTime();
    }

    @Override
    protected void afterPersist(EntityContext entityContext) {
        TouchHomeUtils.VALUES_MAP.put(workspaceBackupEntity.getEntityID(), value);
        entityContext.event().fireEvent(workspaceBackupEntity.getEntityID(), value);
    }
}
