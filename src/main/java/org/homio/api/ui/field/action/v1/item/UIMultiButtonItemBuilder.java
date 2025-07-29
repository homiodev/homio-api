package org.homio.api.ui.field.action.v1.item;

import java.util.Collection;
import org.homio.api.model.Icon;
import org.homio.api.model.OptionModel;
import org.homio.api.ui.field.action.v1.UIEntityItemBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UIMultiButtonItemBuilder
    extends UIEntityItemBuilder<UIMultiButtonItemBuilder, String> {

  default @NotNull UIMultiButtonItemBuilder addButton(@NotNull String key, @Nullable String title) {
    return addButton(key, title, null);
  }

  @NotNull
  UIMultiButtonItemBuilder addButton(
      @NotNull String key, @Nullable String title, @Nullable Icon icon);

  default @NotNull UIMultiButtonItemBuilder addButton(@NotNull OptionModel optionModel) {
    Icon icon =
        optionModel.getIcon() == null
            ? null
            : new Icon(optionModel.getIcon(), optionModel.getColor());
    return addButton(optionModel.getKey(), optionModel.getTitle(), icon);
  }

  default @NotNull UIMultiButtonItemBuilder addButtons(
      @NotNull Collection<OptionModel> optionModels) {
    for (OptionModel optionModel : optionModels) {
      addButton(optionModel);
    }
    return this;
  }

  @NotNull
  UIMultiButtonItemBuilder setActive(@NotNull String activeButtonKey);
}
