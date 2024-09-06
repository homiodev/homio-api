package org.homio.api.workspace.scratch;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.homio.api.entity.EntityFieldMetadata;
import org.homio.api.state.State;
import org.homio.api.util.CommonUtils;
import org.homio.api.workspace.WorkspaceBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static org.homio.api.util.JsonUtils.OBJECT_MAPPER;
import static org.homio.api.workspace.scratch.Scratch3ExtensionBlocks.SETTING;

@SuppressWarnings("ALL")
@Getter
public class Scratch3Block implements Comparable<Scratch3Block> {

    @JsonIgnore
    private final int order;
    private final String opcode;
    private final BlockType blockType;
    private final Map<String, ArgumentTypeDescription> arguments = new HashMap<>();
    @JsonIgnore
    private final Scratch3BlockHandler handler;
    @JsonIgnore
    private final Scratch3BlockEvaluateHandler evaluateHandler;
    @NotNull
    Object text;
    @JsonIgnore
    private int spaceCount = 0;

    private Scratch3Color scratch3Color;

    protected Scratch3Block(int order,
                            @NotNull String opcode,
                            @NotNull BlockType blockType,
                            @NotNull Object text,
                            @Nullable Scratch3BlockHandler handler,
                            @Nullable Scratch3BlockEvaluateHandler evaluateHandler) {
        this.order = order;
        this.opcode = opcode;
        this.blockType = blockType;
        this.text = text;
        this.handler = handler;
        this.evaluateHandler = evaluateHandler;
    }

    public @NotNull ArgumentTypeDescription addArgument(@NotNull String argumentName, @NotNull ArgumentType type) {
        return addArgument(argumentName, type, "");
    }

    public @NotNull ArgumentTypeDescription addArgument(@NotNull String argumentName, boolean defaultValue) {
        return addArgument(argumentName, ArgumentType.checkbox, Boolean.toString(defaultValue));
    }

    /**
     * @param argumentName -
     * @return Add argument with type string and default empty value
     */
    public @NotNull ArgumentTypeDescription addArgument(@NotNull String argumentName) {
        return addArgument(argumentName, "");
    }

    public @NotNull ArgumentTypeDescription addArgument(@NotNull String argumentName, @NotNull String defaultValue) {
        return addArgument(argumentName, ArgumentType.string, defaultValue);
    }

    public @NotNull ArgumentTypeDescription addArgument(@NotNull String argumentName, @NotNull Integer defaultValue) {
        return addArgument(argumentName, ArgumentType.number, String.valueOf(defaultValue));
    }

    public @NotNull ArgumentTypeDescription addArgument(@NotNull String argumentName, @NotNull Long defaultValue) {
        return addArgument(argumentName, ArgumentType.number, String.valueOf(defaultValue));
    }

    public @NotNull ArgumentTypeDescription addArgument(@NotNull String argumentName, @NotNull Float defaultValue) {
        return addArgument(argumentName, ArgumentType.number, String.valueOf(defaultValue));
    }

    public @NotNull ArgumentTypeDescription addArgument(@NotNull String argumentName, @NotNull Double defaultValue) {
        return addArgument(argumentName, ArgumentType.number, String.valueOf(defaultValue));
    }

    @SneakyThrows
    public @NotNull <T extends ScratchSettingBaseEntity> ArgumentTypeDescription addSetting(@NotNull Class<T> settingClass) {
        return addSetting(CommonUtils.newInstance(settingClass));
    }

    @SneakyThrows
    public @NotNull <T extends ScratchSettingBaseEntity> ArgumentTypeDescription addSetting(@NotNull T entity) {
        if (StringUtils.isEmpty(entity.getTitle())) {
            throw new IllegalArgumentException("Setting class has to have non null title method");
        }

        return addArgument(SETTING, ArgumentType.setting, OBJECT_MAPPER.writeValueAsString(entity));
    }

    public @NotNull ArgumentTypeDescription addArgument(@NotNull String argumentName, @NotNull ArgumentType type, @NotNull String defaultValue) {
        ArgumentTypeDescription argumentTypeDescription = new ArgumentTypeDescription(type, defaultValue, null);
        this.arguments.put(argumentName, argumentTypeDescription);
        return argumentTypeDescription;
    }

    public @NotNull ArgumentTypeDescription addArgument(@NotNull String argumentName, @NotNull MenuBlock menu) {
        ArgumentTypeDescription argumentTypeDescription = new ArgumentTypeDescription(ArgumentType.string, menu);
        this.arguments.put(argumentName, argumentTypeDescription);
        return argumentTypeDescription;
    }

    @Override
    public int compareTo(@NotNull Scratch3Block o) {
        return Integer.compare(order, o.order);
    }

    public @Nullable Pair<String, MenuBlock> findMenuBlock(@NotNull Predicate<String> predicate) {
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

    public void overrideColor(@Nullable String color) {
        if (color != null) {
            this.scratch3Color = new Scratch3Color(color);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Scratch3Block that = (Scratch3Block) o;
        return opcode.equals(that.opcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opcode);
    }

    @FunctionalInterface
    public interface Scratch3BlockHandler {

        void handle(@NotNull WorkspaceBlock workspaceBlock) throws Exception;
    }

    @FunctionalInterface
    public interface Scratch3BlockEvaluateHandler {

        State handle(@NotNull WorkspaceBlock workspaceBlock) throws Exception;
    }

    public interface ScratchSettingBaseEntity extends EntityFieldMetadata {

        @Override
        // requires to instantiate entity for fetch select boxes
        default String getEntityID() {
            return getClass().getName();
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class ArgumentTypeDescription {

        private final @NotNull ArgumentType type;
        private final @Nullable String defaultValue;
        private final @Nullable String menu;
        private final @NotNull JSONObject metadata = new JSONObject();

        @JsonIgnore
        private final MenuBlock menuBlock;

        ArgumentTypeDescription(@NotNull ArgumentType type, @Nullable String defaultValue, @Nullable MenuBlock menuBlock) {
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

        ArgumentTypeDescription(@NotNull ArgumentType type, @NotNull MenuBlock menuBlock) {
            this.type = type;
            this.menu = menuBlock.getName();
            this.menuBlock = menuBlock;
            this.defaultValue = null;
        }

        public @Nullable String getDefaultValue() {
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
