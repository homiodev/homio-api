package org.homio.bundle.api.converter.bean;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import lombok.extern.log4j.Log4j2;
import org.homio.bundle.api.util.ApplicationContextHolder;

@Log4j2
public class SpringBeanDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        String beanName = jp.getText();
        try {
            return ApplicationContextHolder.getBean(beanName);
        } catch (Exception ex) {
            log.warn("Unable to find bean: {}", beanName);
            return null;
        }
    }
}
