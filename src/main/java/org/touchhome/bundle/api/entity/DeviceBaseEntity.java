package org.touchhome.bundle.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.json.JSONObject;
import org.touchhome.bundle.api.converter.JSONObjectConverter;
import org.touchhome.bundle.api.model.HasPosition;
import org.touchhome.bundle.api.model.Status;
import org.touchhome.bundle.api.optionProvider.SelectPlaceOptionLoader;
import org.touchhome.bundle.api.ui.UISidebarMenu;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.UIFieldType;
import org.touchhome.bundle.api.ui.field.color.UIFieldColorStatusMatch;
import org.touchhome.bundle.api.ui.field.selection.UIFieldSelectValueOnEmpty;
import org.touchhome.bundle.api.ui.field.selection.UIFieldSelection;

import javax.persistence.*;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@UISidebarMenu(icon = "fas fa-shapes", parent = UISidebarMenu.TopSidebarMenu.HARDWARE, bg = "#51145e", overridePath = "devices")
@NoArgsConstructor
@Accessors(chain = true)
public abstract class DeviceBaseEntity<T extends DeviceBaseEntity> extends BaseEntity<T>
        implements HasPosition<DeviceBaseEntity>, HasJsonData<T>, HasStatusAndMsg<T> {

    @UIField(readOnly = true, order = 100)
    @Getter
    private String ieeeAddress;

    @Setter
    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @UIField(order = 20, type = UIFieldType.SelectBox)
    @UIFieldSelection(SelectPlaceOptionLoader.class)
    @UIFieldSelectValueOnEmpty(label = "SELECT_PLACE", color = "#748994")
    private PlaceEntity ownerPlace;

    @Getter
    @UIField(order = 22, readOnly = true, hideOnEmpty = true)
    @UIFieldColorStatusMatch
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private Status status;
    @Getter
    @UIField(order = 23, readOnly = true, hideOnEmpty = true)
    @Column(length = 512)
    private String statusMessage;
    @Getter
    @Setter
    @UIField(order = 22, readOnly = true)
    @Enumerated(EnumType.STRING)
    @UIFieldColorStatusMatch
    @Column(length = 32)
    private Status joined = Status.UNKNOWN;
    @Lob
    @Getter
    @Column(length = 3145728) // 3MB
    @Convert(converter = JSONObjectConverter.class)
    private JSONObject jsonData = new JSONObject();
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

    @Override
    public T setStatus(Status status) {
        this.status = status;
        return (T) this;
    }

    @Override
    public T setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        return (T) this;
    }

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

    /**
     * Determine if status, joined and statusMessage should be reset to default at startup
     */
    @JsonIgnore
    public boolean isResetStatusAtStartup() {
        return true;
    }
}
