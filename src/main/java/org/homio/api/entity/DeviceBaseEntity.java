package org.homio.api.entity;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.homio.api.ui.field.UIFieldType.HTML;
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
import org.homio.api.ui.field.color.UIFieldColorBgRef;
import org.homio.api.ui.field.condition.UIFieldShowOnCondition;
import org.homio.api.ui.field.selection.UIFieldSelectValueOnEmpty;
import org.homio.api.ui.field.selection.UIFieldSelection;
import org.homio.api.ui.field.selection.UIFieldTreeNodeSelection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@UISidebarMenu(icon = "fas fa-shapes", parent = UISidebarMenu.TopSidebarMenu.HARDWARE, bg = "#51145e", overridePath = "devices")
@NoArgsConstructor
@Accessors(chain = true)
public abstract class DeviceBaseEntity<T extends DeviceBaseEntity> extends BaseEntity<T>
    implements HasJsonData, HasStatusAndMsg<T> {

    private static final String PREFIX = "dvc_";

    @UIField(hideInEdit = true, order = 5, hideOnEmpty = true)
    @Getter
    @Nullable
    private String ieeeAddress;

    @Setter
    @Getter
    @Column(length = 64)
    @UIField(order = 50, type = UIFieldType.SelectBox, color = "#538744")
    @UIFieldSelection(SelectPlaceOptionLoader.class)
    @UIFieldSelectValueOnEmpty(label = "SELECT_PLACE")
    @Nullable
    @UIFieldShowOnCondition("return !context.get('compactMode')")
    private String place;

    @Getter
    @Setter
    @Column(length = 100_000)
    @Convert(converter = JSONConverter.class)
    @NotNull
    private JSON jsonData = new JSON();

    @UIField(order = 1, fullWidth = true, color = "#89AA50", type = HTML, style = "height: 32px;")
    @UIFieldShowOnCondition("return context.get('compactMode')")
    @UIFieldColorBgRef(value = "statusColor", animate = true)
    @UIFieldGroup(value = "GENERAL", order = 1, borderColor = "#CDD649")
    public String getCompactDescription() {
        String description = getCompactDescriptionImpl();
        if (description == null) {
            description = getName();
        }
        Status status = getStatus();
        return """
            <div class="inline-2row_d"><div>%s <span style="color:%s">${%s}</span>
            <span style="float:right" class="color-primary">%s</span></div><div>${%s}</div></div>""".formatted(
            getIeeeAddress(), status.getColor(), status, trimToEmpty(getModel()), description);
    }

    @JsonIgnore
    public String getCompactDescriptionImpl() {
        return null;
    }

    @JsonIgnore
    public @Nullable String getModel() {
        return null;
    }

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
        return new EntityStatus(status);
    }

    // May be required for @UIFieldColorBgRef("statusColor")
    public @NotNull String getStatusColor() {
        EntityStatus entityStatus = getEntityStatus();
        if (entityStatus == null || entityStatus.getValue().isOnline()) {
            return "";
        }
        return entityStatus.getColor() + "30";
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
    protected @Nullable String getImageIdentifierImpl() {
        return getJsonData("img");
    }

    @Override
    public final @NotNull String getEntityPrefix() {
        return PREFIX + getDevicePrefix() + "_";
    }

    protected abstract @NotNull String getDevicePrefix();
}
