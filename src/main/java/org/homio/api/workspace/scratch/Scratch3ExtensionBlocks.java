package org.homio.api.workspace.scratch;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.homio.api.AddonEntrypoint;
import org.homio.api.Context;
import org.homio.api.entity.BaseEntityIdentifier;
import org.homio.api.model.OptionModel.KeyValueEnum;
import org.homio.api.util.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "rawtypes", "SameParameterValue", "unchecked"})
@Getter
public abstract class Scratch3ExtensionBlocks {

  public static final String EVENT = "EVENT";
  public static final String VALUE = "VALUE";
  public static final String ENTITY = "ENTITY";
  public static final String CONDITION = "CONDITION";
  public static final String SETTING = "SETTING";

  protected final @NotNull Context context;
  private final @NotNull String id;
  private final @NotNull Map<String, MenuBlock> menus = new HashMap<>();
  private final @NotNull List<Scratch3Block> blocks = new ArrayList<>();
  private final @NotNull Map<String, Scratch3Block> blocksMap = new HashMap<>();

  @Setter private String name;
  private String blockIconURI;
  private Scratch3Color scratch3Color;

  /** Uses for grouping extensions inside select box */
  @Setter private ScratchParent parent;

  public Scratch3ExtensionBlocks(
      @Nullable String color, @NotNull Context context, @Nullable AddonEntrypoint addonEntryPoint) {
    this(color, context, addonEntryPoint, null);
  }

  @SneakyThrows
  public Scratch3ExtensionBlocks(
      @Nullable String color,
      @NotNull Context context,
      @Nullable AddonEntrypoint addonEntryPoint,
      @Nullable String idSuffix) {
    this.id =
        addonEntryPoint == null
            ? Objects.requireNonNull(idSuffix)
            : addonEntryPoint.getAddonID() + (idSuffix == null ? "" : "-" + idSuffix);
    this.context = context;
    if (color != null) {
      URL resource = getImage(addonEntryPoint);
      if (resource == null) {
        throw new IllegalArgumentException(
            "Unable to find Scratch3 image: " + this.id + ".png in classpath");
      }
      this.blockIconURI =
          "data:image/png;base64,"
              + Base64.getEncoder()
                  .encodeToString(IOUtils.toByteArray(Objects.requireNonNull(resource)));
      this.scratch3Color = new Scratch3Color(color);
    }
  }

  @SneakyThrows
  public Scratch3ExtensionBlocks(
      @NotNull String color,
      @NotNull Context context,
      @NotNull String id,
      @NotNull String name,
      @NotNull URL imageResource) {
    this.id = id;
    this.context = context;
    this.blockIconURI =
        "data:image/png;base64,"
            + Base64.getEncoder().encodeToString(IOUtils.toByteArray(imageResource));
    this.scratch3Color = new Scratch3Color(color);
  }

  // Uses only in app
  public Scratch3ExtensionBlocks(String id, @NotNull Context context) {
    this(null, context, null, id);
  }

  public void init() {}

  protected @NotNull Scratch3Block blockHat(
      int order,
      @NotNull String opcode,
      @NotNull String text,
      @NotNull Scratch3Block.Scratch3BlockHandler handler,
      @Nullable Consumer<Scratch3Block> configureHandler) {
    return addBlock(
        new Scratch3Block(order, opcode, BlockType.hat, text, handler, null), configureHandler);
  }

  protected @NotNull Scratch3Block blockHat(
      int order,
      @NotNull String opcode,
      @NotNull String text,
      @NotNull Scratch3Block.Scratch3BlockHandler handler) {
    return blockHat(order, opcode, text, handler, null);
  }

  protected @NotNull Scratch3Block blockHat(
      @NotNull String opcode, @NotNull Scratch3Block.Scratch3BlockHandler handler) {
    return blockHat(0, opcode, "", handler, null);
  }

  protected @NotNull Scratch3ConditionalBlock blockCondition(
      int order,
      @NotNull String opcode,
      @NotNull String text,
      @NotNull Scratch3Block.Scratch3BlockHandler handler,
      @Nullable Consumer<Scratch3ConditionalBlock> configureHandler) {
    return addBlock(
        new Scratch3ConditionalBlock(order, opcode, BlockType.conditional, text, handler, null),
        configureHandler);
  }

  protected @NotNull Scratch3ConditionalBlock blockCondition(
      int order,
      @NotNull String opcode,
      @NotNull String text,
      @NotNull Scratch3Block.Scratch3BlockHandler handler) {
    return blockCondition(order, opcode, text, handler, null);
  }

