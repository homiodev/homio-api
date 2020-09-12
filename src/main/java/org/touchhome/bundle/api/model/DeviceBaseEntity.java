package org.touchhome.bundle.api.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.json.JSONObject;
import org.touchhome.bundle.api.DynamicOptionLoader;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.converter.JSONObjectConverter;
import org.touchhome.bundle.api.json.Option;
import org.touchhome.bundle.api.ui.UISidebarMenu;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.UIFieldType;
import org.touchhome.bundle.api.ui.field.selection.UIFieldSelectValueOnEmpty;
import org.touchhome.bundle.api.ui.field.selection.UIFieldSelection;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "device_discriminator")
@UISidebarMenu(icon = "fas fa-shapes", parent = UISidebarMenu.TopSidebarMenu.HARDWARE, bg = "#51145e")
@NoArgsConstructor
@Accessors(chain = true)
public abstract class DeviceBaseEntity<T extends DeviceBaseEntity> extends BaseEntity<T> implements HasPosition<DeviceBaseEntity> {

    @UIField(readOnly = true, order = 100)
    @Getter
    private String ieeeAddress;

    @Setter
    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @UIField(order = 20, type = UIFieldType.Selection)
    @UIFieldSelection(SelectPlaceLoader.class)
    @UIFieldSelectValueOnEmpty(label = "SELECT_PLACE", color = "#748994")
    private PlaceEntity ownerPlace;

    @Getter
    @Lob
    @Column(length = 1048576)
    @Convert(converter = JSONObjectConverter.class)
    private JSONObject jsonData;

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

    public String getShortTitle() {
        return "";
    }

    @Override
    public String refreshName() {
        return getShortTitle();
    }

    @Override
    public void getAllRelatedEntities(Set<BaseEntity> set) {
        set.add(ownerPlace);
    }

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

    public static class SelectPlaceLoader implements DynamicOptionLoader {

        @Override
        public List<Option> loadOptions(Object parameter, EntityContext entityContext) {
            return Option.list(entityContext.findAll(PlaceEntity.class));
        }
    }
}
