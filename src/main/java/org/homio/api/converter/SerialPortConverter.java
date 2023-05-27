package org.homio.api.converter;

import com.fazecast.jSerialComm.SerialPort;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Converter(autoApply = true)
@RequiredArgsConstructor
public class SerialPortConverter implements AttributeConverter<SerialPort, String> {

    @Override
    public String convertToDatabaseColumn(SerialPort provider) {
        if (provider == null) {
            return null;
        }
        return provider.getSystemPortName();
    }

    @Override
    public SerialPort convertToEntityAttribute(String name) {
        if (name == null) {
            return null;
        }
        try {
            return SerialPort.getCommPort(name);
        } catch (Exception ex) {
            log.warn("Unable to find serial port with name: {}", name);
            return null;
        }
    }
}
