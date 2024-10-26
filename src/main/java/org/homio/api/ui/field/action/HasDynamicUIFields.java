package org.homio.api.ui.field.action;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.homio.api.model.OptionModel;
import org.homio.api.model.UpdatableValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * For BaseItems that requires additional ui fields
 */
public interface HasDynamicUIFields {

    /**
     * Evaluates every time user fetch page to check if response class is changed
     *
     * @param uiFieldBuilder - ui field builder
     */
    void assembleUIFields(@NotNull UIFieldBuilder uiFieldBuilder);

    /**
     * We maye gather all fields inside this method to find UpdatableValue to write or assembleUIFields(...)
     *
     * @return - list of all UpdatableValue that assembles in 'assembleUIFields' func or null
     */
    @JsonIgnore
    default @Nullable List<UpdatableValue> getAllFields() {
        return null;
    }

    // fires when user save updated entity with
    default void writeDynamicFieldValue(@NotNull String key, @Nullable Object value) {
        AtomicReference<UpdatableValue> updatableValue = new AtomicReference<>(null);
        List<UpdatableValue> fields = getAllFields();
        if (fields != null) {
            updatableValue.set(fields.stream().filter(f -> f.getName().equals(key)).findAny().orElse(null));
        }
        if (updatableValue.get() == null) {
            assembleUIFields(new UIFieldBuilder() {
                @Override
                public @NotNull FieldBuilder addColorPicker(int order, @NotNull UpdatableValue<String> value) {
                    return fake(value);
                }

                @Override
                public @NotNull FieldBuilder addIconPicker(int order, @NotNull UpdatableValue<String> value) {
                    return fake(value);
                }

                @Override
                public @NotNull FieldBuilder addSwitch(int order, @NotNull UpdatableValue<Boolean> value) {
                    return fake(value);
                }

                @Override
                public @NotNull FieldBuilder addInput(int order, @NotNull UpdatableValue<String> value) {
                    return fake(value);
                }

                @Override
                public @NotNull FieldBuilder addSlider(int order, float min, float max, @Nullable String header, @NotNull UpdatableValue<Float> value) {
                    return fake(value);
                }

                @Override
                public @NotNull FieldBuilder addNumber(int order, @NotNull UpdatableValue<Integer> value) {
                    return fake(value);
                }

                @Override
                public @NotNull FieldBuilder addSelect(int order, @NotNull UpdatableValue<String> value, @NotNull List<OptionModel> selections) {
                    return fake(value);
                }

                @Override
                public @NotNull FieldBuilder addMultiSelect(int order, @NotNull UpdatableValue<String> value, @NotNull List<OptionModel> selections) {
                    return fake(value);
                }

                @Override
                public @NotNull FieldBuilder addChips(int order, @NotNull UpdatableValue<String> value) {
                    return fake(value);
                }

                private FieldBuilder fake(UpdatableValue value) {
                    if (value.getName().equals(key)) {
                        updatableValue.set(value);
                    }
                    return new FieldBuilder() {
                        @Override
                        public @NotNull FieldBuilder group(@NotNull String name, int order, @Nullable String borderColor) {
                            return this;
                        }

                        @Override
                        public @NotNull FieldBuilder hideInEdit(boolean value) {
                            return this;
                        }

                        @Override
                        public @NotNull FieldBuilder label(@Nullable String value) {
                            return this;
                        }

                        @Override
                        public @NotNull FieldBuilder hideInView(boolean value) {
                            return this;
                        }

                        @Override
                        public @NotNull FieldBuilder hideOnEmpty(boolean value) {
                            return this;
                        }

                        @Override
                        public @NotNull FieldBuilder inlineEdit(boolean value) {
                            return this;
                        }

                        @Override
                        public @NotNull FieldBuilder disableEdit(boolean value) {
                            return this;
                        }

                        @Override
                        public @NotNull FieldBuilder required(boolean value) {
                            return this;
                        }

                        @Override
                        public @NotNull FieldBuilder color(@Nullable String value) {
                            return this;
                        }

                        @Override
                        public @NotNull FieldBuilder background(@Nullable String value) {
                            return this;
                        }
                    };
                }
            });
        }
        if (updatableValue.get() == null) {
            throw new IllegalStateException("Unable to find dynamic UI field: " + key);
        }
        updatableValue.get().parseAndUpdate(value == null ? "" : value.toString());
    }

    interface UIFieldBuilder {

        @NotNull
        FieldBuilder addColorPicker(int order, @NotNull UpdatableValue<String> value);

        @NotNull
        FieldBuilder addIconPicker(int order, @NotNull UpdatableValue<String> value);

        @NotNull
        FieldBuilder addSwitch(int order, @NotNull UpdatableValue<Boolean> value);

        @NotNull
        FieldBuilder addInput(int order, @NotNull UpdatableValue<String> value);

        @NotNull
        FieldBuilder addSlider(int order, float min, float max,
                               @Nullable String header, @NotNull UpdatableValue<Float> value);

        @NotNull
        FieldBuilder addNumber(int order, @NotNull UpdatableValue<Integer> value);

        @NotNull
        FieldBuilder addSelect(int order, @NotNull UpdatableValue<String> value,
                               @NotNull List<OptionModel> selections);

        @NotNull
        FieldBuilder addMultiSelect(int order, @NotNull UpdatableValue<String> value,
                                    @NotNull List<OptionModel> selections);

        @NotNull
        FieldBuilder addChips(int order, @NotNull UpdatableValue<String> value);
    }

    interface FieldBuilder {

        @NotNull
        FieldBuilder group(@NotNull String name, int order, @Nullable String borderColor);

        @NotNull
        FieldBuilder hideInEdit(boolean value);

        @NotNull
        FieldBuilder label(@Nullable String value);

        @NotNull
        FieldBuilder hideInView(boolean value);

        @NotNull
        FieldBuilder hideOnEmpty(boolean value);

        @NotNull
        FieldBuilder inlineEdit(boolean value);

        @NotNull
        FieldBuilder disableEdit(boolean value);

        @NotNull
        FieldBuilder required(boolean value);

        @NotNull
        FieldBuilder color(@Nullable String value);

        @NotNull
        FieldBuilder background(@Nullable String value);
    }
}
