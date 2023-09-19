package org.homio.api.util;

import static org.homio.api.util.JsonUtils.OBJECT_MAPPER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.EntityContext;
import org.homio.api.entity.BaseEntity;
import org.homio.api.entity.widget.ability.HasSetStatusValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class DataSourceUtil {

    public static SelectionSource getSelection(String value) {
        String[] items = value.split("###");
        return new SelectionSource(items[0], items.length > 1 ? items[1] : null);
    }

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
    public static @NotNull DataSourceContext getSource(@NotNull EntityContext entityContext, @Nullable String dataSource) {
        DataSourceContext dataSourceContext = new DataSourceContext(dataSource);
        if (StringUtils.isNotEmpty(dataSource)) {
            dataSource = dataSource.split("###")[0];
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

    private static @Nullable Object evaluateDataSource(String dsb, String source, EntityContext entityContext) {
        return switch (dsb) {
            case "bean" -> entityContext.getBean(source, Object.class);
            case "entityByClass" -> entityContext.getEntity(source);
            default -> null;
        };
    }

    @Getter
    @RequiredArgsConstructor
    public static class DataSourceContext {

        private final String raw;
        private Object source;
        private String sourceClass;

        public <T> T getSource(@NotNull Class<T> targetType, T defaultValue) {
            if (targetType.isAssignableFrom(source.getClass())) {
                return (T) source;
            }
            return defaultValue;
        }

        @Nullable
        public String getSourceEntityID() {
            return Optional.ofNullable(getSource(BaseEntity.class, null)).map(BaseEntity::getEntityID).orElse(null);
        }
    }

    @Getter
    public static class SelectionSource {

        private final String value;
        private JsonNode metadata;

        public SelectionSource(String value, String metadata) {
            this.value = value;
            try {
                this.metadata = OBJECT_MAPPER.readValue(metadata, JsonNode.class);
            } catch (Exception e) {
                this.metadata = OBJECT_MAPPER.createObjectNode();
            }
        }
    }
}
