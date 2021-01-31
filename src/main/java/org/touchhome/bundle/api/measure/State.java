package org.touchhome.bundle.api.measure;

public interface State {

    float floatValue();

    int intValue();

    default String toFullString() {
        return stringValue();
    }

    default long longValue() {
        return intValue();
    }

    boolean boolValue();

    default String stringValue() {
        return toString();
    }

    default <T extends State> T as(Class<T> target) {
        if (target != null && target.isInstance(this)) {
            return target.cast(this);
        } else {
            return null;
        }
    }
}
