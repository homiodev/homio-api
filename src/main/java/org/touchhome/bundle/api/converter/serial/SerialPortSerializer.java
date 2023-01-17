package org.touchhome.bundle.api.converter.serial;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;

public class SerialPortSerializer extends JsonSerializer<SerialPort> {

    @Override
    public void serialize(SerialPort serialPort, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeString(serialPort.getSystemPortName());
    }
}
