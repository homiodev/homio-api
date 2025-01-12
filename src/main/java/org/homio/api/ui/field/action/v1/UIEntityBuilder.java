package org.homio.api.ui.field.action.v1;

public interface UIEntityBuilder {

  String getEntityID();

  UIInputEntity buildEntity();
}
