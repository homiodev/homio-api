package org.homio.api.entity.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.homio.api.Context;
import org.homio.api.entity.BaseEntityIdentifier;
import org.homio.api.entity.HasStatusAndMsg;
import org.homio.api.fs.FileSystemProvider;
import org.homio.api.fs.TreeConfiguration;
import org.homio.api.model.ActionResponseModel;
import org.homio.api.model.Icon;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldType;
import org.homio.api.ui.field.action.HasDynamicContextMenuActions;
import org.homio.api.ui.field.action.UIContextMenuAction;
import org.homio.api.util.DataSourceUtil;
import org.homio.api.util.Lang;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BaseFileSystemEntity<FS extends FileSystemProvider>
  extends BaseEntityIdentifier, HasDynamicContextMenuActions, HasStatusAndMsg,
  HasPathAlias {

  Map<String, Map<Integer, FileSystemProvider>> fileSystemMap = new HashMap<>();

  default @NotNull List<TreeConfiguration> buildFileSystemConfiguration(@NotNull Context context) {
    List<TreeConfiguration> configurations = new ArrayList<>(3);
    configurations.add(new TreeConfiguration(this));
    String pathAlias1 = DataSourceUtil.getSelection(getAliasOnePath()).getValue("");
    if (!pathAlias1.isEmpty()) {
      configurations.add(new TreeConfiguration(this, pathAlias1, new Icon(getAliasOneIcon(), getAliasOneIconColor())));
    }
    String pathAlias2 = DataSourceUtil.getSelection(getAliasTwoPath()).getValue("");
    if (!pathAlias2.isEmpty()) {
      configurations.add(new TreeConfiguration(this, pathAlias2, new Icon(getAliasTwoIcon(), getAliasTwoIconColor())));
    }
    for (TreeConfiguration configuration : configurations) {
      configuration.setDynamicUpdateId("tree-%s-%d".formatted(configuration.getId(), configuration.getAlias()));
    }

    return configurations;
  }

  @NotNull String getFileSystemRoot();

  /**
   * @return Short FS alias
   */
  @JsonIgnore
  @NotNull String getFileSystemAlias();

  /**
   * @return Does show fs in file manager console tab
   */
  @JsonIgnore
  boolean isShowInFileManager();

  @JsonIgnore
  @NotNull Icon getFileSystemIcon();

  @JsonIgnore
  boolean requireConfigure();

  default @NotNull FS getFileSystem(@NotNull Context context, int alias) {
    String key = getEntityID();
    var fsMap = fileSystemMap.computeIfAbsent(key, s -> new HashMap<>());
    if (!fsMap.containsKey(alias)) {
      fsMap.put(alias, buildFileSystem(context, alias));
    }
    return (FS) fsMap.get(alias);
  }

  @NotNull FS buildFileSystem(@NotNull Context context, int alias);

  @JsonIgnore
  long getConnectionHashCode();

  @UIField(order = 1, hideInEdit = true, hideOnEmpty = true, fullWidth = true, bg = "#334842", type = UIFieldType.HTML)
  default String getDescription() {
    String prefix = getEntityPrefix();
    return requireConfigure() ? Lang.getServerMessage(prefix.substring(0, prefix.length() - 1) + ".description") : null;
  }

  @UIContextMenuAction(value = "RESTART_FS", icon = "fas fa-file-invoice", iconColor = "#418121")
  default ActionResponseModel restart(Context context) {
    var fsMap = fileSystemMap.computeIfAbsent(getEntityID(), s -> new HashMap<>());
    fsMap.values().forEach(d -> d.restart(true));
    return ActionResponseModel.showSuccess("Success restarted");
  }

  @Override
  default void afterDelete() {
    var fsMap = fileSystemMap.remove(getEntityID());
    if (fsMap != null) {
      fsMap.values().forEach(FileSystemProvider::dispose);
    }
  }

  @Override
  default void afterUpdate() {
    var fsMap = fileSystemMap.computeIfAbsent(getEntityID(), s -> new HashMap<>());
    fsMap.values().forEach(d -> d.setEntity(this));
  }

  boolean isShowHiddenFiles();

  /**
   * List of archive formats that able to expand
   *
   * @return - list of archive extensions
   */
  default @NotNull Set<String> getSupportArchiveFormats() {
    return Collections.emptySet();
  }
}
