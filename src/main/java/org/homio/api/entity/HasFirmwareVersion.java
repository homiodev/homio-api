package org.homio.api.entity;

import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldGroup;

/**
 * Interface for entities that has specific program to execute and has version
 */
public interface HasFirmwareVersion {

    @UIField(order = 8, hideInEdit = true)
    @UIFieldGroup(value = "GENERAL", order = 10)
    String getFirmwareVersion();
}

