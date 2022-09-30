package org.touchhome.bundle.api.workspace.scratch;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.touchhome.bundle.api.BundleEntryPoint;
import org.touchhome.bundle.api.EntityContext;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

@Getter
public abstract class Scratch3ExtensionBlocks {

    public static final String EVENT = "EVENT";
    public static final String VALUE = "VALUE";
    public static final String ENTITY = "ENTITY";

    protected final EntityContext entityContext;
    private final String id;
    private final List<Scratch3Block> blocks = new ArrayList<>();
    private final Map<String, MenuBlock> menus = new HashMap<>();
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

    public Scratch3ExtensionBlocks(String color, EntityContext entityContext, BundleEntryPoint bundleEntryPoint) {
        this(color, entityContext, bundleEntryPoint, null);
    }

    @SneakyThrows
    public Scratch3ExtensionBlocks(String color, EntityContext entityContext, BundleEntryPoint bundleEntryPoint,
                                   String idSuffix) {
        this.id = bundleEntryPoint == null ? idSuffix : bundleEntryPoint.getBundleId() + (idSuffix == null ? "" : "-" + idSuffix);
        this.parent = bundleEntryPoint == null ? null : bundleEntryPoint.getBundleId();
        this.entityContext = entityContext;
        if (color != null) {
            URL resource = getImage(bundleEntryPoint);
            if (resource == null) {
                throw new IllegalArgumentException("Unable to find Scratch3 image: " + this.id + ".png in classpath");
            }
            this.blockIconURI = "data:image/png;base64," +
                    Base64.getEncoder().encodeToString(IOUtils.toByteArray(Objects.requireNonNull(resource)));
            this.scratch3Color = new Scratch3Color(color);
        }
    }

    /**
     * Uses only in app
     */
    public Scratch3ExtensionBlocks(String id, EntityContext entityContext) {
        this(null, entityContext, null, id);
    }

    private URL getImage(BundleEntryPoint bundleEntryPoint) {
        URL resource = null;
        if (bundleEntryPoint != null) {
            resource = bundleEntryPoint.getResource(this.id + ".png");
            if (resource == null) {
                resource = bundleEntryPoint.getResource("image/" + this.id + ".png");
            }
            if (resource == null) {
                resource = bundleEntryPoint.getResource("image.png");
            }
        }
        if (resource == null) {
            resource = getClass().getClassLoader().getResource(this.id + ".png");
            if (resource == null) {
                resource = getClass().getClassLoader().getResource("image/" + this.id + ".png");
            }
            if (resource == null) {
                resource = getClass().getClassLoader().getResource("image.png");
            }
        }
        return resource;
    }

    public void init() {
        if (blocksMap.isEmpty()) {
            this.postConstruct();
        }
    }

    @SneakyThrows
    private void postConstruct() {
        assembleFields(this);
        postUpdateBlocks(blocks);
    }

    public void postConstruct(List<Scratch3Block> blocks, Map<String, MenuBlock> menus) {
        this.blocks.clear();
        this.menus.clear();
        this.blocksMap.clear();

        this.blocks.addAll(blocks);
        this.menus.putAll(menus);
        postUpdateBlocks(blocks);
    }

    private void postUpdateBlocks(List<Scratch3Block> blocks) {
        Collections.sort(blocks);
        for (Scratch3Block block : blocks) {
            if (blocksMap.put(block.getOpcode(), block) != null) {
                throw new RuntimeException("Found multiple blocks with same opcode: " + block.getOpcode());
            }
        }
    }

    private void assembleFields(Object extensionObject) throws IllegalAccessException {
        for (Field field : FieldUtils.getAllFields(extensionObject.getClass())) {
            if (InlineExtensionBlocks.class.isAssignableFrom(field.getType())) {
                assembleFields(FieldUtils.readField(field, extensionObject, true));
            }
            if (Scratch3Block.class.isAssignableFrom(field.getType())) {
                blocks.add((Scratch3Block) FieldUtils.readField(field, extensionObject, true));
            } else if (MenuBlock.class.isAssignableFrom(field.getType())) {
                MenuBlock menuBlock = (MenuBlock) FieldUtils.readField(field, extensionObject, true);
                menus.put(menuBlock.getName(), menuBlock);
            }
        }
    }
}
