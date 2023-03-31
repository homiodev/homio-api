package org.homio.bundle.api.converter.serial;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

@Log4j2
public class SerialPortDeserializer extends JsonDeserializer<SerialPort> {

    public static SerialPort getSerialPort(String systemPortName) {
        if (StringUtils.isEmpty(systemPortName)) {
            return null;
        }
        try {
            return SerialPort.getCommPort(systemPortName);
        } catch (Exception ex) {
            log.warn("Unable to find serial port: {}", systemPortName);
            return null;
        }
    }

    @Override
    public SerialPort deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        return getSerialPort(jp.getText());
    }
}
