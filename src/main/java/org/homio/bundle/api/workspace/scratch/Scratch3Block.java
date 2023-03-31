package org.homio.bundle.api.workspace.scratch;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.homio.bundle.api.state.State;
import org.homio.bundle.api.workspace.WorkspaceBlock;
import org.jetbrains.annotations.NotNull;

@Getter
public class Scratch3Block implements Comparable<Scratch3Block> {
    public static final String CONDITION = "CONDITION";
    Object text;
    @JsonIgnore
    private final int order;
    private final String opcode;
    private final BlockType blockType;
    private final Map<String, ArgumentTypeDescription> arguments = new HashMap<>();
    @JsonIgnore
    private final Scratch3BlockHandler handler;
    @JsonIgnore
    private final Scratch3BlockEvaluateHandler evaluateHandler;
    @JsonIgnore
    private int spaceCount = 0;

    private Scratch3Color scratch3Color;

    protected Scratch3Block(int order, String opcode, BlockType blockType, Object text, Scratch3BlockHandler handler,
                            Scratch3BlockEvaluateHandler evaluateHandler) {
        this.order = order;
        this.opcode = opcode;
        this.blockType = blockType;
        this.text = text;
        this.handler = handler;
        this.evaluateHandler = evaluateHandler;
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

    @FunctionalInterface
    public interface Scratch3BlockHandler {
        void handle(WorkspaceBlock workspaceBlock) throws Exception;
    }

    @FunctionalInterface
    public interface Scratch3BlockEvaluateHandler {
        State handle(WorkspaceBlock workspaceBlock) throws Exception;
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
}
