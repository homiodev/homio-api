package org.homio.api.converter.serial;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation uses for jackson serialization / deserialization for serial port types.
 * Uses in conjunctions with SerialPortConverter class i.e.:
 *
 * JsonSerialPort
 * Convert(converter = SerialPortConverter.class) -- uses for save/get with DB
 * UIFieldBeanSelection -- uses for selection on ui
 * private SerialPort somePort;
 */
@JacksonAnnotationsInside
@JsonSerialize(using = SerialPortSerializer.class)
@JsonDeserialize(using = SerialPortDeserializer.class)
@Target(value = {ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonSerialPort {
}
