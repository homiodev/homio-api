package org.homio.api.console;

import org.homio.api.exception.NotFoundException;
import org.homio.api.model.HasEntityIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ConsolePluginTable<T extends HasEntityIdentifier> extends ConsolePlugin<Collection<T>> {

    @Override
    default @NotNull RenderType getRenderType() {
        return RenderType.table;
    }

    Class<T> getEntityClass();

    default @NotNull HasEntityIdentifier findEntity(@NotNull String entityID) {
        Collection<? extends HasEntityIdentifier> baseEntities = getValue();
        return baseEntities.stream().filter(e -> e.getEntityID().equals(entityID))
                .findAny().orElseThrow(() -> new NotFoundException("Entity <" + entityID + "> not found"));
    }
}
