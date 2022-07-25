package org.touchhome.bundle.api.repository;

import org.springframework.stereotype.Repository;
import org.touchhome.bundle.api.entity.widget.ChartRequest;
import org.touchhome.bundle.api.entity.workspace.backup.WorkspaceBackupEntity;

import java.util.List;

@Repository("backupRepository")
public class WorkspaceBackupRepository extends AbstractRepository<WorkspaceBackupEntity> {

    public WorkspaceBackupRepository() {
        super(WorkspaceBackupEntity.class);
    }

    public List<Object[]> getLineChartSeries(WorkspaceBackupEntity source, ChartRequest request) {
        //noinspection unchecked
        return (List<Object[]>) queryForValues("creationTime, value", source, request, "ORDER BY creationTime");
    }

    public Float getBackupLastValue(WorkspaceBackupEntity source, ChartRequest request) {
        return findExactOneBackupValue("value", source, request, "ORDER BY creationTime DESC");
    }

    public float getBackupMAXValue(WorkspaceBackupEntity source, ChartRequest request) {
        return findExactOneBackupValue("MAX(value)", source, request);
    }

    public float getBackupMINValue(WorkspaceBackupEntity source, ChartRequest request) {
        return findExactOneBackupValue("MIN(value)", source, request);
    }

    public float getBackupSUMValue(WorkspaceBackupEntity source, ChartRequest request) {
        return findExactOneBackupValue("SUM(value)", source, request);
    }

    public float getBackupAVGValue(WorkspaceBackupEntity source, ChartRequest request) {
        return findExactOneBackupValue("AVG(value)", source, request);
    }

    public float getBackupCountValue(WorkspaceBackupEntity source, ChartRequest request) {
        return findExactOneBackupValue("COUNT(value)", source, request);
    }

    private float findExactOneBackupValue(String select, WorkspaceBackupEntity entity, ChartRequest request) {
        return findExactOneBackupValue(select, entity, request, "");
    }

    private float findExactOneBackupValue(String select, WorkspaceBackupEntity entity, ChartRequest request, String sort) {
        List<?> list = queryForValues(select, entity, request, sort);
        return list.isEmpty() ? 0 : (Float) list.get(0);
    }

    private List<?> queryForValues(String select, WorkspaceBackupEntity entity, ChartRequest request, String sort) {
        return (List<?>) em.createQuery("SELECT " + select + " FROM WorkspaceBackupValueCrudEntity " +
                        "where workspaceBackupEntity = :source and creationTime >= :from and creationTime <= :to" + sort)
                .setParameter("source", entity)
                .setParameter("from", request.getFrom())
                .setParameter("to", request.getTo())
                .getResultList();
    }
}



