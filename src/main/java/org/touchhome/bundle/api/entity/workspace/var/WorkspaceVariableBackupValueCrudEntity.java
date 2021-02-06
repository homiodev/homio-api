package org.touchhome.bundle.api.entity.workspace.var;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.touchhome.bundle.api.entity.CrudEntity;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Accessors(chain = true)
@Table(indexes = {@Index(columnList = "creationTime")})
public final class WorkspaceVariableBackupValueCrudEntity extends CrudEntity<WorkspaceVariableBackupValueCrudEntity> {

    @Column(nullable = false)
    private float value;

    @ManyToOne
    private WorkspaceVariableEntity workspaceVariableEntity;

    @Override
    public String getIdentifier() {
        return workspaceVariableEntity.getEntityID() + getCreationTime().getTime();
    }
}
