package org.homio.api.workspace.scratch;

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.homio.api.AddonEntrypoint;
import org.homio.api.EntityContext;
import org.homio.api.entity.BaseEntity;
import org.homio.api.model.OptionModel.KeyValueEnum;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class Scratch3ExtensionBlocks {

    public static final String EVENT = "EVENT";
    public static final String VALUE = "VALUE";
    public static final String ENTITY = "ENTITY";

    protected final EntityContext entityContext;
    private final String id;
    private final Map<String, MenuBlock> menus = new HashMap<>();
    private final List<Scratch3Block> blocks = new ArrayList<>();
    private final Map<String, Scratch3Block> blocksMap = new HashMap<>();

    @Setter
    private String name;
    private String blockIconURI;
    private Scratch3Color scratch3Color;

    /**
     * Uses for grouping extensions inside select box
     */
    @Setter
    private String parent;

    public Scratch3ExtensionBlocks(String color, EntityContext entityContext, AddonEntrypoint addonEntryPoint) {
        this(color, entityContext, addonEntryPoint, null);
    }

    @SneakyThrows
    public Scratch3ExtensionBlocks(String color, EntityContext entityContext, AddonEntrypoint addonEntryPoint,
                                   String idSuffix) {
        this.id = addonEntryPoint == null ? idSuffix : addonEntryPoint.getAddonID() + (idSuffix == null ? "" : "-" + idSuffix);
        this.parent = addonEntryPoint == null ? null : addonEntryPoint.getAddonID();
        this.entityContext = entityContext;
        if (color != null) {
            URL resource = getImage(addonEntryPoint);
            if (resource == null) {
                throw new IllegalArgumentException("Unable to find Scratch3 image: " + this.id + ".png in classpath");
            }
            this.blockIconURI = "data:image/png;base64," +
                    Base64.getEncoder().encodeToString(IOUtils.toByteArray(Objects.requireNonNull(resource)));
            this.scratch3Color = new Scratch3Color(color);
        }
    }

    @SneakyThrows
    public Scratch3ExtensionBlocks(@NotNull String color, @NotNull EntityContext entityContext, @NotNull String id,
                                   @NotNull String name, @NotNull URL imageResource) {
        this.id = id;
        this.entityContext = entityContext;
        this.blockIconURI = "data:image/png;base64," + Base64.getEncoder().encodeToString(IOUtils.toByteArray(imageResource));
        this.scratch3Color = new Scratch3Color(color);
    }

    // Uses only in app
    public Scratch3ExtensionBlocks(String id, EntityContext entityContext) {
        this(null, entityContext, null, id);
    }

    protected Scratch3Block blockHat(int order, String opcode, String text, Scratch3Block.Scratch3BlockHandler handler,
                                     Consumer<Scratch3Block> configureHandler) {
        return addBlock(new Scratch3Block(order, opcode, BlockType.hat, text, handler, null), configureHandler);
    }

    protected Scratch3Block blockHat(int order, String opcode, String text, Scratch3Block.Scratch3BlockHandler handler) {
        return blockHat(order, opcode, text, handler, null);
    }

    protected Scratch3Block blockHat(String opcode, Scratch3Block.Scratch3BlockHandler handler) {
        return blockHat(0, opcode, null, handler, null);
    }

    protected Scratch3ConditionalBlock blockCondition(int order, String opcode, String text,
                                                      Scratch3Block.Scratch3BlockHandler handler,
                                                      Consumer<Scratch3ConditionalBlock> configureHandler) {
        return addBlock(new Scratch3ConditionalBlock(order, opcode, BlockType.conditional, text, handler, null),
                configureHandler);
    }

    protected Scratch3ConditionalBlock blockCondition(int order, String opcode, String text,
                                                      Scratch3Block.Scratch3BlockHandler handler) {
        return blockCondition(order, opcode, text, handler, null);
    }

    protected Scratch3Block blockCommand(int order, String opcode, String text, Scratch3Block.Scratch3BlockHandler handler,
                                         Consumer<Scratch3Block> configureHandler) {
        return addBlock(new Scratch3Block(order, opcode, BlockType.command, text, handler, null),
                configureHandler);
    }

    protected Scratch3Block blockCommand(int order, String opcode, String text,
                                         Scratch3Block.Scratch3BlockHandler handler) {
        return blockCommand(order, opcode, text, handler, null);
    }

    protected Scratch3Block blockCommand(String opcode, Scratch3Block.Scratch3BlockHandler handler) {
        return blockCommand(0, opcode, null, handler, null);
    }

    protected Scratch3Block blockReporter(int order, String opcode, String text,
                                          Scratch3Block.Scratch3BlockEvaluateHandler evalHandler,
                                          Consumer<Scratch3Block> configureHandler) {
        return addBlock(new Scratch3Block(order, opcode, BlockType.reporter, text, null, evalHandler), configureHandler);
    }

    protected Scratch3Block blockReporter(int order, String opcode, String text,
                                          Scratch3Block.Scratch3BlockEvaluateHandler evalHandler) {
        return blockReporter(order, opcode, text, evalHandler, null);
    }

    protected Scratch3Block blockReporter(String opcode, Scratch3Block.Scratch3BlockEvaluateHandler evalHandler) {
        return blockReporter(0, opcode, null, evalHandler, null);
    }

    /*@SneakyThrows


    protected Scratch3Block ofReporter(String opcode, Scratch3Block.Scratch3BlockEvaluateHandler evalHandler) {
        return new Scratch3Block(0, opcode, BlockType.reporter, null, null, evalHandler);
    }
*/

    @SneakyThrows
    protected <T extends Scratch3Block> T blockTargetReporter(int order, String opcode, String text,
                                                              Scratch3Block.Scratch3BlockEvaluateHandler evalHandler,
                                                              Class<T> targetClass, Consumer<T> configureHandler) {
        Constructor<T> constructor = targetClass.getDeclaredConstructor(int.class, String.class, BlockType.class, String.class,
                Scratch3Block.Scratch3BlockHandler.class, Scratch3Block.Scratch3BlockEvaluateHandler.class);
        T instance = constructor.newInstance(order, opcode, BlockType.reporter, text, null, evalHandler);
        return addBlock(instance, configureHandler);
    }

    protected Scratch3Block blockBoolean(int order, String opcode, String text,
                                         Scratch3Block.Scratch3BlockEvaluateHandler evalHandler,
                                         Consumer<Scratch3Block> configureHandler) {
        return addBlock(new Scratch3Block(order, opcode, BlockType.Boolean, text, null, evalHandler), configureHandler);
    }

    protected Scratch3Block blockBoolean(int order, String opcode, String text,
                                         Scratch3Block.Scratch3BlockEvaluateHandler evalHandler) {
        return blockBoolean(order, opcode, text, evalHandler, null);
    }

    protected <T extends Enum> MenuBlock.StaticMenuBlock<T> menuStatic(String name, Class<T> enumClass, T defaultValue) {
        return addMenu(new MenuBlock.StaticMenuBlock(name, null, enumClass).addEnum(enumClass)
                .setDefaultValue(defaultValue));
    }

    protected <T extends Enum> MenuBlock.StaticMenuBlock<T> menuStatic(String name, Class<T> enumClass, T defaultValue,
                                                                       Predicate<T> filter) {
        return addMenu(
                new MenuBlock.StaticMenuBlock(name, null, enumClass).addEnum(enumClass, filter).setDefaultValue(defaultValue));
    }

    protected MenuBlock.ServerMenuBlock menuServer(String name, String url, String firstKey, String firstValue,
                                                   Integer... clusters) {
        return addMenu(new MenuBlock.ServerMenuBlock(name, url, firstKey, firstValue, clusters, true));
    }

    protected MenuBlock.ServerMenuBlock menuServer(String name, String url, String firstKey) {
        return menuServer(name, url, firstKey, "-");
    }

    protected MenuBlock.ServerMenuBlock menuServer(String name, String url, String firstKey, String firstValue) {
        return addMenu(new MenuBlock.ServerMenuBlock(name, url, firstKey, firstValue, null, true));
    }

    protected MenuBlock.ServerMenuBlock menuServerServiceItems(String name, Class<?> entityServiceClass, String firstKey) {
        return addMenu(new MenuBlock.ServerMenuBlock(name, "rest/item/service/" + entityServiceClass.getSimpleName(), firstKey,
                "-", null, true));
    }

    protected MenuBlock.ServerMenuBlock menuServerItems(String name, Class<? extends BaseEntity> itemClass, String firstKey) {
        return menuServerItems(name, itemClass, firstKey, "-");
    }

    protected MenuBlock.ServerMenuBlock menuServerItems(String name, Class<? extends BaseEntity> itemClass, String firstKey,
                                                        String firstValue) {
        return addMenu(
                new MenuBlock.ServerMenuBlock(name, "rest/item/type/" + itemClass.getSimpleName(), firstKey, firstValue, null,
                        true));
    }

    protected <T extends KeyValueEnum> MenuBlock.StaticMenuBlock<T> menuStaticKV(@NotNull String name, @NotNull Class<T> enumClass,
        T defaultValue) {
        return addMenu(new MenuBlock.StaticMenuBlock(name, null, enumClass).addEnumKVE(enumClass).setDefaultValue(defaultValue));
    }

    protected MenuBlock.StaticMenuBlock<String> menuStaticList(String name, Map<String, String> items, String defaultValue) {
        return addMenu(new MenuBlock.StaticMenuBlock(name, items, String.class).setDefaultValue(defaultValue));
    }

    protected MenuBlock.ServerMenuBlock menuServerFiles(@Nullable String regexp) {
        return menuServer("FILE", defaultString(regexp, ".*"), "File").setUIDelimiter("/");
    }

    protected MenuBlock.ServerMenuBlock menuServerFolders(@Nullable String regexp) {
        return menuServer("FOLDER", defaultString(regexp, ".*"), "Folder").setUIDelimiter("/");
    }

    private <T extends Scratch3Block> T addBlock(T scratch3Block, Consumer<T> configureHandler) {
        if (blocksMap.containsKey(scratch3Block.getOpcode())) {
            throw new RuntimeException("Found multiple blocks with same opcode: " + scratch3Block.getOpcode());
        }
        blocksMap.put(scratch3Block.getOpcode(), scratch3Block);
        blocks.add(scratch3Block);

        if (configureHandler != null) {
            configureHandler.accept(scratch3Block);
        }
        return scratch3Block;
    }

    private URL getImage(AddonEntrypoint addonEntryPoint) {
        URL resource = null;
        if (addonEntryPoint != null) {
            resource = addonEntryPoint.getResource("images/" + this.id + ".png");
            if (resource == null) {
                resource = addonEntryPoint.getResource("images/image.png");
            }
        }
        if (resource == null) {
            resource = getClass().getClassLoader().getResource("images/" + this.id + ".png");
            if (resource == null) {
                resource = getClass().getClassLoader().getResource("images/image.png");
            }
        }
        return resource;
    }

    public void init() {
    }

    private <T extends MenuBlock> T addMenu(T menuBlock) {
        if (this.menus.put(menuBlock.getName(), menuBlock) != null) {
            throw new RuntimeException("Found multiple menu with same name: " + menuBlock.getName());
        }
        return menuBlock;
    }
}
