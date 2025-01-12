package org.homio.api.port;

public enum PortFlowControl {
  /**
   * No flow control
   */
  FLOWCONTROL_OUT_NONE,
  /**
   * XOn / XOff (software) flow control
   */
  FLOWCONTROL_OUT_XONOFF,
  /**
   * RTS / CTS (hardware) flow control
   */
  FLOWCONTROL_OUT_RTSCTS
}
