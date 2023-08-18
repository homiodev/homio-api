package org.homio.api.service.scan;

import org.homio.api.EntityContext;
import org.homio.api.util.Lang;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface VideoStreamScanner extends ItemDiscoverySupport {

    default void handleDevice(String headerConfirmButtonKey, String key, String name, EntityContext entityContext,
                              Consumer<List<String>> messageConsumer, Runnable saveHandler) {
        List<String> messages = new ArrayList<>();
        messages.add(Lang.getServerMessage("VIDEO_STREAM.NEW_DEVICE_QUESTION"));
        messageConsumer.accept(messages);
        entityContext.ui().sendConfirmation("Confirm-Video-" + key,
                Lang.getServerMessage("NEW_DEVICE.TITLE", name),
                saveHandler, messages, headerConfirmButtonKey);
    }
}
