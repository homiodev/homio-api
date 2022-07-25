package org.touchhome.bundle.api.entity.workspace.backup;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.entity.widget.AggregationType;
import org.touchhome.bundle.api.entity.widget.ChartRequest;
import org.touchhome.bundle.api.entity.widget.HasAggregateValueFromSeries;
import org.touchhome.bundle.api.entity.widget.HasTimeValueSeries;
import org.touchhome.bundle.api.repository.WorkspaceBackupRepository;

import javax.persistence.*;
import java.util.List;

@Getter
@Entity
@Accessors(chain = true)
public final class WorkspaceBackupEntity extends BaseEntity<WorkspaceBackupEntity> implements HasTimeValueSeries,
        HasAggregateValueFromSeries {

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
    public List<Object[]> getTimeValueSeries(ChartRequest request) {
        return request.getEntityContext().getBean(WorkspaceBackupRepository.class)
                .getLineChartSeries(this, request);
    }

    @Override
    public Float getAggregateValueFromSeries(ChartRequest request, AggregationType aggregationType) {
        WorkspaceBackupRepository repo = request.getEntityContext().getBean(WorkspaceBackupRepository.class);
        switch (aggregationType) {
            case Max:
                return repo.getBackupMAXValue(this, request);
            case Min:
                return repo.getBackupMINValue(this, request);
            case Sum:
                return repo.getBackupSUMValue(this, request);
            case Average:
                return repo.getBackupAVGValue(this, request);
            case Count:
                return repo.getBackupCountValue(this, request);
            case Median:
                throw new IllegalStateException("Not implemented");
        }
        return repo.getBackupLastValue(this, request);
    }
}
