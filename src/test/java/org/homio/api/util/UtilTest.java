package org.homio.api.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.homio.api.util.JsonUtils.OBJECT_MAPPER;
import static org.homio.api.util.JsonUtils.updateJsonPath;

public class UtilTest {

    @Test
    public void testUpdateJsonPath() {
        ObjectNode node = OBJECT_MAPPER.createObjectNode();
        Assertions.assertTrue(updateJsonPath(node, "active/timeout", 14));
        Assertions.assertFalse(updateJsonPath(node, "active/timeout", 14));
        Assertions.assertTrue(updateJsonPath(node, "active/timeout", 15));
    }
}
