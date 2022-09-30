package org.touchhome.bundle.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

import java.util.Objects;
import java.util.function.Predicate;

public interface EntityContextVar {

    EntityContext getEntityContext();

    Object get(String variableId);

    void set(String variableId, Object value);

    default void setIfNotMatch(String variableId, Object value) {
        if (!Objects.equals(get(variableId), value)) {
            set(variableId, value);
        }
    }

    default void setIfNotMatch(String variableId, boolean value) {
        if (!Objects.equals(get(variableId), value)) {
            set(variableId, value);
        }
    }

    default void setIfNotMatch(String variableId, float value) {
        if (!Objects.equals(get(variableId), value)) {
            set(variableId, value);
        }
    }

    default void inc(String variableId, float value) {
        Object o = get(variableId);
        if (Number.class.isAssignableFrom(o.getClass())) {
            set(variableId, ((Number) o).floatValue() + value);
        }
    }

    String getTitle(String variableId, String defaultTitle);

    /**
     * Return count of messages
     */
    long count(String variableId);

    /**
     * Does variable exists in system
     */
    boolean exists(String variableId);

    // return variable id
    String createVariable(String groupId, String variableName, VariableType variableType);

    /**
     * @return false if group already exists
     */
    boolean createGroup(String groupId, String groupName);

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
