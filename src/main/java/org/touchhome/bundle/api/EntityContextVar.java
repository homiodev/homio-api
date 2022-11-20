package org.touchhome.bundle.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Objects;
import java.util.function.Predicate;

public interface EntityContextVar {

    EntityContext getEntityContext();

    Object get(@NotNull String variableId);

    void set(@NotNull String variableId, @Nullable Object value);

    default void setIfNotMatch(@NotNull String variableId, @Nullable Object value) {
        if (!Objects.equals(get(variableId), value)) {
            set(variableId, value);
        }
    }

    default void setIfNotMatch(@NotNull String variableId, boolean value) {
        if (!Objects.equals(get(variableId), value)) {
            set(variableId, value);
        }
    }

    default void setIfNotMatch(@NotNull String variableId, float value) {
        if (!Objects.equals(get(variableId), value)) {
            set(variableId, value);
        }
    }

    default void inc(@NotNull String variableId, float value) {
        Object o = get(variableId);
        if (Number.class.isAssignableFrom(o.getClass())) {
            set(variableId, ((Number) o).floatValue() + value);
        }
    }

    String getTitle(@NotNull String variableId, @Nullable String defaultTitle);

    /**
     * Return count of messages
     */
    long count(@NotNull String variableId);

    /**
     * Does variable exists in system
     */
    boolean exists(@NotNull String variableId);

    /**
     * Get or create new variable.
     */
    default String createVariable(@NotNull String groupId, @NotNull String variableName, @NotNull VariableType variableType) {
        return createVariable(groupId, null, variableName, variableType, null);
    }

    /**
     * Get or create new variable.
     */
    default String createVariable(@NotNull String groupId, @Nullable String variableId, @NotNull String variableName,
                                  @NotNull VariableType variableType) {
        return createVariable(groupId, variableId, variableName, variableType, null);
    }

    /**
     * Get or create new variable.
     */
    default String createVariable(@NotNull String groupId, @Nullable String variableId, @NotNull String variableName,
                                  @NotNull VariableType variableType, @Nullable String description) {
        return createVariable(groupId, variableId, variableName, variableType, description, null);
    }

    String createVariable(@NotNull String groupId, @Nullable String variableId, @NotNull String variableName,
                          @NotNull VariableType variableType, @Nullable String description, @Nullable String color);

    default boolean createGroup(@NotNull String groupId, @NotNull String groupName) {
        return createGroup(groupId, groupName, false, "fas fa-layer-group", "#18C0DB", null);
    }

    /**
     * @param locked - locked group and related variables unable to remove from UI
     * @return false if group already exists
     */
    boolean createGroup(@NotNull String groupId, @NotNull String groupName, boolean locked, @NotNull String icon,
                        @NotNull String iconColor, @Nullable String description);

    default boolean createGroup(@NotNull String groupId, @NotNull String groupName, boolean locked, @NotNull String icon,
                                @NotNull String iconColor) {
        return createGroup(groupId, groupName, locked, icon, iconColor, null);
    }

    /**
     * Remove group and all associated variables
     */
    boolean removeGroup(@NotNull String groupId);

    @Getter
    @RequiredArgsConstructor
    enum VariableType {
        Any(o -> true, 0),
        Json(o -> {
            try {
                new JSONObject(o.toString());
                return true;
            } catch (Exception ex) {
                return false;
            }
        }, "{}"),
        Boolean(o -> o instanceof Boolean, false),
        Float(o -> o instanceof Number, 0F);

        private final Predicate<Object> validate;
        private final Object defaultValue;
    }
}
