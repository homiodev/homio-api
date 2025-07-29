package org.homio.api.console;

import java.util.Collection;
import org.homio.api.exception.NotFoundException;
import org.homio.api.model.HasEntityIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ConsolePluginTable<T extends HasEntityIdentifier>
    extends ConsolePlugin<Collection<T>> {

  @Override
  default @NotNull RenderType getRenderType() {
    return RenderType.table;
  }

  @NotNull
  Class<T> getEntityClass();

  default @NotNull HasEntityIdentifier findEntity(@NotNull String entityID) {
    Collection<? extends HasEntityIdentifier> baseEntities = getValue();
    return baseEntities.stream()
        .filter(e -> e.getEntityID().equals(entityID))
        .findAny()
        .orElseThrow(() -> new NotFoundException("Entity <" + entityID + "> not found"));
  }

  /** Fires every 1sec to get fresh values for corresponding cells, if user open console plugin */
  default @Nullable Collection<TableCell> getUpdatableValues() {
    return null;
  }

  record TableCell(String row, String cell, Object value) {}
}
