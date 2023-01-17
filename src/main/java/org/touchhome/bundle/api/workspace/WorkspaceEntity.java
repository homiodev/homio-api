package org.touchhome.bundle.api.workspace;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.touchhome.bundle.api.entity.BaseEntity;

@Getter
@Setter
@Entity
@Accessors(chain = true)
public final class WorkspaceEntity extends BaseEntity<WorkspaceEntity>
        implements Comparable<WorkspaceEntity> {

    public static final String PREFIX = "ws_";

    @Lob
    @Column(length = 10_000_000)
    private String content;

    @Override
    public int compareTo(WorkspaceEntity o) {
        return this.getCreationTime().compareTo(o.getCreationTime());
    }

    @Override
    public String getDefaultName() {
        return null;
    }

    @Override
    protected void validate() {
        if (getName() == null || getName().length() < 2 || getName().length() > 10) {
            throw new IllegalStateException("Workspace tab name must be between 2..10 characters");
        }
    }

    @Override
    public String getEntityPrefix() {
        return PREFIX;
    }
}
