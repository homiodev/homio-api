package org.touchhome.bundle.api.entity.widget;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.converter.JSONObjectConverter;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.entity.HasJsonData;
import org.touchhome.bundle.api.model.HasPosition;
import org.touchhome.bundle.api.ui.UISidebarMenu;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.UIFieldIgnore;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@UISidebarMenu(icon = "fas fa-tachometer-alt", bg = "#107d6b", overridePath = "widgets")
@Accessors(chain = true)
@NoArgsConstructor
public abstract class WidgetBaseEntity<T extends WidgetBaseEntity> extends BaseEntity<T>
        implements HasPosition<WidgetBaseEntity>, HasJsonData<T> {

    @ManyToOne(fetch = FetchType.LAZY)
    private WidgetTabEntity widgetTabEntity;

    @Getter
    private int xb = 0;

    @Getter
    private int yb = 0;

    @Getter
    private int bw = 1;

    @Getter
    private int bh = 1;

    @Getter
    @UIField(order = 20)
    private boolean autoScale;
    @Lob
    @Column(length = 1048576)
    @Convert(converter = JSONObjectConverter.class)
    private JSONObject jsonData = new JSONObject();

    public String getFieldFetchType() {
        return getJsonData("fieldFetchType", null);
    }

    public T setFieldFetchType(String value) {
        jsonData.put("fieldFetchType", value);
        return (T) this;
    }

    @Override
    @UIFieldIgnore
    public String getName() {
        return super.getName();
    }

    public abstract String getImage();

    public boolean updateRelations(EntityContext entityContext) {
        return false;
    }
}
