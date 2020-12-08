package org.touchhome.bundle.api.ui;

import org.touchhome.bundle.api.manager.En;
import org.touchhome.bundle.api.util.FlowMap;

public class ToastrException extends Exception {

    public ToastrException(String message) {
        super(En.getServerMessage(message));
    }

    public ToastrException(String message, FlowMap flowMap) {
        super(En.getServerMessage(message, flowMap));
    }

    public ToastrException(String message, String param0, String value0) {
        super(En.getServerMessage(message, param0, value0));
    }
}
