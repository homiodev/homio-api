package org.homio.bundle.api.converter.bean;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation uses for jackson serialization / deserialization for bean types.
 * Uses in conjunctions with JsonBeanConverter class i.e.:
 *
 * @JsonBean
 * @Convert(converter = JsonBeanConverter.class) -- uses for save/get with DB
 * @UIFieldBeanSelection -- uses for selection on ui
 * private SomeBeanInterface provider;
 */
@JacksonAnnotationsInside
@JsonSerialize(using = SpringBeanSerializer.class)
@JsonDeserialize(using = SpringBeanDeserializer.class)
@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonBean {
}
