package org.touchhome.bundle.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface EntityContextVar {

    @NotNull EntityContext getEntityContext();

    default void listen(@NotNull String key, @NotNull String variableId, @NotNull Consumer<Object> listener) {
        getEntityContext().event().addEventListener(variableId, key, listener);
    }

    /**
     * Every writable variable has to have link listener which handle to write operation from UI, etc...
     */
    void setLinkListener(@NotNull String variableId, @NotNull Consumer<Object> listener);

    Object get(@NotNull String variableId);

    /**
     * Push new value in queue.
     *
     * @param variableId - variable id
     * @param value      - value may be State type, primitive, or String/Number/Boolean/etc...
     *                   ignore setting if value is null
     * @return converted value that has been stored into queue
     * @throws IllegalArgumentException if value doesn't validatet agains VariableType restriction
     */
    default Object set(@NotNull String variableId, @Nullable Object value) throws IllegalArgumentException {
        AtomicReference<Object> ref = new AtomicReference<>();
        set(variableId, value, ref::set);
        return ref.get();
    }

    /**
     * Push new value in queue.
     *
     * @param convertedValue - supplier calls before store value to queue and before fire events
     */
    void set(@NotNull String variableId, @Nullable Object value, Consumer<Object> convertedValue) throws IllegalArgumentException;

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

    /**
     * Get variable title - name or defaultTitle
     */
    String getTitle(@NotNull String variableId, @Nullable String defaultTitle);

    /**
     * Return count of messages
     */
    long count(@NotNull String variableId);

    /**
     * Does variable exists in system
     */
    boolean exists(@NotNull String variableId);

    boolean existsGroup(@NotNull String groupId);

    boolean renameGroup(@NotNull String groupId, @NotNull String name, @Nullable String description);

    boolean renameVariable(@NotNull String variableId, @NotNull String name, @Nullable String description);

    boolean setVariableIcon(@NotNull String variableId, @NotNull String icon, @Nullable String iconColor);

    @NotNull String createVariable(@NotNull String groupId,
                                   @Nullable String variableId,
                                   @NotNull String variableName,
                                   @NotNull VariableType variableType,
                                   @Nullable Consumer<VariableMetaBuilder> metaBuilder);

    @NotNull String createEnumVariable(@NotNull String groupId,
                                       @Nullable String variableId,
                                       @NotNull String variableName,
                                       @NotNull List<String> values,
                                       @Nullable Consumer<VariableMetaBuilder> metaBuilder);

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
     * Create group and attach it to parent group
     */
    boolean createGroup(@NotNull String parentGroupId, @NotNull String groupId, @NotNull String groupName, boolean locked,
                        @NotNull String icon, @NotNull String iconColor, @Nullable String description);

    /**
     * Remove group and all associated variables
     */
    boolean removeGroup(@NotNull String groupId);

    /**
     * Build full data source path to variable
     *
     * @param forSet - build data source for getting or setting value
     */
    @NotNull String buildDataSource(@NotNull String variableId, boolean forSet);

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
        Color(o -> {
            if (o instanceof String) {
                String str = o.toString();
                return (str.length() == 7 || str.length() == 9) && str.startsWith("#");
            }
            return false;
        }, false),
        Enum(o -> o instanceof String, null),
        Bool(o -> o instanceof Boolean, false),
        Float(o -> o instanceof Number, 0F);

        private final Predicate<Object> validate;
        private final Object defaultValue;
    }

    interface VariableMetaBuilder {
        VariableMetaBuilder setReadOnly(boolean value);

        VariableMetaBuilder setColor(String value);

        VariableMetaBuilder setDescription(String value);

        VariableMetaBuilder setUnit(String value);

        VariableMetaBuilder setAttributes(List<String> attributes);
    }
}
