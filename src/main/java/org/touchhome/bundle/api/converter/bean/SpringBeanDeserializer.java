package org.touchhome.bundle.api.converter.bean;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.log4j.Log4j2;
import org.touchhome.bundle.api.util.ApplicationContextHolder;

import java.io.IOException;

@Log4j2
public class SpringBeanDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        String beanName = jp.getText();
        try {
            return ApplicationContextHolder.getBean(beanName);
        } catch (Exception ex) {
            log.warn("Unable to fund bean with name: {}", beanName);
            return null;
        }
    }
}
