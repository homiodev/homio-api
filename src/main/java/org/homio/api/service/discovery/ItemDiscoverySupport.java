package org.homio.api.service.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.homio.api.Context;
import org.homio.api.util.Lang;
import org.homio.hquery.ProgressBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ItemDiscoverySupport {

  @NotNull
  String getName();

  @Nullable
  DeviceScannerResult scan(@NotNull Context context, @NotNull ProgressBar progressBar);

  default void handleDevice(
      String key,
      String name,
      Context context,
      Consumer<List<String>> messageConsumer,
      Runnable saveHandler) {
    List<String> messages = new ArrayList<>();
    messageConsumer.accept(messages);
    context
        .ui()
        .dialog()
        .sendConfirmation(
            "confirm-device-" + key,
            Lang.getServerMessage("NEW_DEVICE.TITLE", name),
            saveHandler,
            messages,
            "scan-devices");
  }

  @Getter
  @NoArgsConstructor
  class DeviceScannerResult {

    private final AtomicInteger existedCount = new AtomicInteger(0);
    private final AtomicInteger newCount = new AtomicInteger(0);

    public DeviceScannerResult(int existedCount, int newCount) {
      this.existedCount.set(existedCount);
      this.newCount.set(newCount);
    }
  }
}
