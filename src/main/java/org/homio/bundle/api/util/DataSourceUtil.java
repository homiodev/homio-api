package org.homio.bundle.api.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.entity.widget.ability.HasSetStatusValue;
import org.json.JSONObject;

public class DataSourceUtil {

    /**
     * Try update target data source value
     *
     * @param entityContext          - entity context
     * @param dataSource             - data source
     * @param dynamicParameterFields - field?
     * @param value                  - new value
     */
    public static void setValue(EntityContext entityContext, String dataSource, JSONObject dynamicParameterFields, Object value) {
        DataSourceUtil.DataSourceContext dsContext = DataSourceUtil.getSourceRequire(entityContext, dataSource);
        ((HasSetStatusValue) dsContext.getSource()).setStatusValue(new HasSetStatusValue.SetStatusValueRequest(entityContext, dynamicParameterFields, value));
    }

    public static DataSourceContext getSourceRequire(EntityContext entityContext, String dataSource) {
        DataSourceContext dsContext = getSource(entityContext, dataSource);
        if (dsContext.getSource() == null) {
            throw new IllegalArgumentException("Unable to find source set data source");
        }
        if (!(dsContext.getSource() instanceof HasSetStatusValue)) {
            throw new IllegalArgumentException("Set data source must be of type HasSetStatusValue");
        }
        return dsContext;
    }

    /**
     * Samples:
     * <pre>
     * 0 = "entityByClass",
     * 1 = "HasAggregateValueFromSeries",
     * 2 = "wgv_0x00158d0002a4dd24_state_left",
     * 3 = "wg_z2m_0x00158d0002a4dd24",
     * 4 = "wg_zigbee"
     * </pre>
     *
     * @param entityContext - entity context
     * @param dataSource    - source
     * @return dataSource context
     */
    public static DataSourceContext getSource(EntityContext entityContext, String dataSource) {
        DataSourceContext dataSourceContext = new DataSourceContext();
        if (StringUtils.isNotEmpty(dataSource)) {
            List<String> vds = Arrays.asList(dataSource.split("~~~"));
            Collections.reverse(vds);
            if (vds.size() > 2) {
                dataSourceContext.sourceClass = vds.get(1);
                dataSourceContext.source = evaluateDataSource(vds.get(0), vds.get(2), entityContext);

            } else {
                throw new IllegalArgumentException("Unable to parse dataSource");
            }
        }
        return dataSourceContext;
    }

    private static Object evaluateDataSource(String dsb, String source, EntityContext entityContext) {
        switch (dsb) {
            case "bean":
                return entityContext.getBean(source, Object.class);
            case "entityByClass":
                return entityContext.getEntity(source);
        }
        return null;
    }

    /**
     * Build data source string
     *
     * @param beanClass     - target bean class
     * @param beanBaseClass - i.e. "CloudProviderService" for @UIFieldBeanSelection(CloudProviderService.class)
     * @return data source string
     */
    public static String buildBeanSource(Class<?> beanBaseClass, Class<?> beanClass) {
        return StringUtils.uncapitalize(beanClass.getSimpleName()) + "~~~" + beanBaseClass.getSimpleName() + "~~~bean";
    }

    public static String buildBeanSource(Class<?> beanClass) {
        return StringUtils.uncapitalize(beanClass.getSimpleName()) + "~~~" + beanClass.getInterfaces()[0].getSimpleName() + "~~~bean";
    }

    @Getter
    public static class DataSourceContext {

        private Object source;
        private String sourceClass;
    }
}
