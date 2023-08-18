package org.homio.api.entity.device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.homio.api.converter.JSONConverter;
import org.homio.api.entity.BaseEntity;
import org.homio.api.model.JSON;
import org.homio.api.model.Status;
import org.homio.api.optionProvider.SelectPlaceOptionLoader;
import org.homio.api.ui.UISidebarMenu;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldGroup;
import org.homio.api.ui.field.UIFieldType;
import org.homio.api.ui.field.color.UIFieldColorBgRef;
import org.homio.api.ui.field.condition.UIFieldShowOnCondition;
import org.homio.api.ui.field.selection.UIFieldSelectValueOnEmpty;
import org.homio.api.ui.field.selection.UIFieldSelection;
import org.homio.api.ui.field.selection.UIFieldTreeNodeSelection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.homio.api.ui.field.UIFieldType.HTML;
import static org.homio.api.ui.field.selection.UIFieldTreeNodeSelection.IMAGE_PATTERN;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@UISidebarMenu(icon = "fas fa-shapes", parent = UISidebarMenu.TopSidebarMenu.HARDWARE, bg = "#FFFFFF", overridePath = "devices")
@NoArgsConstructor
public abstract class DeviceBaseEntity extends BaseEntity implements DeviceContract {

    private static final String PREFIX = "dvc_";

    @UIField(hideInEdit = true, order = 5, hideOnEmpty = true)
    @Getter
    private @Nullable String ieeeAddress;

    @Setter
    @Getter
    @Column(length = 64)
    @UIField(order = 50, type = UIFieldType.SelectBox, color = "#538744")
    @UIFieldSelection(SelectPlaceOptionLoader.class)
    @UIFieldSelectValueOnEmpty(label = "PLACEHOLDER.SELECT_PLACE")
    @UIFieldShowOnCondition("return !context.get('compactMode')")
    private @Nullable String place;

    @Getter
    @Setter
    @Column(length = 100_000)
    @Convert(converter = JSONConverter.class)
    private @NotNull JSON jsonData = new JSON();

    @UIField(order = 1, hideOnEmpty = true, fullWidth = true, color = "#89AA50", type = HTML)
    @UIFieldShowOnCondition("return !context.get('compactMode')")
    @UIFieldColorBgRef(value = "statusColor", animate = true)
    @UIFieldGroup(value = "GENERAL", order = 1, borderColor = "#CDD649")
    public String getDescription() {
        if (isCompactMode()) {
            return null;
        }
        return getDescriptionImpl();
    }

    @JsonIgnore
    public String getDescriptionImpl() {
        return null;
    }

    @UIField(order = 1, fullWidth = true, color = "#89AA50", type = HTML, style = "height: 32px;")
    @UIFieldShowOnCondition("return context.get('compactMode')")
    @UIFieldColorBgRef(value = "statusColor", animate = true)
    @UIFieldGroup(value = "GENERAL", order = 1, borderColor = "#CDD649")
    public String getCompactDescription() {
        if (!isCompactMode()) {
            return null;
        }
        return getCompactDescriptionImpl();
    }

    @JsonIgnore
    public String getCompactDescriptionImpl() {
        String cd = getName();
        Status status = getStatus();
        return """
                <div class="inline-2row_d"><div>%s <span style="color:%s">${%s}</span>
                <span style="float:right" class="color-primary">%s</span></div><div>${%s}</div></div>""".formatted(
                getIeeeAddress(), status.getColor(), status, trimToEmpty(getModel()), cd);
    }

    public boolean isCompactMode() {
        return false;
    }

    /**
     * @return Define order in which entity will be shown on UI map
     */
    public int getOrder() {
        return 100;
    }

    public void setIeeeAddress(@Nullable String ieeeAddress) {
        this.ieeeAddress = ieeeAddress;
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

    public void setImageIdentifier(@Nullable String value) {
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
    public @Nullable String getImageIdentifierImpl() {
        if (getJsonData().has("img")) {
            return getJsonData("img");
        }
        return getType();
    }

    @Override
    public final @NotNull String getEntityPrefix() {
        return PREFIX + getDevicePrefix() + "_";
    }

    protected abstract @NotNull String getDevicePrefix();

    @Override
    protected int getChildEntityHashCode() {
        int result = ieeeAddress != null ? ieeeAddress.hashCode() : 0;
        result = 31 * result + (place != null ? place.hashCode() : 0);
        result = 31 * result + jsonData.hashCode();
        return result;
    }
}