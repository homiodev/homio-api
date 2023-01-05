package org.touchhome.bundle.api.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.touchhome.bundle.api.converter.JSONConverter;
import org.touchhome.bundle.api.model.HasPosition;
import org.touchhome.bundle.api.model.JSON;
import org.touchhome.bundle.api.optionProvider.SelectPlaceOptionLoader;
import org.touchhome.bundle.api.ui.UISidebarMenu;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.UIFieldType;
import org.touchhome.bundle.api.ui.field.selection.UIFieldSelectValueOnEmpty;
import org.touchhome.bundle.api.ui.field.selection.UIFieldSelection;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@UISidebarMenu(icon = "fas fa-shapes", parent = UISidebarMenu.TopSidebarMenu.HARDWARE, bg = "#51145e", overridePath = "devices")
@NoArgsConstructor
@Accessors(chain = true)
public abstract class DeviceBaseEntity<T extends DeviceBaseEntity> extends BaseEntity<T>
        implements HasPosition<DeviceBaseEntity>, HasJsonData, HasStatusAndMsg<T> {

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
    @Column(length = 65535)
    @Convert(converter = JSONConverter.class)
    private JSON jsonData = new JSON();

    @Getter
    @Setter
    private int xb = 0;

    @Getter
    @Setter
    private int yb = 0;

    @Getter
    @Setter
    private int bw = 1;

    @Getter
    @Setter
    private int bh = 1;

    /**
     * Define order in which entity will be shown on UI map
     */
    public int getOrder() {
        return 100;
    }

    public T setIeeeAddress(String ieeeAddress) {
        this.ieeeAddress = ieeeAddress;
        return (T) this;
    }
}
