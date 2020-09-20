package org.touchhome.bundle.api.optionProvider;

import org.touchhome.bundle.api.DynamicOptionLoader;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.json.Option;

import java.util.List;

public class SelectSerialPortOptionLoader implements DynamicOptionLoader {

    @Override
    public List<Option> loadOptions(Object parameter, EntityContext entityContext) {
        return Option.listOfPorts();
    }
}
