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
    @UIFieldSelection(SelectPlaceOptionLoader.class)
    @UIFieldSelectValueOnEmpty(label = "SELECT_PLACE", color = "#748994")
    private PlaceEntity ownerPlace;

    @Getter
    @Setter
    @UIField(order = 22, readOnly = true, hideOnEmpty = true)
    @UIFieldColorStatusMatch
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Getter
    @Setter
    @UIField(order = 23, readOnly = true, hideOnEmpty = true)
    private String statusMessage;

    @Getter
    @Setter
    @UIField(order = 22, readOnly = true)
    @Enumerated(EnumType.STRING)
    @UIFieldColorStatusMatch
    private Status joined = Status.UNKNOWN;

    @Lob
    @Column(length = 1048576)
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

    protected <P> T setJsonData(String key, P value) {
        jsonData.put(key, value);
        return (T) this;
    }

    protected Integer getJsonData(String key, int defaultValue) {
        return jsonData.optInt(key, defaultValue);
    }

    protected Boolean getJsonData(String key, boolean defaultValue) {
        return jsonData.optBoolean(key, defaultValue);
    }

    protected String getJsonData(String key, String defaultValue) {
        return jsonData.optString(key, defaultValue);
    }

    protected Long getJsonData(String key, long defaultValue) {
        return jsonData.optLong(key, defaultValue);
    }

    protected String getJsonData(String key) {
        return jsonData.optString(key);
    }

    protected Double getJsonData(String key, double defaultValue) {
        return jsonData.optDouble(key, defaultValue);
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
