package org.homio.bundle.api.ui.field.selection;

/**
 * Interface that uses by beans that has to be exposes via @UIFieldBeanSelection(value = XXX.class) Some of beans may be hidden from UI
 */
public interface UIFieldSelectionCondition {

    default boolean isBeanVisibleForSelection() {
        return true;
    }
}
