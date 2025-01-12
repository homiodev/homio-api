package org.homio.api.entity;

/**
 * Able to ordering entities
 */
public interface HasOrder extends HasJsonData {

  default int getOrder() {
    return getJsonData("order", -1);
  }

  default void setOrder(int value) {
    setJsonData("order", value);
  }

  default boolean enableUiOrdering() {
    return false;
  }
}
