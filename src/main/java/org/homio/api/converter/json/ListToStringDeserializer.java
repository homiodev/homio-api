package org.homio.api.converter.json;

import static org.homio.api.entity.HasJsonData.LIST_DELIMITER;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Custom converter in case when getter is array but setter is string Need in case when we update widget and need live reload widget
 */
public class ListToStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        if (node instanceof ArrayNode) {
            return StreamSupport.stream(node.spliterator(), false)
                                .map(JsonNode::asText).collect(Collectors.joining(LIST_DELIMITER));
        }
        return node.asText();
    }
}
