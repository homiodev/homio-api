package org.touchhome.bundle.api.workspace.scratch;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.workspace.WorkspaceBlock;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@Getter
public class Scratch3Block implements Comparable<Scratch3Block> {
    public static final String CONDITION = "CONDITION";

    @JsonIgnore
    private int order;
    private String opcode;
    private BlockType blockType;
    Object text;
    private Map<String, ArgumentTypeDescription> arguments = new HashMap<>();
    @JsonIgnore
    private Scratch3BlockHandler handler;
    @JsonIgnore
    private Scratch3BlockEvaluateHandler evaluateHandler;
    @JsonIgnore
    private int spaceCount = 0;

    private Scratch3Color scratch3Color;

    private BiConsumer<String, WorkspaceBlock> allowLinkBoolean;
    private BiConsumer<String, WorkspaceBlock> allowLinkVariable;

    @JsonIgnore
    private LinkGeneratorHandler linkGenerator;

    protected Scratch3Block(int order, String opcode, BlockType blockType, Object text, Scratch3BlockHandler handler, Scratch3BlockEvaluateHandler evaluateHandler) {
        this.order = order;
        this.opcode = opcode;
        this.blockType = blockType;
        this.text = text;
        this.handler = handler;
        this.evaluateHandler = evaluateHandler;
    }

    public static Scratch3ConditionalBlock ofConditional(int order, String opcode, String text, Scratch3BlockHandler handler) {
        return new Scratch3ConditionalBlock(order, opcode, BlockType.conditional, text, handler, null);
    }

    public static Scratch3Block ofHat(int order, String opcode, String text, Scratch3BlockHandler handler) {
        return new Scratch3Block(order, opcode, BlockType.hat, text, handler, null);
    }

    public static Scratch3Block ofHandler(int order, String opcode, BlockType blockType, String text, Scratch3BlockHandler handler) {
        return new Scratch3Block(order, opcode, blockType, text, handler, null);
    }

    public static Scratch3Block ofCommand(int order, String opcode, String text, Scratch3BlockHandler handler) {
        return ofHandler(order, opcode, BlockType.command, text, handler);
    }

    @SneakyThrows
    public static <T extends Scratch3Block> T ofHandler(int order, String opcode, BlockType blockType, String text, Scratch3BlockHandler handler, Class<T> targetClass) {
        Constructor<T> constructor = targetClass.getDeclaredConstructor(int.class, String.class, BlockType.class, String.class, Scratch3BlockHandler.class, Scratch3BlockEvaluateHandler.class);
        return constructor.newInstance(order, opcode, blockType, text, handler, null);
    }

    public static Scratch3Block ofHandler(String opcode, BlockType blockType, Scratch3BlockHandler handler) {
        return new Scratch3Block(0, opcode, blockType, null, handler, null);
    }

    public static Scratch3Block ofReporter(int order, String opcode, String text, Scratch3BlockEvaluateHandler evalHandler) {
        return new Scratch3Block(order, opcode, BlockType.reporter, text, null, evalHandler);
    }

    public static Scratch3Block ofBoolean(int order, String opcode, String text, Scratch3BlockEvaluateHandler evalHandler) {
        return new Scratch3Block(order, opcode, BlockType.Boolean, text, null, evalHandler);
    }

    @SneakyThrows
    public static <T extends Scratch3Block> T ofReporter(int order, String opcode,
                                                         String text, Scratch3BlockEvaluateHandler evalHandler, Class<T> targetClass) {
        Constructor<T> constructor = targetClass.getDeclaredConstructor(int.class, String.class, BlockType.class, String.class, Scratch3BlockHandler.class, Scratch3BlockEvaluateHandler.class);
        return constructor.newInstance(order, opcode, BlockType.reporter, text, null, evalHandler);
    }

    public static Scratch3Block ofReporter(String opcode, Scratch3BlockEvaluateHandler evalHandler) {
        return new Scratch3Block(0, opcode, BlockType.reporter, null, null, evalHandler);
    }

    public ArgumentTypeDescription addArgument(String argumentName, ArgumentType type) {
        return addArgument(argumentName, type, "");
    }

    public ArgumentTypeDescription addArgument(String argumentName, boolean defaultValue) {
        return addArgument(argumentName, ArgumentType.checkbox, Boolean.toString(defaultValue));
    }

    /**
     * Add argument with type string and default empty value
     */
    public ArgumentTypeDescription addArgument(String argumentName) {
        return addArgument(argumentName, "");
    }

    public ArgumentTypeDescription addArgument(String argumentName, String defaultValue) {
        return addArgument(argumentName, ArgumentType.string, defaultValue);
    }

    public ArgumentTypeDescription addArgument(String argumentName, Integer defaultValue) {
        return addArgument(argumentName, ArgumentType.number, String.valueOf(defaultValue));
    }

