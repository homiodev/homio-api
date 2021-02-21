package org.touchhome.bundle.api.ui.field.action.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.touchhome.bundle.api.ui.field.action.ActionInputParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Getter
@Setter
@Log4j2
@Accessors(chain = true)
public class DynamicContextMenuAction implements Comparable<DynamicContextMenuAction> {
    private final String name;
    private final int order;
    private String icon;
    private String iconColor;
    private JSONObject metadata;
    private List<ActionInputParameter> parameters = new ArrayList<>();

    @JsonIgnore
    private final Consumer<JSONObject> action;

    public DynamicContextMenuAction(String name, int order, Consumer<JSONObject> action) {
        this.name = name;
        this.order = order;
        this.action = action;
    }

    public void addInput(ActionInputParameter parameter) {
        this.parameters.add(parameter);
    }

    @Override
    public int compareTo(DynamicContextMenuAction other) {
        return Integer.compare(this.order, other.order);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return name.equals(((DynamicContextMenuAction) o).name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
