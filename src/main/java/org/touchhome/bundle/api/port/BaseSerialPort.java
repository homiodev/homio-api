package org.touchhome.bundle.api.port;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.server.PortInUseException;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.util.TouchHomeUtils;
import org.touchhome.common.util.CommonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.fazecast.jSerialComm.SerialPort.TIMEOUT_NONBLOCKING;

@Log4j2
public abstract class BaseSerialPort implements SerialPortDataListener {
    protected final Object bufferSynchronisationObject = new Object();

    /**
     * The baud rate.
     */
    @Getter
    protected final int baudRate;

    /**
     * True to enable RTS / CTS flow control
     */
    protected final PortFlowControl flowControl;
    protected final String coordinator;
    protected final EntityContext entityContext;
    protected final Runnable portUnavailableListener;
    protected final Consumer<SerialPort> portOpenSuccessListener;
    /**
     * The portName portName.
     */
    @Getter
    protected SerialPort serialPort;
    /**
     * The serial port input stream.
     */
    protected InputStream inputStream;
    /**
     * The serial port output stream.
     */
    @Getter
    protected OutputStream outputStream;

    public BaseSerialPort(String coordinator,
                          EntityContext entityContext,
                          SerialPort serialPort,
                          int baudRate,
                          PortFlowControl flowControl,
                          Runnable portUnavailableListener,
                          Consumer<SerialPort> portOpenSuccessListener) {
        this.coordinator = coordinator;
        this.entityContext = entityContext;
        this.serialPort = serialPort;
        this.baudRate = baudRate;
        this.flowControl = flowControl;
        this.portUnavailableListener = portUnavailableListener;
        this.portOpenSuccessListener = portOpenSuccessListener;
    }

    public boolean open() {
        return open(baudRate, flowControl);
    }

    public boolean open(int baudRate) {
        return open(baudRate, flowControl);
    }

    public boolean open(int baudRate, PortFlowControl flowControl) {
        try {
            log.debug("Connecting to serial port [{}] at {} baud, flow control {}.",
                    serialPort == null ? "null" : serialPort.getSystemPortName(), baudRate, flowControl);
            try {
                if (serialPort == null) {
                    serialPort = Stream.of(SerialPort.getCommPorts()).filter(p -> p.getPortDescription().toLowerCase().contains(coordinator)).findAny().orElse(null);

                    if (serialPort == null) {
                        log.error("Serial Error: Port does not exist.");
                        return false;
                    } else if (portOpenSuccessListener != null) {
                        portOpenSuccessListener.accept(serialPort);
                    }
                }
                switch (flowControl) {
                    case FLOWCONTROL_OUT_NONE:
                        serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
                        break;
                    case FLOWCONTROL_OUT_RTSCTS:
                        serialPort.setFlowControl(SerialPort.FLOW_CONTROL_RTS_ENABLED);
                        break;
                    case FLOWCONTROL_OUT_XONOFF:
                        serialPort.setFlowControl(SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED);
                        break;
                    default:
                        break;
                }

                serialPort.setComPortTimeouts(TIMEOUT_NONBLOCKING, 100, 0);
                serialPort.addDataListener(this);

                log.debug("Serial port [{}] is initialized.", serialPort.getSystemPortName());
                serialPort.openPort();
            } catch (PortInUseException e) {
                log.error("Serial Error: Port {} in use.", serialPort.getSystemPortName());
                return false;
            } catch (RuntimeException e) {
                log.error("Serial Error: Device cannot be opened on Port {}. Caused by {}",
                        serialPort == null ? "UNKNOWN_SYSTEM_PORT" : serialPort.getSystemPortName(), e.getMessage());
                return false;
            }

            inputStream = this.serialPort.getInputStream();
            outputStream = this.serialPort.getOutputStream();

            return true;
        } catch (Exception e) {
            log.error("Unable to open serial port: ", e);
            return false;
        }
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
            try {
                synchronized (bufferSynchronisationObject) {
                    int available = inputStream.available();
                    log.trace("Processing DATA_AVAILABLE event: have {} bytes available", available);
                    byte buf[] = new byte[available];
                    int offset = 0;
                    while (offset != available) {
                        if (log.isTraceEnabled()) {
                            log.trace("Processing DATA_AVAILABLE event: try read  {} at offset {}",
                                    available - offset, offset);
                        }
                        int n = inputStream.read(buf, offset, available - offset);
                        if (log.isTraceEnabled()) {
                            log.trace("Processing DATA_AVAILABLE event: did read {} of {} at offset {}", n,
                                    available - offset, offset);
                        }
                        if (n <= 0) {
                            throw new IOException("Expected to be able to read " + available
                                    + " bytes, but saw error after " + offset);
                        }
                        offset += n;
                    }
                    this.handleSerialEvent(buf);
                }
            } catch (IOException e) {
                log.warn("Processing DATA_AVAILABLE event: received IOException in serial port event", e);
            } catch (Exception ex) {
                log.warn("Port read exception: " + CommonUtils.getErrorMessage(ex));
                if (this.portUnavailableListener != null) {
                    this.portUnavailableListener.run();
                }
                return;
            }

            synchronized (this) {
                this.notify();
            }
        }
    }

    public void close() {
        String serialPortName = "";
        try {
            if (serialPort != null) {
                serialPortName = serialPort.getSystemPortName();
                serialPort.removeDataListener();

                outputStream.flush();

                inputStream.close();
                outputStream.close();

                serialPort.closePort();

                serialPort = null;
                inputStream = null;
                outputStream = null;

                synchronized (this) {
                    this.notify();
                }

                log.debug("Serial port '{}' closed.", serialPortName);
            }
        } catch (Exception e) {
            log.error("Error closing serial port: '{}' ", serialPortName, e);
        }
    }

    protected abstract void handleSerialEvent(byte[] buf);

    public void write(int value) {
        if (outputStream == null) {
            return;
        }
        try {
            outputStream.write(value);
        } catch (IOException ignore) {
        }
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }
}
