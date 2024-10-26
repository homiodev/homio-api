package org.homio.api.ui.field;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.homio.api.ContextWidget;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldStringTemplate {

    boolean allowSuffix() default true;

    boolean allowPrefix() default true;

    boolean allowValueClickShowHistoryOption() default false;

    @Getter
    @Setter
    @Accessors(chain = true)
    class StringTemplate {
        private String nvt;

        // value
        private String p;
        private String s;

        // font size
        private Double vfs;
        private Double pfs;
        private Double sfs;

        // color
        private String vc;
        private String pc;
        private String sc;

        // icon
        private String vi;
        private String pi;
        private String si;

        // icon color
        private String vic;
        private String pic;
        private String sic;

        // vertical align
        private ContextWidget.VerticalAlign va;
        private ContextWidget.VerticalAlign pa;
        private ContextWidget.VerticalAlign sa;

        // fire history dialog on value click
        private Boolean hoc;
    }
}
