package org.homio.bundle.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.homio.bundle.api.converter.JSONConverter;
import org.homio.bundle.api.model.JSON;
import org.homio.bundle.api.optionProvider.SelectPlaceOptionLoader;
import org.homio.bundle.api.ui.UISidebarMenu;
import org.homio.bundle.api.ui.field.UIField;
import org.homio.bundle.api.ui.field.UIFieldType;
import org.homio.bundle.api.ui.field.selection.UIFieldSelectValueOnEmpty;
import org.homio.bundle.api.ui.field.selection.UIFieldSelection;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@UISidebarMenu(icon = "fas fa-shapes", parent = UISidebarMenu.TopSidebarMenu.HARDWARE, bg = "#51145e", overridePath = "devices")
@NoArgsConstructor
@Accessors(chain = true)
public abstract class DeviceBaseEntity<T extends DeviceBaseEntity> extends BaseEntity<T>
        implements HasJsonData, HasStatusAndMsg<T> {

    @UIField(hideInEdit = true, order = 5, hideOnEmpty = true)
    @Getter
    private String ieeeAddress;

    @Setter
    @Getter
    @UIField(order = 50, type = UIFieldType.SelectBox, color = "#538744")
    @UIFieldSelection(SelectPlaceOptionLoader.class)
    @UIFieldSelectValueOnEmpty(label = "SELECT_PLACE")
    private String place;

    @Getter
    @Setter
    @Column(length = 100_000)
    @Convert(converter = JSONConverter.class)
    private JSON jsonData = new JSON();

    /**
     * @return Define order in which entity will be shown on UI map
     */
    public int getOrder() {
        return 100;
    }

    public T setIeeeAddress(String ieeeAddress) {
        this.ieeeAddress = ieeeAddress;
        return (T) this;
    }

    @Override
    public boolean isDisableEdit() {
        return getJsonData("dis_edit", false);
    }

    @Override
    public boolean isDisableDelete() {
        return getJsonData("dis_del", false);
    }
}
