package org.touchhome.bundle.api.ui;

import org.touchhome.bundle.api.Lang;
import org.touchhome.bundle.api.util.FlowMap;

public class ToastrException extends Exception {

    public ToastrException(String message) {
        super(Lang.getServerMessage(message));
    }

    public ToastrException(String message, FlowMap flowMap) {
        super(Lang.getServerMessage(message, flowMap));
    }

    public ToastrException(String message, String param0, String value0) {
        super(Lang.getServerMessage(message, param0, value0));
    }
}
