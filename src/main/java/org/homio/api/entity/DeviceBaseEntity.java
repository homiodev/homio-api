package org.homio.api.entity;

import static org.homio.api.ui.field.selection.UIFieldTreeNodeSelection.IMAGE_PATTERN;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.homio.api.converter.JSONConverter;
import org.homio.api.model.JSON;
import org.homio.api.model.Status;
import org.homio.api.model.Status.EntityStatus;
import org.homio.api.optionProvider.SelectPlaceOptionLoader;
import org.homio.api.ui.UISidebarMenu;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldGroup;
import org.homio.api.ui.field.UIFieldType;
import org.homio.api.ui.field.selection.UIFieldSelectValueOnEmpty;
import org.homio.api.ui.field.selection.UIFieldSelection;
import org.homio.api.ui.field.selection.UIFieldTreeNodeSelection;
import org.jetbrains.annotations.Nullable;

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
    @Column(length = 64)
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

    /**
     * Uses on UI to set png image with appropriate status and mark extra image if need
     */
    public @Nullable Status.EntityStatus getEntityStatus() {
        Status status = getStatus();
        return status == null ? null : new EntityStatus(status);
    }

    /**
     * Uses to show 'outdated' image on entity
     */
    public @Nullable Boolean isOutdated() {
        return null;
    }

    @UIField(order = 500, hideInView = true)
    @UIFieldTreeNodeSelection(pattern = IMAGE_PATTERN, dialogTitle = "DIALOG.SELECT_IMAGE_ID")
    @UIFieldGroup(value = "ADVANCED", order = 50, borderColor = "#FF1E00")
    public @Nullable String getImageIdentifier() {
        return getImageIdentifierImpl();
    }

    public void setImageIdentifier(String value) {
        setJsonData("img", value);
    }

    /**
     * Uses as fallback image in case if getImageIdentifier() return null or if image not exists
     *
     * @return FQDN image url
     */
    public @Nullable String getFallbackImageIdentifier() {
        return null;
    }

    @JsonIgnore
    protected @Nullable String getImageIdentifierImpl() {
        return getJsonData("img");
    }
}