  protected @NotNull Scratch3Block blockCommand(
      int order,
      @NotNull String opcode,
      @NotNull String text,
      @NotNull Scratch3Block.Scratch3BlockHandler handler,
      @Nullable Consumer<Scratch3Block> configureHandler) {
    return addBlock(
        new Scratch3Block(order, opcode, BlockType.command, text, handler, null), configureHandler);
  }

  protected @NotNull Scratch3Block blockCommand(
      int order,
      @NotNull String opcode,
      @NotNull String text,
      @NotNull Scratch3Block.Scratch3BlockHandler handler) {
    return blockCommand(order, opcode, text, handler, null);
  }

  protected @NotNull Scratch3Block blockCommand(
      @NotNull String opcode, @NotNull Scratch3Block.Scratch3BlockHandler handler) {
    return blockCommand(0, opcode, "", handler, null);
  }

  protected @NotNull Scratch3Block blockReporter(
      int order,
      @NotNull String opcode,
      @NotNull String text,
      @NotNull Scratch3Block.Scratch3BlockEvaluateHandler evalHandler,
      @Nullable Consumer<Scratch3Block> configureHandler) {
    return addBlock(
        new Scratch3Block(order, opcode, BlockType.reporter, text, null, evalHandler),
        configureHandler);
  }

  protected @NotNull Scratch3Block blockReporter(
      int order,
      @NotNull String opcode,
      @NotNull String text,
      @NotNull Scratch3Block.Scratch3BlockEvaluateHandler evalHandler) {
    return blockReporter(order, opcode, text, evalHandler, null);
  }

  protected @NotNull Scratch3Block blockReporter(
      @NotNull String opcode, @NotNull Scratch3Block.Scratch3BlockEvaluateHandler evalHandler) {
    return blockReporter(0, opcode, "", evalHandler, null);
  }

  @SneakyThrows
  protected <T extends Scratch3Block> @NotNull T blockTargetReporter(
      int order,
      @NotNull String opcode,
      @NotNull String text,
      @NotNull Scratch3Block.Scratch3BlockEvaluateHandler evalHandler,
      @NotNull Class<T> targetClass,
      Consumer<T> configureHandler) {
    Constructor<T> constructor =
        targetClass.getDeclaredConstructor(
            int.class,
            String.class,
            BlockType.class,
            String.class,
            Scratch3Block.Scratch3BlockHandler.class,
            Scratch3Block.Scratch3BlockEvaluateHandler.class);
    T instance =
        constructor.newInstance(order, opcode, BlockType.reporter, text, null, evalHandler);
    return addBlock(instance, configureHandler);
  }

  protected @NotNull Scratch3Block blockBoolean(
      int order,
      @NotNull String opcode,
      @NotNull String text,
      @NotNull Scratch3Block.Scratch3BlockEvaluateHandler evalHandler,
      @Nullable Consumer<Scratch3Block> configureHandler) {
    return addBlock(
        new Scratch3Block(order, opcode, BlockType.Boolean, text, null, evalHandler),
        configureHandler);
  }

  protected @NotNull Scratch3Block blockBoolean(
      int order,
      @NotNull String opcode,
      @NotNull String text,
      @NotNull Scratch3Block.Scratch3BlockEvaluateHandler evalHandler) {
    return blockBoolean(order, opcode, text, evalHandler, null);
  }

  protected <T extends Enum> MenuBlock.StaticMenuBlock<T> menuStatic(
      @NotNull String name, @NotNull Class<T> enumClass, @NotNull T defaultValue) {
    return addMenu(
        new MenuBlock.StaticMenuBlock(name, null, enumClass)
            .addEnum(enumClass)
            .setDefaultValue(defaultValue));
  }

  protected <T extends Enum> MenuBlock.StaticMenuBlock<T> menuStatic(
      @NotNull String name,
      @NotNull Class<T> enumClass,
      @NotNull T defaultValue,
      @NotNull Predicate<T> filter) {
    return addMenu(
        new MenuBlock.StaticMenuBlock(name, null, enumClass)
            .addEnum(enumClass, filter)
            .setDefaultValue(defaultValue));
  }

  protected @NotNull MenuBlock.ServerMenuBlock menuServer(
      @NotNull String name,
      @NotNull String url,
      @NotNull String firstKey,
      @NotNull String firstValue,
      Integer... clusters) {
    return addMenu(new MenuBlock.ServerMenuBlock(name, url, firstKey, firstValue, clusters, true));
  }

