package org.homio.api;

import com.pivovarit.function.ThrowingConsumer;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.homio.api.model.Icon;
import org.homio.api.state.State;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public interface EntityContextVar {

    @NotNull EntityContext getEntityContext();

    default void listen(@NotNull String key, @NotNull String variableId, @NotNull Consumer<State> listener) {
        getEntityContext().event().addEventListener(variableId, key, listener);
    }

    /**
     * Every writable variable has to have exact one link listener which handle to write operation from UI, etc...
     *
     * @param variableId -
     * @param listener   -
     */
    void setLinkListener(@NotNull String variableId, @NotNull ThrowingConsumer<Object, Exception> listener);

    @Nullable Object get(@NotNull String variableId);

    /**
     * Push new value in queue.
     *
     * @param variableId - variable id
     * @param value      - value may be State type, primitive, or String/Number/Boolean/etc... ignore setting if value is null
     * @return converted value that has been stored into queue
     * @throws IllegalArgumentException if value doesn't validatet agains VariableType restriction
     */
    @Nullable Object set(@NotNull String variableId, @Nullable Object value) throws IllegalArgumentException;

    default @Nullable Object setIfNotMatch(@NotNull String variableId, @Nullable Object value) {
        if (!Objects.equals(get(variableId), value)) {
            return set(variableId, value);
        }
        return value;
    }

    default @Nullable Object setIfNotMatch(@NotNull String variableId, boolean value) {
        Object oldValue = get(variableId);
        if (!Objects.equals(oldValue, value)) {
            return set(variableId, value);
        }
        return oldValue;
    }

    default @Nullable Object setIfNotMatch(@NotNull String variableId, float value) {
        Object oldValue = get(variableId);
        if (!Objects.equals(oldValue, value)) {
            return set(variableId, value);
        }
        return oldValue;
    }

    default void inc(@NotNull String variableId, float value) {
        Object o = get(variableId);
        if (Number.class.isAssignableFrom(o.getClass())) {
            set(variableId, ((Number) o).floatValue() + value);
        }
    }

    /**
     * Get variable title - name or defaultTitle
     *
     * @param variableId-
     * @param defaultTitle-
     * @return -
     */
    String getTitle(@NotNull String variableId, @Nullable String defaultTitle);

    /**
     * Return count of messages
     *
     * @param variableId -
     * @return count
     */
    long count(@NotNull String variableId);

    /**
     * Does variable exists in system
     *
     * @param variableId -
     * @return -
     */
    boolean exists(@NotNull String variableId);

    boolean existsGroup(@NotNull String groupId);

    boolean renameGroup(@NotNull String groupId, @NotNull String name, @Nullable String description);

    boolean renameVariable(@NotNull String variableId, @NotNull String name, @Nullable String description);

    boolean updateVariableIcon(@NotNull String variableId, @Nullable Icon icon);

    @NotNull String createVariable(@NotNull String groupId,
        @Nullable String variableId,
        @NotNull String variableName,
        @NotNull VariableType variableType,
        @Nullable Consumer<VariableMetaBuilder> metaBuilder);

    @NotNull String createTransformVariable(@NotNull String groupId,
        @Nullable String variableId,
        @NotNull String variableName,
        @NotNull VariableType variableType,
        @Nullable Consumer<TransformVariableMetaBuilder> metaBuilder);

    default @NotNull String createEnumVariable(@NotNull String groupId,
        @Nullable String variableId,
        @NotNull String variableName,
        @NotNull Set<String> values,
        @Nullable Consumer<VariableMetaBuilder> metaBuilder) {
        return createVariable(groupId, variableId, variableName, VariableType.Enum, builder -> {
            builder.setValues(values);
            if (metaBuilder != null) {
                metaBuilder.accept(builder);
            }
        });
    }

    boolean createGroup(@NotNull String groupId, @NotNull String groupName, @NotNull Consumer<GroupMetaBuilder> groupBuilder);

    /**
     * Create group and attach it to parent group
     *
     * @param parentGroupId -
     * @param groupId       -
     * @param groupName     -
     * @param groupBuilder  -
     * @return if group was create ot already exists
     */
    boolean createSubGroup(@NotNull String parentGroupId, @NotNull String groupId, @NotNull String groupName, @NotNull Consumer<GroupMetaBuilder> groupBuilder);

    /**
     * Remove group and all associated variables
     *
     * @param groupId -
     * @return if group was removed
     */
    boolean removeGroup(@NotNull String groupId);

    /**
     * Build full data source path to variable
     *
     * @param variableId - id
     * @return result
     */
    @NotNull String buildDataSource(@NotNull String variableId);

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

    interface VariableMetaBuilder extends GeneralVariableMetaBuilder {

        /**
         * Is it possible to write to variable from UI. Default: false
         *
         * @param value writable or not
         * @return this
         */
        @NotNull VariableMetaBuilder setWritable(boolean value);

        /**
         * Set enum values. Useful only for Enum variable type
         *
         * @param values list of available options
         */
        @NotNull VariableMetaBuilder setValues(Set<String> values);
    }

    interface TransformVariableMetaBuilder extends GeneralVariableMetaBuilder {

        @NotNull TransformVariableMetaBuilder setSourceVariables(@NotNull List<TransformVariableSource> sources);

        @NotNull TransformVariableMetaBuilder setTransformCode(@NotNull String code);
    }

    interface GeneralVariableMetaBuilder {

        /**
         * Is it variable store to db
         *
         * @param value store or not
         * @return this
         */
        @NotNull GeneralVariableMetaBuilder setPersistent(boolean value);

        @NotNull GeneralVariableMetaBuilder setQuota(int value);

        // is disable to delete entity
        @NotNull GeneralVariableMetaBuilder setLocked(boolean locked);

        @NotNull GeneralVariableMetaBuilder setColor(@Nullable String value);

        @NotNull GeneralVariableMetaBuilder setDescription(@Nullable String value);

        @NotNull GeneralVariableMetaBuilder setUnit(@Nullable String value);

        @NotNull GeneralVariableMetaBuilder setNumberRange(float min, float max);

        @NotNull GeneralVariableMetaBuilder setIcon(@Nullable Icon icon);

        @NotNull GeneralVariableMetaBuilder setAttributes(@Nullable List<String> attributes);
    }

    interface GroupMetaBuilder {

        // is disable to delete entity
        @NotNull GroupMetaBuilder setLocked(boolean locked);

        @NotNull GroupMetaBuilder setDescription(@Nullable String value);

        @NotNull GroupMetaBuilder setIcon(@Nullable Icon icon);
    }

    @Getter
    @Setter
    class TransformVariableSource {
        private @NotNull String type;
        private @Nullable String value;
        private @Nullable String meta;
    }
}
