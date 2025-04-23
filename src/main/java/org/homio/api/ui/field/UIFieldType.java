package org.homio.api.ui.field;

import java.util.function.Function;
import lombok.Getter;
import org.homio.api.state.State;

@Getter
public enum UIFieldType {
    // Description type uses for showing text inside setting panel on whole width
    Description,
    SelectBoxButton,
    SelectBox,
    MultiSelectBox,

    // Just a text
    Text,
    HTML, // Draw as HTML
    Markdown, // Draw as Markdown

    // Button that fires server action
    Button,
    Toggle,
    Upload,
    TextInput,

    // Return type must be enum. Handle as buttons instead of select box
    EnumButtons,
    // must contains @UIFieldXXXSelection annotation
    EnumMultiButtons,

    IconPicker,
    ColorPicker,
    Chips,

    IpAddress,
    Password, // shows *** for users without admin rights

    Duration,
    DurationDowntime,
    StaticDate,

    String,

    // special type (default for detect a field type by java type)
    AutoDetect,

    // Slider with min/max/step parameters
    Slider(o -> {
        if (o instanceof State) {
            return ((State) o).intValue();
        }
        return java.lang.Integer.parseInt(o.toString());
    }),

    Float(o -> {
        if (o instanceof State) {
            return ((State) o).floatValue();
        }
        return java.lang.Float.parseFloat(o.toString());
    }),
    Boolean(o -> {
        if (o instanceof State) {
            return ((State) o).boolValue();
        }
        return java.lang.Boolean.parseBoolean(o.toString());

    }),
    // for integer, we may set metadata as min, max
    Integer(o -> {
        if (o instanceof State) {
            return ((State) o).intValue();
        }
        return java.lang.Integer.parseInt(o.toString());
    });

    private final Function<Object, Object> convertToObject;

    UIFieldType() {
        this.convertToObject = Object::toString;
    }

    UIFieldType(Function<Object, Object> convertToObject) {
        this.convertToObject = convertToObject;
    }
}
