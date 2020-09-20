package org.touchhome.bundle.api.converter.serial;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fazecast.jSerialComm.SerialPort;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class SerialPortDeserializer extends JsonDeserializer<SerialPort> {

    @Override
    public SerialPort deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        String systemPortName = jp.getText();
        if (systemPortName == null) {
            return null;
        }
        try {
            return SerialPort.getCommPort(systemPortName);
        } catch (Exception ex) {
            log.warn("Unable to find serial port: {}", systemPortName);
            return null;
        }
    }
}
