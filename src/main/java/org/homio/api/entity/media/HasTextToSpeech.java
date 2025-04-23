package org.homio.api.entity.media;

import org.homio.api.entity.BaseEntityIdentifier;
import org.homio.api.entity.HasJsonData;
import org.homio.api.entity.HasStatusAndMsg;
import org.homio.api.service.EntityService;
import org.homio.api.service.TextToSpeechEntityService;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldProgress;

public interface HasTextToSpeech<T extends TextToSpeechEntityService<?>>
    extends EntityService<T>, HasJsonData, BaseEntityIdentifier, HasStatusAndMsg {

  @UIField(order = 40, hideInEdit = true)
  @UIFieldProgress(color = "#8f8359")
  default UIFieldProgress.Progress getUsedQuota() {
    try {
      int current = getService().getSynthesizedCharacters();
      return UIFieldProgress.Progress.of(current, getMaxCharactersQuota());
    } catch (Exception ignored) {
    }
    return null;
  }

  @UIField(order = 40, hideInView = true)
  default int getMaxCharactersQuota() {
    return getJsonData("quota", 1000000);
  }

  default void setMaxCharactersQuota(int value) {
    setJsonData("quota", value);
  }

  @UIField(order = 45)
  default boolean isDisableTranslateAfterQuota() {
    return getJsonData("disOverQuota", true);
  }

  default void setDisableTranslateAfterQuota(boolean value) {
    setJsonData("disOverQuota", value);
  }
}
