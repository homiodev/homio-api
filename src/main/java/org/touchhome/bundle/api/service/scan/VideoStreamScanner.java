package org.touchhome.bundle.api.service.scan;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.service.scan.ItemDiscoverySupport;
import org.touchhome.common.util.Lang;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface VideoStreamScanner extends ItemDiscoverySupport {

    default void handleDevice(String headerConfirmButtonKey, String key, String name, EntityContext entityContext,
                              Consumer<List<String>> messageConsumer, Runnable saveHandler) {
        List<String> messages = new ArrayList<>();
        messages.add(Lang.getServerMessage("VIDEO_STREAM.NEW_DEVICE_QUESTION"));
        messageConsumer.accept(messages);
        entityContext.ui().sendConfirmation("Confirm-Camera-" + key,
                Lang.getServerMessage("NEW_DEVICE.TITLE", "NAME", name),
                saveHandler, messages, headerConfirmButtonKey);
    }
}