    public ArgumentTypeDescription addArgument(String argumentName, Float defaultValue) {
        return addArgument(argumentName, ArgumentType.number, String.valueOf(defaultValue));
    }

    public ArgumentTypeDescription addArgument(String argumentName, Double defaultValue) {
        return addArgument(argumentName, ArgumentType.number, String.valueOf(defaultValue));
    }

    public ArgumentTypeDescription addArgument(String argumentName, ArgumentType type, String defaultValue) {
        ArgumentTypeDescription argumentTypeDescription = new ArgumentTypeDescription(type, defaultValue, null);
        this.arguments.put(argumentName, argumentTypeDescription);
        return argumentTypeDescription;
    }

    public ArgumentTypeDescription addArgument(String argumentName, MenuBlock menu) {
        ArgumentTypeDescription argumentTypeDescription = new ArgumentTypeDescription(ArgumentType.string, menu);
        this.arguments.put(argumentName, argumentTypeDescription);
        return argumentTypeDescription;
    }

    @Override
    public int compareTo(@NotNull Scratch3Block o) {
        return Integer.compare(order, o.order);
    }

    public Pair<String, MenuBlock> findMenuBlock(Predicate<String> predicate) {
        for (String argument : arguments.keySet()) {
            if (predicate.test(argument)) {
                return Pair.of(argument, arguments.get(argument).getMenuBlock());
            }
        }
        return null;
    }

    public void appendSpace() {
        this.spaceCount++;
    }

    public void overrideColor(String color) {
        if (color != null) {
            this.scratch3Color = new Scratch3Color(color);
        }
    }

    public void allowLinkBoolean(BiConsumer<String, WorkspaceBlock> allowLinkBoolean) {
        this.allowLinkBoolean = allowLinkBoolean;
    }

    public void setLinkGenerator(LinkGeneratorHandler linkGenerator) {
        this.linkGenerator = linkGenerator;
    }

    public void allowLinkFloatVariable(BiConsumer<String, WorkspaceBlock> allowLinkVariable) {
        this.allowLinkVariable = allowLinkVariable;
    }

    public WorkspaceCodeGenerator codeGenerator(String extension) {
        return new WorkspaceCodeGenerator(extension);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Scratch3Block that = (Scratch3Block) o;
        return opcode.equals(that.opcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opcode);
    }

    public interface LinkGeneratorHandler {
        void handle(String varGroup, String varName, JSONObject parameter) throws Exception;
    }

    @FunctionalInterface
    public interface Scratch3BlockHandler {
        void handle(WorkspaceBlock workspaceBlock) throws Exception;
    }

    @FunctionalInterface
    public interface Scratch3BlockEvaluateHandler {
        Object handle(WorkspaceBlock workspaceBlock) throws Exception;
    }

    @Getter
    @RequiredArgsConstructor
    public static class ArgumentTypeDescription {
        private final ArgumentType type;
        private final String defaultValue;
        private final String menu;

        @JsonIgnore
        private final MenuBlock menuBlock;

        ArgumentTypeDescription(ArgumentType type, String defaultValue, MenuBlock menuBlock) {
            this.type = type;
            this.defaultValue = defaultValue;
            if (menuBlock != null) {
                this.menu = menuBlock.getName();
                this.menuBlock = menuBlock;
            } else {
                this.menu = null;
                this.menuBlock = null;
            }
        }

        ArgumentTypeDescription(ArgumentType type, MenuBlock menuBlock) {
            this.type = type;
            this.menu = menuBlock.getName();
            this.menuBlock = menuBlock;
            this.defaultValue = null;
        }

        public String getDefaultValue() {
            if (defaultValue != null) {
                return defaultValue;
            }
            if (menuBlock != null) {
                Object defaultValue = menuBlock.getDefaultValue();
                return defaultValue instanceof Enum ? ((Enum) defaultValue).name() : defaultValue.toString();
            }
            return null;
        }
    }

    @RequiredArgsConstructor
    public class WorkspaceCodeGenerator {

        private final Map<String, Object> menuValues = new HashMap<>();
        private final String extension;

        public WorkspaceCodeGenerator setMenu(MenuBlock menuBlock, Object value) {
            menuValues.put(menuBlock.getName(), value);
            return this;
        }

        public void generateBooleanLink(String varGroup, String varName, EntityContext entityContext) {
            getLinkCodeGenerator(entityContext).generateBooleanLink(varGroup, varName);
        }

        public void generateFloatLink(String varGroup, String varName, EntityContext entityContext) {
            getLinkCodeGenerator(entityContext).generateFloatLink(varGroup, varName);
        }

        private LinkCodeGenerator getLinkCodeGenerator(EntityContext entityContext) {
            return new LinkCodeGenerator(extension, getOpcode(), entityContext, menuValues, getArguments());
        }
    }
}
