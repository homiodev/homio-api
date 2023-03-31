package org.homio.bundle.api.converter.bean;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Map;
import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.util.ApplicationContextHolder;

public class SpringBeanSerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Map<String, ?> beans = ApplicationContextHolder.getBean(EntityContext.class).getBeansOfTypeWithBeanName(value.getClass());
        if (beans.isEmpty()) {
            throw new IllegalArgumentException("Unable to find bean with name: " + value.getClass());
        } else if (beans.size() > 1) {
            throw new IllegalArgumentException("Found multiple beans with name: " + value.getClass());
        }
        gen.writeString(beans.keySet().iterator().next());
    }
}
