package org.touchhome.bundle.api.widget;

import lombok.SneakyThrows;
import org.touchhome.bundle.api.ui.field.UIFieldType;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface JavaScriptBuilder {

    static String wrapIntoString(String value) {
        return "\"'\"+" + value + "+\"'\"";
    }

    static String buildBind(String bindKey) {
        return "{{" + bindKey + "}}";
    }

    void rawContent(String content);

    JavaScriptBuilder css(String className, String... values);

    JsMethod js(String methodName, String... params);

    JSContent jsContent();

    void wsHandler(String[] params, Consumer<JsMethod> jsMethodConsumer);

    JsMethod beforeFunc();

    JsMethod readyOnClient();

    JavaScriptBuilder jsonParam(String key, Object value);

    JavaScriptBuilder objectParam(String key, Object value, UIFieldType uiFieldType, int order);

    interface JSStyle {

        JSStyle clazz(String clazz);

        JSStyle style(String style);

        JSStyle ngIf(String condition);

        JSStyle id(String id);

        JSStyle ngStyleIf(String condition, String style, String value, String otherValue);

        JSStyle ngRepeat(String key, String value);

        JSStyle onClick(String content);

        JSStyle onClick(JsMethod jsMethod, String... params);

        JSStyle attr(String attr, String value);
    }

    interface JsCond extends Builder {

        JsCond eq(String cond1, String cond2, boolean escapeSecondCondition);

        JsCond bool(String cond);

        JsCond and();
    }

    interface JsCondBody extends Builder {

        void then(Consumer<JSCodeContext<JsCondBody>> jsCode);
    }

    interface JsMethod extends JSCodeContext<JsMethod> {

        void post(Consumer<JSAjaxPost> jsAjaxPostConsumer);

        @SneakyThrows
        void post(String request, String params);

        JsMethod clientJs(String clientCode);
    }

    interface IterContext extends Builder {

    }

    interface JSWindow {

        <T> T parameter(String name, T param);
    }

    interface JSCodeContext<T> extends Builder {

        JSCodeContext<T> raw(Supplier<String> jsStringBlock);

        void cond(Consumer<JsCond> jsCondConsumerContext, Consumer<JsCondBody> methodContext);

        void iter(String key, String array, Consumer<IterContext> iterContext);

        JSCodeContext<T> window(Consumer<JSWindow> jsWindowConsumer);

        JSCodeContext<T> addGlobalScript(String path);

        JSCodeContext<T> addGlobalLink(String path);
    }

    interface JSAjaxPost extends Builder {

        void param(String paramKey, String paramValue);
    }

    interface JSContent {

        JSContent add(JSInput jsInput);

        void div(Consumer<JSInput> div);

        void div(Consumer<JSStyle> jsStyle, Consumer<JSInput> div);

        void button(Consumer<JSStyle> jsStyle, Consumer<JSInput> button);
    }

    interface JSInput extends Builder {

        void div(Consumer<JSInput> jsInput);

        void div(Consumer<JSStyle> jsStyle, Consumer<JSInput> jsInput);

        void span(Consumer<JSInput> jsInput);

        void span(Consumer<JSStyle> jsStyle, Consumer<JSInput> jsInput);

        void ngLabel(Consumer<JSStyle> jsStyle, Consumer<JSInput> jsInput);

        JSInput innerHtml(String innerHtml);

        JSInput bind(String bindKey);
    }

    interface Builder {
        String build();
    }
}
