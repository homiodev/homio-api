package org.touchhome.bundle.api.entity.workspace.backup;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.entity.widget.HasBarChartSeries;
import org.touchhome.bundle.api.entity.widget.HasDisplaySeries;
import org.touchhome.bundle.api.entity.widget.HasLineChartSeries;
import org.touchhome.bundle.api.entity.widget.HasPieChartSeries;
import org.touchhome.bundle.api.repository.WorkspaceBackupRepository;

import javax.persistence.*;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Entity
@Accessors(chain = true)
@NamedQueries({
        @NamedQuery(name = "WorkspaceBackupEntity.fetchLastValue",
                query = "SELECT e.value FROM WorkspaceBackupValueCrudEntity e WHERE e.workspaceBackupEntity = :source ORDER BY e.creationTime DESC"),
        @NamedQuery(name = "WorkspaceBackupEntity.fetchValues",
                query = "SELECT e.creationTime, e.value FROM WorkspaceBackupValueCrudEntity e WHERE e.workspaceBackupEntity = :source AND e.creationTime >= :from AND e.creationTime <= :to ORDER BY e.creationTime"),
        @NamedQuery(name = "WorkspaceBackupEntity.fetchSum",
                query = "SELECT SUM(e.value) FROM WorkspaceBackupValueCrudEntity e WHERE e.workspaceBackupEntity = :source AND e.creationTime >= :from AND e.creationTime <= :to"),
        @NamedQuery(name = "WorkspaceBackupEntity.fetchCount",
                query = "SELECT COUNT(e) FROM WorkspaceBackupValueCrudEntity e WHERE e.workspaceBackupEntity = :source AND e.creationTime >= :from AND e.creationTime <= :to"),
        @NamedQuery(name = "WorkspaceBackupEntity.fetchMinDate",
                query = "SELECT MIN(e.creationTime) FROM WorkspaceBackupValueCrudEntity e WHERE e.workspaceBackupEntity = :source GROUP BY e.workspaceBackupEntity")
})
public final class WorkspaceBackupEntity extends BaseEntity<WorkspaceBackupEntity> implements HasLineChartSeries,
        HasPieChartSeries, HasDisplaySeries, HasBarChartSeries {

    public static final String PREFIX = "wsbp_";

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    private WorkspaceBackupGroupEntity workspaceBackupGroupEntity;

    @Setter
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "workspaceBackupEntity", cascade = CascadeType.REMOVE)
    private List<WorkspaceBackupValueCrudEntity> values;

    @Override
    public String getTitle() {
        return "Backup[" + workspaceBackupGroupEntity.getName() + "] - variable: " + this.getName();
    }

    @Override
    public void merge(WorkspaceBackupEntity entity) {
        super.merge(entity);
        this.values = entity.getValues();
    }

    @Override
    public String getEntityPrefix() {
        return PREFIX;
    }

    @Override
    public Map<LineChartDescription, List<Object[]>> getLineChartSeries(EntityContext entityContext, JSONObject parameters, Date from, Date to, String dateFromNow) {
        return Collections.singletonMap(new LineChartDescription(), entityContext.getBean(WorkspaceBackupRepository.class).getLineChartSeries(this, from, to));
    }

    @Override
    public double getPieSumChartSeries(EntityContext entityContext, Date from, Date to, String dateFromNow) {
        return entityContext.getBean(WorkspaceBackupRepository.class).getPieSumChartSeries(this, from, to);
    }

    @Override
    public double getPieCountChartSeries(EntityContext entityContext, Date from, Date to, String dateFromNow) {
        return entityContext.getBean(WorkspaceBackupRepository.class).getPieCountChartSeries(this, from, to);
    }

    @Override
    public Object getDisplayValue() {
        List<WorkspaceBackupValueCrudEntity> values = getValues();
        return values.isEmpty() ? null : values.iterator().next().getValue();
    }

    @Override
    public double getBarValue(EntityContext entityContext) {
        return entityContext.getBean(WorkspaceBackupRepository.class).getBackupLastValue(this);
    }
}
