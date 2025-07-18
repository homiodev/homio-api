package org.homio.api;

import com.pivovarit.function.ThrowingConsumer;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.homio.api.entity.device.DeviceBaseEntity;
import org.homio.api.entity.device.DeviceEndpointsBehaviourContract;
import org.homio.api.model.HasEntityIdentifier;
import org.homio.api.model.Icon;
import org.homio.api.model.JSON;
import org.homio.api.model.endpoint.BaseDeviceEndpoint;
import org.homio.api.model.endpoint.DeviceEndpoint;
import org.homio.api.state.State;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public interface ContextVar {

    String GROUP_HARDWARE = "hardware";
    String GROUP_BROADCAST = "broadcast";
    String GROUP_MISC = "misc";

    @NotNull
    Context context();

    default void onVariableCreated(@NotNull String discriminator, Consumer<Variable> variableListener) {
        onVariableCreated(discriminator, null, variableListener);
    }

    default void onVariableRemoved(@NotNull String discriminator, Consumer<Variable> variableListener) {
        onVariableRemoved(discriminator, null, variableListener);
    }

    void onVariableCreated(@NotNull String discriminator, @Nullable Pattern variableIdPattern, Consumer<Variable> variableListener);

    void onVariableRemoved(@NotNull String discriminator, @Nullable Pattern variableIdPattern, Consumer<Variable> variableListener);

    default void onVariableUpdated(@NotNull String discriminator, @NotNull String variableId, @NotNull Consumer<State> listener) {
        context().event().addEventListener(variableId, discriminator, listener);
    }

    /**
     * Every writable variable has to have exact one link listener which handle to write operation from UI, etc...
     *
     * @param variableId -
     * @param listener   -
     */
    void setLinkListener(@NotNull String variableId, @NotNull ThrowingConsumer<Object, Exception> listener);

    @Nullable
    Object getRawValue(@NotNull String variableId);

    default @Nullable State getValue(@NotNull String variableId) {
        return State.of(getRawValue(variableId));
    }

    /**
     * Push new value in queue.
     *
     * @param variableId - variable id
     * @param value      - value may be State type, primitive, or String/Number/Boolean/etc... ignore setting if value is null
     * @return converted value that has been stored into queue
     * @throws IllegalArgumentException if value doesn't validatet agains VariableType restriction
     */
    default @Nullable Object set(@NotNull String variableId, @Nullable Object value) throws IllegalArgumentException {
        return set(variableId, value, true);
    }

    @Nullable
    Object set(@NotNull String variableId, @Nullable Object value, boolean fireLinkListener) throws IllegalArgumentException;

    default @Nullable Object setIfNotMatch(@NotNull String variableId, @Nullable Object value) {
        if (!Objects.equals(getRawValue(variableId), value)) {
            return set(variableId, value);
        }
        return value;
    }

    default @Nullable Object setIfNotMatch(@NotNull String variableId, boolean value) {
        Object oldValue = getRawValue(variableId);
        if (!Objects.equals(oldValue, value)) {
            return set(variableId, value);
        }
        return oldValue;
    }

    default @Nullable Object setIfNotMatch(@NotNull String variableId, float value) {
        Object oldValue = getRawValue(variableId);
        if (!Objects.equals(oldValue, value)) {
            return set(variableId, value);
        }
        return oldValue;
    }

    default void inc(@NotNull String variableId, float value) {
        Object o = getRawValue(variableId);
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

    Set<Variable> getVariables();

    Variable getVariable(@NotNull String variableId);

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

    @NotNull
    Variable createVariable(@NotNull String groupId,
                            @Nullable String variableId,
                            @NotNull String variableName,
                            @NotNull VariableType variableType,
                            @Nullable Consumer<VariableMetaBuilder> metaBuilder);

    @NotNull
    Variable createTransformVariable(@NotNull String groupId,
                                     @Nullable String variableId,
                                     @NotNull String variableName,
                                     @NotNull VariableType variableType,
                                     @Nullable Consumer<TransformVariableMetaBuilder> metaBuilder);

    default @NotNull Variable createEnumVariable(@NotNull String groupId,
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

    String createGroup(@NotNull String groupId, @NotNull String groupName, @NotNull Consumer<GroupMetaBuilder> groupBuilder);

    /**
     * Create group and attach it to parent group
     *
     * @param parentGroupId -
     * @param groupId       -
     * @param groupName     -
     * @param groupBuilder  -
     * @return if group was create ot already exists
     */
    String createSubGroup(@NotNull String parentGroupId, @NotNull String groupId, @NotNull String groupName, @NotNull Consumer<GroupMetaBuilder> groupBuilder);

    /**
     * Remove group and all associated variables
     *
     * @param groupId -
     * @return if group was removed
     */
    boolean removeGroup(@NotNull String groupId);

    /**
     * Ignore if variable not exists
     * Allow remove variable if calls from 'owner' service. Also method owner has to have 'org.homio.addon' endpoint
     * Determine owner by stacktrace if owner is null
     */
    <T extends BaseDeviceEndpoint<?>> void removeVariable(@NotNull String variableId, @Nullable T owner);

    /**
     * Build full data source path to variable
     *
     * @param variableId - id
     * @return result
     */
    @NotNull
    String buildDataSource(@NotNull String variableId);

    @Getter
    @RequiredArgsConstructor
    enum VariableType {
        Any(o -> true, 0, DeviceEndpoint.EndpointType.string),
        Broadcast(o -> true, 0, DeviceEndpoint.EndpointType.string),
        Json(o -> {
            try {
                new JSONObject(o.toString());
                return true;
            } catch (Exception ex) {
                return false;
            }
        }, "{}", DeviceEndpoint.EndpointType.string),
        Color(o -> {
            if (o instanceof String) {
                String str = o.toString();
                return (str.length() == 7 || str.length() == 9) && str.startsWith("#");
            }
            return false;
        }, false, DeviceEndpoint.EndpointType.color),
        Enum(o -> o instanceof String, null, DeviceEndpoint.EndpointType.select),
        Text(o -> o instanceof String, null, DeviceEndpoint.EndpointType.string),
        Bool(o -> o instanceof Boolean, false, DeviceEndpoint.EndpointType.bool),
        Float(o -> o instanceof Number, 0F, DeviceEndpoint.EndpointType.number),
        Percentage(o -> o instanceof Number num && num.intValue() >= 0 && num.intValue() <= 100, 0F, DeviceEndpoint.EndpointType.number);

        private final Predicate<Object> validate;
        private final Object defaultValue;
        private final DeviceEndpoint.EndpointType endpointType;
    }

    interface VariableMetaBuilder extends GeneralVariableMetaBuilder {

        /**
         * Is it possible to write to variable from UI. Default: false
         *
         * @param value writable or not
         * @return this
         */
        @NotNull
        VariableMetaBuilder setWritable(boolean value);

        /**
         * Set enum values. Useful only for Enum variable type
         *
         * @param values list of available options
         */
        @NotNull
        VariableMetaBuilder setValues(@NotNull Set<String> values);

        @NotNull
        VariableMetaBuilder setRange(float min, float max);

        @NotNull
        VariableMetaBuilder setStep(float step);

        @NotNull
        VariableMetaBuilder setDescription(@Nullable String description);

        @NotNull
        VariableMetaBuilder setOwner(@NotNull DeviceBaseEntity owner);

        @NotNull
        VariableMetaBuilder setOwner(@NotNull DeviceEndpointsBehaviourContract owner);
    }

    interface TransformVariableMetaBuilder extends GeneralVariableMetaBuilder {

        @NotNull
        TransformVariableMetaBuilder setSourceVariables(@NotNull List<TransformVariableSource> sources);

        @NotNull
        TransformVariableMetaBuilder setTransformCode(@NotNull String code);
    }

    interface GeneralVariableMetaBuilder {

        /**
         * Is it variable store to db
         *
         * @param days how much days to keep in db
         */
        // Default: 0 days
        @NotNull
        GeneralVariableMetaBuilder setPersistent(int days);

        // Does aggregate values if more than 1 value in minute
        // Default: true
        @NotNull
        GeneralVariableMetaBuilder setPersistentAggregateValues(boolean value);

        @NotNull
        GeneralVariableMetaBuilder setQuota(int value);

        // is disable to delete entity
        @NotNull
        GeneralVariableMetaBuilder setLocked(boolean locked);

        @NotNull
        GeneralVariableMetaBuilder setColor(@Nullable String value);

        @NotNull
        GeneralVariableMetaBuilder setDescription(@Nullable String value);

        @NotNull
        GeneralVariableMetaBuilder setUnit(@Nullable String value);

        @NotNull
        GeneralVariableMetaBuilder setNumberRange(float min, float max);

        @NotNull
        GeneralVariableMetaBuilder setIcon(@Nullable Icon icon);

        @NotNull
        GeneralVariableMetaBuilder setDecimalScale(int scale);

        @NotNull
        GeneralVariableMetaBuilder setAttributes(@Nullable List<String> attributes);

        /**
         * Assign any info to variable
         *
         * @param key   - key
         * @param value - value
         * @return - this
         */
        @NotNull
        GeneralVariableMetaBuilder set(@NotNull String key, @NotNull String value);
    }

    interface GroupMetaBuilder {

        // is disable to delete entity
        @NotNull
        GroupMetaBuilder setLocked(boolean locked);

        @NotNull
        GroupMetaBuilder setDescription(@Nullable String value);

        @NotNull
        GroupMetaBuilder setIcon(@Nullable Icon icon);
    }

    interface Variable extends HasEntityIdentifier {

        @Override
        default @NotNull String getEntityID() {
            return getId();
        }

        @NotNull
        String getIcon();

        @NotNull
        String getIconColor();

        default @NotNull Icon getIconModel() {
            return new Icon(getIcon(), getIconColor());
        }

        @NotNull
        String getId();

        @NotNull
        String getName();

        @Nullable
        Object getRawValue();

        default @NotNull State getValue() {
            return State.of(getRawValue());
        }

        @NotNull
        JSON getJsonData();

        void set(@Nullable Object value);

        double getMinValue(double defValue);

        double getMaxValue(double defValue);

        double getStep(double defValue);

        boolean isWritable();

        @Nullable
        String getUnit();

        @Nullable
        String getDescription();

        @NotNull
        VariableType getRestriction();

        default boolean isPercentType() {
            return getMinValue(-1) == 0 && getMaxValue(-1) == 100;
        }

        // set or remove(if value is null) listener
        void addListener(@NotNull String key, @NotNull ThrowingConsumer<State, Exception> callback);

        void removeListener(@NotNull String key);
    }

    @Getter
    @Setter
    class TransformVariableSource {
        private @NotNull String type;
        private @Nullable String value;
        private @Nullable String meta;
    }
}
