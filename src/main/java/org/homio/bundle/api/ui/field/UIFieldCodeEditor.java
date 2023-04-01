package org.homio.bundle.api.ui.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldCodeEditor {

    MonacoLanguage editorType() default MonacoLanguage.PlainText;

    /**
     * @return  Link to another field that holds current editor type
     */
    String editorTypeRef() default "";

    boolean autoFormat() default false;

    boolean wordWrap() default false;
}
