package org.touchhome.bundle.api.ui.field;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.touchhome.bundle.api.state.State;

import java.util.function.Function;

@Getter
@AllArgsConstructor
public enum UIFieldType {
    // Description type uses for showing text inside setting panel on whole width
    Description(Object::toString),
    SelectBoxButton(Object::toString),

    // Select box with options fetched from server
    SelectBoxDynamic(Object::toString),
    // Just a text
    Text(Object::toString),
    HTML(Object::toString), // Draw as html
    Markdown(Object::toString), // Draw as Markdown

    // Button that fires server action
    Button(Object::toString),
    Toggle(Object::toString),
    Info(Object::toString),
    Upload(Object::toString),
    TextArea(Object::toString),

    // Slider with min/max/step parameters
    Slider(o -> {
        if (o instanceof State) {
            return ((State) o).intValue();
        }
        return java.lang.Integer.parseInt(o.toString());
    }),

    IpAddress(Object::toString),
    Password(Object::toString), // shows *** for users without admin rights

    SelectBox(Object::toString),
    // Input text with additional button that able to fetch values from server
    TextSelectBoxDynamic(Object::toString), // text input type with ability to select values from server

    Float(o -> {
        if (o instanceof State) {
            return ((State) o).floatValue();
        }
        return java.lang.Float.parseFloat(o.toString());
    }),
    Duration(Object::toString),
    StaticDate(Object::toString),

    String(Object::toString),
    Boolean(o -> {
        if (o instanceof State) {
            return ((State) o).boolValue();
        }
        return java.lang.Boolean.parseBoolean(o.toString());

    }),
    // for integer we may set metadata as min, max
    Integer(o -> {
        if (o instanceof State) {
            return ((State) o).intValue();
        }
        return java.lang.Integer.parseInt(o.toString());
    }),
    // String type
    ColorPicker(Object::toString),
    // stores as String type with type 'string' or json inside
    // {color:'#FFFFFF', threshold: [{value:16,op:'>',color:'#DDAAEE'}]}
    ColorPickerWithThreshold(Object::toString),
    Chips(Object::toString), // https://material.angular.io/components/chips/examples
    // string with position matrix 3x3. i.e.: label position location, etc...
    // Available values:
    // 1x1 - Top Left
    // 1x2 - Top Center
    // 1x3 - Top Right
    // 2x1 - Middle Left
    // 2x2 - Middle Center
    // 2x3 - Middle Right
    // 3x1 - Bottom Left
    // 3x2 - Bottom Center
    // 3x3 - Bottom Right
    Position(Object::toString),

    // special type (default for detect field type by java type)
    AutoDetect(Object::toString);

    private final Function<Object, Object> convertToObject;
}
