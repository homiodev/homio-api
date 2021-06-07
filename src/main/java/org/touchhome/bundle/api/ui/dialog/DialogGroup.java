package org.touchhome.bundle.api.ui.dialog;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.touchhome.bundle.api.ui.field.action.ActionInputParameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@RequiredArgsConstructor
public class DialogGroup {
    private final String name;
    private String borderColor;
    private List<ActionInputParameter> inputs = new ArrayList<>();

    public DialogGroup input(ActionInputParameter inputParameter) {
        inputs.add(inputParameter);
        return this;
    }

    public DialogGroup inputs(Collection<ActionInputParameter> inputParameters) {
        inputs.addAll(inputParameters);
        return this;
    }
}
