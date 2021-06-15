package org.touchhome.bundle.api.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.entity.widget.HasLineChartSeries;
import org.touchhome.bundle.api.entity.workspace.backup.WorkspaceBackupEntity;

import java.util.Date;
import java.util.List;

@Repository("backupRepository")
public class WorkspaceBackupRepository extends AbstractRepository<WorkspaceBackupEntity> {

    public WorkspaceBackupRepository() {
        super(WorkspaceBackupEntity.class);
    }

    @Transactional
    public List<Object[]> getLineChartSeries(BaseEntity baseEntity, Date from, Date to) {
        return HasLineChartSeries.buildValuesQuery(em, "WorkspaceBackupEntity.fetchValues", baseEntity, from, to).getResultList();
    }

    @Transactional
    public Float getBackupLastValue(WorkspaceBackupEntity entity) {
        List list = em.createNamedQuery("WorkspaceBackupEntity.fetchLastValue").setParameter("source", entity).setMaxResults(1).getResultList();
        return list.isEmpty() ? 0 : (Float) list.get(0);
    }

    @Transactional
    public double getPieSumChartSeries(BaseEntity source, Date from, Date to) {
        return (double) HasLineChartSeries.buildValuesQuery(em, "WorkspaceBackupEntity.fetchSum", source, from, to).getSingleResult();
    }

    @Transactional
    public double getPieCountChartSeries(BaseEntity source, Date from, Date to) {
        return (double) HasLineChartSeries.buildValuesQuery(em, "WorkspaceBackupEntity.fetchCount", source, from, to).getSingleResult();
    }

    /*@Override
    public Date getMinDate(BaseEntity source) {
        return null;
        //em.createNamedQuery("WorkspaceBackupValueCrudEntity.fetchMinDate", Date.class)
        //       .setParameter("source", source).getSingleResult();
    }*/
}



