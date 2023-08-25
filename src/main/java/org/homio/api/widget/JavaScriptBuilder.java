package org.homio.api.widget;

import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import org.homio.api.EntityContext;
import org.json.JSONObject;

public interface JavaScriptBuilder {

    static String wrapIntoString(String value) {
        return "\"'\"+" + value + "+\"'\"";
    }

    static String buildBind(String bindKey) {
        return "{{" + bindKey + "}}";
    }

    void rawContent(String content);

    JavaScriptBuilder css(String className, String... values);

    void setJsonReadOnly();

    JsMethod js(String methodName, String... params);

    JSContent jsContent();

    void wsHandler(String[] params, Consumer<JsMethod> jsMethodConsumer);

    JsMethod beforeFunc();

    JsMethod readyOnClient();

    JavaScriptBuilder jsonParam(String key, Object value);

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

    interface JSWindow extends JSONParameterContext {

    }

    interface EvaluableValue extends Supplier<String> {

    }

    interface ProxyEntityContextValue {

        void apply(EntityContext entityContext);
    }

    interface JSONParameterContext {

        JSONParameter obj(String name);

        JSONParameter array(String name);
    }

    interface JSONParameter {

        JSONParameter obj(String key);

        JSONParameter array(String key);

        JSONParameter value(String key, String value);

        JSONParameter value(String key, Consumer<JSONObject> consumer);

        JSONParameter value(String key, EvaluableValue value);

        JSONParameter value(String key, ProxyEntityContextValue proxyEntityContextValue);

        String toString(int indentFactor);
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
