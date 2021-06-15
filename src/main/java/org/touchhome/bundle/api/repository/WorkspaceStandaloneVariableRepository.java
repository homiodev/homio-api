package org.touchhome.bundle.api.repository;

import org.springframework.stereotype.Repository;
import org.touchhome.bundle.api.entity.workspace.WorkspaceStandaloneVariableEntity;

@Repository
public class WorkspaceStandaloneVariableRepository extends AbstractRepository<WorkspaceStandaloneVariableEntity> {

    public WorkspaceStandaloneVariableRepository() {
        super(WorkspaceStandaloneVariableEntity.class);
    }
}
