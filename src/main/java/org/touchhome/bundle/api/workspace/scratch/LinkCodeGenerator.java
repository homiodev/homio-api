package org.touchhome.bundle.api.workspace.scratch;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.EntityContextVar;
import org.touchhome.bundle.api.workspace.WorkspaceEntity;

import java.util.Map;

@RequiredArgsConstructor
class LinkCodeGenerator {

    private final String extension;
    private final String opcode;
    private final EntityContext entityContext;
    private final Map<String, Object> menuValues;
    private final Map<String, Scratch3Block.ArgumentTypeDescription> arguments;

    private String generateID() {
        return RandomStringUtils.random(20, true, true);
    }

    void generateFloatLink(String varGroup, String varName) {
        generateLink(varGroup, varName, EntityContextVar.VariableType.Float,
                "data_group_variable_link");
    }

    void generateBooleanLink(String varGroup, String varName) {
        generateLink(varGroup, varName, EntityContextVar.VariableType.Boolean,
                "data_boolean_link");
    }

    private void generateLink(String varGroup, String varName, EntityContextVar.VariableType variableType,
                              String variableOpcode) {
        WorkspaceEntity workspaceEntity = entityContext.getEntityByName("links", WorkspaceEntity.class);
        if (workspaceEntity == null) {
            workspaceEntity = entityContext.save(new WorkspaceEntity().setName("links"));
        }
        JSONObject content = new JSONObject(StringUtils.defaultIfEmpty(workspaceEntity.getContent(), "{}"));
        JSONObject blocks = get("blocks", get("target", content));

        String variableId = generateID();
        String bodyID = generateID();
        blocks.put(bodyID, this.generateCode(blocks, variableId, bodyID));

        JSONObject variableObject = generateVariable(bodyID, varGroup, varName, calcPosition(blocks),
                variableOpcode, variableType);

        blocks.put(variableId, variableObject);

        entityContext.save(workspaceEntity.setContent(content.toString()));
    }

    private int calcPosition(JSONObject blocks) {
        int y = 0;
        for (String key : blocks.keySet()) {
            y = Math.max(y, blocks.getJSONObject(key).optInt("y"));
        }
        y += 60;
        return y;
    }

    // TODO: not tested
    private JSONObject generateVariable(String sourceID, String groupId, String varName, int y,
                                        String linkName, EntityContextVar.VariableType variableType) {
        entityContext.var().createGroup(groupId, groupId);
        String variableId = entityContext.var().createVariable(groupId, varName, variableType);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("opcode", linkName).put("topLevel", true).put("shadow", false).put("x", 0).put("y", y);
        jsonObject.put("inputs", new JSONObject().put("SOURCE", new JSONArray().put(2).put(sourceID)));
        jsonObject.put("fields", new JSONObject()
                .put("group_variables_group", new JSONArray().put(varName).put(variableId))
                .put("group_variables", new JSONArray().put(groupId).put(groupId))
        );
        return jsonObject;
    }

    private JSONObject generateCode(JSONObject blocks, String parent, String bodyID) {
        return new JSONObject()
                .put("opcode", this.extension + "_" + this.opcode)
                .put("parent", parent)
                .put("shadow", false)
                .put("topLevel", false)
                .put("inputs", generateInputs(blocks, bodyID));
    }

    private JSONObject generateInputs(JSONObject blocks, String parentBodyID) {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, Scratch3Block.ArgumentTypeDescription> entry : arguments.entrySet()) {
            String inputID = generateID();
            jsonObject.put(entry.getKey(), new JSONArray().put(1).put(inputID));
            JSONObject menuJSON = generateMenu(entry.getValue(), parentBodyID);
            blocks.put(inputID, menuJSON);
        }
        return jsonObject;
    }

    private JSONObject generateMenu(Scratch3Block.ArgumentTypeDescription value, String parentBodyID) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("opcode", extension + "_menu_" + value.getMenuBlock().getName());
        jsonObject.put("parent", parentBodyID);
        jsonObject.put("shadow", true).put("topLevel", false);
        Object mv = menuValues.get(value.getMenu());
        if (mv == null) {
            throw new IllegalStateException("Unable to find menu value");
        }
        jsonObject.put("fields", new JSONObject().put(value.getMenu(), new JSONArray().put(mv)));
        return jsonObject;
    }

    private JSONObject get(String name, JSONObject parent) {
        if (!parent.has(name)) {
            parent.put(name, new JSONObject());
        }
        return parent.getJSONObject(name);
    }
}
