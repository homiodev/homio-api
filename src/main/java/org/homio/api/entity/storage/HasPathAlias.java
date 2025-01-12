package org.homio.api.entity.storage;

import org.homio.api.entity.HasJsonData;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldColorPicker;
import org.homio.api.ui.field.UIFieldGroup;
import org.homio.api.ui.field.UIFieldIconPicker;
import org.homio.api.ui.field.UIFieldTab;
import org.homio.api.ui.field.UIFieldType;
import org.homio.api.ui.field.selection.UIFieldTreeNodeSelection;
import org.homio.api.util.DataSourceUtil;
import org.springframework.util.StringUtils;

import static java.lang.String.format;

public interface HasPathAlias extends HasJsonData {

  default String getAliasPath(int alias) {
    String value = DataSourceUtil.getSelection(getAliasOnePath()).getValue("");
    if (Math.abs(value.hashCode()) == alias) {
      return value;
    }
    value = DataSourceUtil.getSelection(getAliasTwoPath()).getValue("");
    if (Math.abs(value.hashCode()) == alias) {
      return value;
    }
    throw new IllegalArgumentException("Unable to find alias with hashCode: " + alias);
  }

  @UIField(order = 500, type = UIFieldType.HTML, hideInEdit = true)
  default String getAliasOne() {
    if (StringUtils.hasLength(getAliasOnePath())) {
      return format("<div class=\"it-group\"><i style=\"color:%s\" class=\"%s\"></i>%s</div>",
        getAliasOneIconColor(),
        getAliasOneIcon(),
        DataSourceUtil.getSelection(getAliasOnePath()).getValue());
    }
    return "-";
  }

  @UIField(order = 1, label = "path", hideInView = true)
  @UIFieldGroup(value = "ALIAS_1", order = 100, borderColor = "#46B8C4")
  @UIFieldTreeNodeSelection(dialogTitle = "DIALOG.SELECT_PATH", allowSelectFiles = false, allowSelectDirs = true)
  @UIFieldTab("ALIASES")
  default String getAliasOnePath() {
    return getJsonData("a1p");
  }

  default void setAliasOnePath(String value) {
    setJsonData("a1p", value);
  }

  @UIField(order = 2, label = "icon", hideInView = true)
  @UIFieldIconPicker(allowSize = false, simple = true)
  @UIFieldGroup("ALIAS_1")
  @UIFieldTab("ALIASES")
  default String getAliasOneIcon() {
    return getJsonData("a1i", "fas fa-hard-drive");
  }

  default void setAliasOneIcon(String value) {
    setJsonData("a1i", value);
  }

  @UIField(order = 3, label = "iconColor", hideInView = true)
  @UIFieldColorPicker
  @UIFieldGroup("ALIAS_1")
  @UIFieldTab("ALIASES")
  default String getAliasOneIconColor() {
    return getJsonData("a1c", "#ADB5BD");
  }

  default void setAliasOneIconColor(String value) {
    setJsonData("a1c", value);
  }

  @UIField(order = 501, type = UIFieldType.HTML, hideInEdit = true)
  default String getAliasTwo() {
    if (StringUtils.hasLength(getAliasTwoPath())) {
      return format("<div class=\"it-group\"><i style=\"color:%s\" class=\"%s\"></i>%s</div>",
        getAliasTwoIconColor(),
        getAliasTwoIcon(),
        DataSourceUtil.getSelection(getAliasTwoPath()).getValue());
    }
    return "-";
  }

  @UIField(order = 1, label = "path", hideInView = true)
  @UIFieldGroup(value = "ALIAS_2", order = 101, borderColor = "#46B8C4")
  @UIFieldTreeNodeSelection(dialogTitle = "DIALOG.SELECT_PATH", allowSelectFiles = false, allowSelectDirs = true)
  @UIFieldTab("ALIASES")
  default String getAliasTwoPath() {
    return getJsonData("a2p");
  }

  default void setAliasTwoPath(String value) {
    setJsonData("a2p", value);
  }

  @UIField(order = 2, label = "icon", hideInView = true)
  @UIFieldIconPicker(allowSize = false, simple = true)
  @UIFieldGroup("ALIAS_2")
  @UIFieldTab("ALIASES")
  default String getAliasTwoIcon() {
    return getJsonData("a2i", "fas fa-hard-drive");
  }

  default void setAliasTwoIcon(String value) {
    setJsonData("a2i", value);
  }

  @UIField(order = 3, label = "iconColor", hideInView = true)
  @UIFieldColorPicker
  @UIFieldGroup("ALIAS_2")
  @UIFieldTab("ALIASES")
  default String getAliasTwoIconColor() {
    return getJsonData("a2c", "#ADB5BD");
  }

  default void setAliasTwoIconColor(String value) {
    setJsonData("a2c", value);
  }
}
