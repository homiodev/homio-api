package org.homio.api.entity.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Set;
import lombok.*;
import org.homio.api.entity.HasJsonData;
import org.homio.api.fs.TreeNode;
import org.homio.api.model.Icon;
import org.jetbrains.annotations.NotNull;

public interface HasPathAlias extends HasJsonData {

    default boolean removeAlias(@NotNull int aliasCode) {
        Set<Alias> aliases = getAliases();
        if (aliases.removeIf(alias -> alias.alias == aliasCode)) {
            setJsonDataObject("alias", aliases);
            return true;
        }
        return false;
    }

    default boolean createAlias(@NotNull TreeNode path, @NotNull String name, @NotNull Icon icon) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Alias name cannot be empty");
        }
        Set<Alias> aliases = getAliases();
        if (aliases.add(new Alias(path.getId(), Math.abs(path.hashCode()), name, icon))) {
            setJsonDataObject("alias", aliases);
            return true;
        }
        return false;
    }

    default Alias getAlias(int alias) {
        Set<Alias> aliases = getAliases();
        return aliases.stream().filter(a -> a.alias == alias).findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unable to find alias with hashCode: " + alias));
    }

    @JsonIgnore
    default Set<Alias> getAliases() {
        return getJsonDataSet("alias", Alias.class);
    }

    @Getter
    @Setter
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @NoArgsConstructor
    @AllArgsConstructor
    class Alias {
        private String path;
        @EqualsAndHashCode.Include
        private int alias;
        private String name;
        private Icon icon;
    }
}