  protected @NotNull MenuBlock.ServerMenuBlock menuServer(
      @NotNull String name, @NotNull String url, @NotNull String firstKey) {
    return menuServer(name, url, firstKey, "-");
  }

  protected @NotNull MenuBlock.ServerMenuBlock menuServer(
      @NotNull String name,
      @NotNull String url,
      @NotNull String firstKey,
      @NotNull String firstValue) {
    return addMenu(new MenuBlock.ServerMenuBlock(name, url, firstKey, firstValue, null, true));
  }

  @Deprecated
  protected @NotNull MenuBlock.ServerMenuBlock menuServerServiceItems(
      @NotNull String name, @NotNull Class<?> entityServiceClass, @NotNull String firstKey) {
    return addMenu(
        new MenuBlock.ServerMenuBlock(
            name,
            "rest/item/service/" + entityServiceClass.getSimpleName(),
            firstKey,
            "-",
            null,
            true));
  }

  protected @NotNull MenuBlock.ServerMenuBlock menuServerItems(
      @NotNull String name,
      @NotNull Class<? extends BaseEntityIdentifier> itemClass,
      @NotNull String firstKey) {
    return menuServerItems(name, itemClass, firstKey, "-");
  }

  protected @NotNull MenuBlock.ServerMenuBlock menuServerItems(
      @NotNull String name,
      @NotNull Class<? extends BaseEntityIdentifier> itemClass,
      @NotNull String firstKey,
      @NotNull String firstValue) {
    return addMenu(
        new MenuBlock.ServerMenuBlock(
            name,
            "rest/item/type/%s/options".formatted(itemClass.getSimpleName()),
            firstKey,
            firstValue,
            null,
            true));
  }

  protected <T extends KeyValueEnum> MenuBlock.StaticMenuBlock<T> menuStaticKV(
      @NotNull String name, @NotNull Class<T> enumClass, @Nullable T defaultValue) {
    return addMenu(
        new MenuBlock.StaticMenuBlock(name, null, enumClass)
            .addEnumKVE(enumClass)
            .setDefaultValue(defaultValue));
  }

  protected @NotNull MenuBlock.StaticMenuBlock<String> menuStaticList(
      @NotNull String name, @NotNull Map<String, String> items, @Nullable String defaultValue) {
    return addMenu(
        new MenuBlock.StaticMenuBlock(name, items, String.class).setDefaultValue(defaultValue));
  }

  protected @NotNull MenuBlock.ServerMenuBlock menuServerFiles(@Nullable String regexp) {
    return menuServer("FILE", Objects.toString(regexp, ".*"), "File").setUIDelimiter("/");
  }

  protected @NotNull MenuBlock.ServerMenuBlock menuServerFolders(@Nullable String regexp) {
    return menuServer("FOLDER", Objects.toString(regexp, ".*"), "Folder").setUIDelimiter("/");
  }

  private <T extends Scratch3Block> @NotNull T addBlock(
      @NotNull T scratch3Block, @Nullable Consumer<T> configureHandler) {
    if (blocksMap.containsKey(scratch3Block.getOpcode())) {
      throw new RuntimeException(
          "Found multiple blocks with same opcode: " + scratch3Block.getOpcode());
    }
    blocksMap.put(scratch3Block.getOpcode(), scratch3Block);
    blocks.add(scratch3Block);

    if (configureHandler != null) {
      configureHandler.accept(scratch3Block);
    }
    return scratch3Block;
  }

  private @Nullable URL getImage(@Nullable AddonEntrypoint addonEntryPoint) {
    URL resource = null;
    if (addonEntryPoint != null) {
      resource = addonEntryPoint.getResource("images/" + this.id + ".png");
      if (resource == null) {
        resource = addonEntryPoint.getResource("images/image.png");
      }
    }
    if (resource == null) {
      resource = CommonUtils.getClassLoader().getResource("images/" + this.id + ".png");
      if (resource == null) {
        resource = CommonUtils.getClassLoader().getResource("images/image.png");
      }
    }
    return resource;
  }

  private @NotNull <T extends MenuBlock> T addMenu(@NotNull T menuBlock) {
    if (this.menus.put(menuBlock.getName(), menuBlock) != null) {
      throw new RuntimeException("Found multiple menu with same name: " + menuBlock.getName());
    }
    return menuBlock;
  }

  public enum ScratchParent {
    storage,
    communication,
    media,
    hardware,
    ui,
    devices,
    net,
    misc,
    identity,
    fs,
    imageeditor,
    audio
  }
}
