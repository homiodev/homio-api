package org.homio.api.entity.device;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.homio.api.converter.JSONConverter;
import org.homio.api.entity.BaseEntity;
import org.homio.api.entity.HasPermissions;
import org.homio.api.model.JSON;
import org.homio.api.model.Status;
import org.homio.api.ui.UISidebarMenu;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldGroup;
import org.homio.api.ui.field.action.HasDynamicUIFields;
import org.homio.api.ui.field.color.UIFieldColorBgRef;
import org.homio.api.ui.field.condition.UIFieldShowOnCondition;
import org.homio.api.ui.field.selection.UIFieldTreeNodeSelection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.homio.api.ui.field.UIFieldType.HTML;
import static org.homio.api.ui.field.selection.UIFieldTreeNodeSelection.IMAGE_PATTERN;

@Getter
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@UISidebarMenu(order = 700,
        icon = "fas fa-shapes",
        parent = UISidebarMenu.TopSidebarMenu.HARDWARE,
        bg = "#FFFFFF",
        overridePath = "devices",
        filter = {"*:fas fa-filter:#8DBA73", "status:fas fa-heart-crack:#C452C4"},
        sort = {
                "name~#FF9800:fas fa-arrow-up-a-z:fas fa-arrow-down-z-a",
                "status~#7EAD28:fas fa-turn-up:fas fa-turn-down",
                "place~#9C27B0:fas fa-location-dot:fas fa-location-dot fa-rotate-180"
        })
@NoArgsConstructor
public abstract class DeviceBaseEntity extends BaseEntity implements DeviceContract, HasPermissions {

    public static final String PREFIX = "dvc_";
    @UIField(hideInEdit = true, order = 5, hideOnEmpty = true)
    private @Nullable String ieeeAddress;
    @Getter
    @Setter
    @Column(length = 100_000)
    @Convert(converter = JSONConverter.class)
    @JsonIgnore
    private @NotNull JSON jsonData = new JSON();

    @Override
    @UIField(order = 10, inlineEdit = true)
    @UIFieldGroup(value = "GENERAL", order = 10)
    @UIFieldShowOnCondition("return !context.get('compactMode')")
    public String getName() {
        return super.getName();
    }

    @UIField(order = 1, hideOnEmpty = true, hideInEdit = true, fullWidth = true, color = "#89AA50", type = HTML)
    @UIFieldShowOnCondition("return !context.get('compactMode')")
    @UIFieldColorBgRef(value = "statusColor", animate = true)
    @UIFieldGroup(value = "TOP", order = 1)
    public String getDescription() {
        if (isCompactMode()) {
            return null;
        }
        return getDescriptionImpl();
    }

    @JsonIgnore
    public String getDescriptionImpl() {
        Set<String> fields = getMissingMandatoryFields();
        if (!fields.isEmpty()) {
            return "W.ERROR." + fields.iterator().next().toUpperCase() + "_REQUIRED";
        }
        return null;
    }

    @UIField(order = 1, fullWidth = true, color = "#89AA50", type = HTML, style = "height: 32px;")
    @UIFieldShowOnCondition("return context.get('compactMode')")
    @UIFieldColorBgRef(value = "statusColor", animate = true)
    @UIFieldGroup(value = "TOP", order = 1)
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
    public boolean isDisableView() {
        return super.isDisableView() || getJsonData("dis_view", false);
    }

    @Override
    public boolean isDisableEdit() {
        return super.isDisableEdit() || getJsonData("dis_edit", false);
    }

    @Override
    public boolean isDisableDelete() {
        return super.isDisableDelete() || getJsonData("dis_del", false);
    }

    /**
     * Uses to show 'outdated' image on entity
     */
    public @Nullable Boolean isOutdated() {
        return null;
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
        return getType() + ".png";
    }

    @Override
    public final @NotNull String getEntityPrefix() {
        return PREFIX + getDevicePrefix() + "_";
    }

    protected abstract @NotNull String getDevicePrefix();

    @Override
    protected long getChildEntityHashCode() {
        long result = ieeeAddress != null ? ieeeAddress.hashCode() : 0;
        result = 31 * result + jsonData.toString().hashCode();
        return result;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String key, Object value) {
        setJsonData(key, value);
        if (this instanceof HasDynamicUIFields field) {
            field.writeDynamicFieldValue(key, value);
        }
    }

    @UIField(order = 2, hideInView = true)
    @UIFieldTreeNodeSelection(pattern = IMAGE_PATTERN, dialogTitle = "DIALOG.SELECT_IMAGE_ID")
    @UIFieldGroup("ADVANCED")
    public @Nullable String getImageIdentifier() {
        return getImageIdentifierImpl();
    }

    public void setImageIdentifier(@Nullable String value) {
        setJsonData("img", value);
    }
}
