package org.homio.api.model;

import static java.lang.String.format;
import static org.homio.api.entity.HasJsonData.LEVEL_DELIMITER;
import static org.homio.api.util.JsonUtils.OBJECT_MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fazecast.jSerialComm.SerialPort;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.Context;
import org.homio.api.entity.BaseEntity;
import org.homio.api.entity.HasStatusAndMsg;
import org.homio.api.fs.TreeNode;
import org.homio.api.ui.field.selection.SelectionConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OptionModel implements Comparable<OptionModel> {

    private static final Comparator<OptionModel> DEFAULT_COMPARATOR = (o1, o2) -> {
        if (o1.hasChildren()) {
            if (!o2.hasChildren()) {
                return -1;
            }
        } else if (o2.hasChildren()) {
            return 1;
        }
        return o1.getTitleOrKey().compareTo(o2.getTitleOrKey());
    };

    private final ObjectNode json = OBJECT_MAPPER.createObjectNode();

    private @Getter
    @NotNull String key;

    private @Setter
    @Nullable String title;

    private @Getter
    @Nullable String icon;

    private @Setter
    @Getter
    @Nullable String color;

    private @Getter
    @Nullable List<OptionModel> children;

    private @Getter
    @Nullable Status status;

    // disabled option is shown but not clickable
    private @Getter Boolean disabled;

    private OptionModel(@NotNull Object key, @Nullable Object title) {
        this.key = key.toString();
        this.title = title == null ? null : title.toString();
    }

    public static @NotNull OptionModel key(@NotNull String key) {
        return new OptionModel(key, null);
    }

    public static @NotNull OptionModel separator() {
        return OptionModel.key("~~~sep~~~");
    }

    // case when we are unable to show options on some circumstances
    public static @NotNull OptionModel error(String message) {
        return OptionModel.of("~~~err~~~", message);
    }

    public static @NotNull OptionModel of(@NotNull String key) {
        return new OptionModel(key, key);
    }

    public static @NotNull OptionModel of(@NotNull String key, @Nullable String title) {
        return new OptionModel(key, title);
    }

    public static @NotNull List<OptionModel> listWithEmpty(@NotNull Class<? extends KeyValueEnum> enumClass) {
        return withEmpty(list(enumClass));
    }

    public static @NotNull List<OptionModel> enumWithEmpty(@NotNull Class<? extends Enum> enumClass) {
        return withEmpty(enumList(enumClass));
    }

    public static @NotNull List<OptionModel> withEmpty(@NotNull List<OptionModel> list) {
        list.add(0, OptionModel.of("", "Empty"));
        return list;
    }

    public static @NotNull OptionModel of(@NotNull TreeNode item) {
        OptionModel model = OptionModel.of(Objects.toString(item.getId(), item.toString()),
                item.getName()).json(
                json -> json.put("dir", item.getAttributes().isDir())
                        .put("size", item.getAttributes().getSize())
                        .put("type", item.getAttributes().getType())
                        .put("empty", item.getAttributes().isEmpty())
                        .put("lastUpdated", item.getAttributes().getLastUpdated()));
        Collection<TreeNode> children = item.getChildren();
        if (children != null) {
            for (TreeNode child : children) {
                model.addChild(OptionModel.of(child));
            }
        }
        return model;
    }

    public static @NotNull List<OptionModel> listOfPorts(boolean withEmpty) {
        List<OptionModel> optionModels = Arrays.stream(SerialPort.getCommPorts()).map(p ->
                        new OptionModel(p.getSystemPortName(), p.getSystemPortName() + "/" + p.getDescriptivePortName()))
                .collect(Collectors.toList());
        return withEmpty ? withEmpty(optionModels) : optionModels;
    }

    public static @NotNull List<OptionModel> enumList(@NotNull Class<? extends Enum> enumClass) {
        return enumList(enumClass, null);
    }

    public static <T extends Enum<T>> @NotNull List<OptionModel> enumList(
            @NotNull Class<T> enumClass,
            @Nullable Predicate<T> filter) {
        return Stream.of(enumClass.getEnumConstants())
                .filter(e -> filter == null || filter.test(e))
                .map(n -> {
                    OptionModel optionModel = OptionModel.of(n.name(), n.toString());
                    if (HasDescription.class.isAssignableFrom(enumClass)) {
                        optionModel.setDescription(((HasDescription) n).getDescription());
                    }
                    if (HasIcon.class.isAssignableFrom(enumClass)) {
                        String icon = ((HasIcon) n).getIcon();
                        if (icon != null) {
                            optionModel.setIcon(new Icon(icon, ((HasIcon) n).getColor()));
                        }
                    }
                    return optionModel;
                }).collect(Collectors.toList());
    }

    public static @NotNull List<OptionModel> list(@NotNull Class<? extends KeyValueEnum> enumClass) {
        return Stream.of(enumClass.getEnumConstants()).map(n -> OptionModel.of(n.getKey(), n.getValue()))
                .collect(Collectors.toList());
    }

    public static @NotNull List<OptionModel> list(@NotNull String... values) {
        return list(Arrays.asList(values));
    }

    public static @NotNull List<OptionModel> list(@NotNull Collection<String> values) {
        List<OptionModel> models = new ArrayList<>();
        for (String value : values) {
            if (value.contains("..")) { // 1..12;Value %s
                String[] items = value.split("\\.\\.");
                String[] toAndDefinition = items[1].split(";");
                String title = toAndDefinition.length == 2 ? toAndDefinition[1] : "%s";
                for (int i = Integer.parseInt(items[0]); i <= Integer.parseInt(toAndDefinition[0]); i++) {
                    models.add(OptionModel.of(String.valueOf(i), format(title, i)));
                }
            } else if (value.contains(":")) {
                String[] items = value.split(":");
                models.add(OptionModel.of(items[0], items[1]));
            } else {
                models.add(OptionModel.of(value));
            }
        }
        return models;
    }

    public static @NotNull List<OptionModel> listWithEmpty(@NotNull Collection<String> values) {
        return withEmpty(list(values));
    }

    public static @NotNull List<OptionModel> list(@NotNull OptionModel... optionModels) {
        return Stream.of(optionModels).collect(Collectors.toList());
    }

    public static @NotNull List<OptionModel> list(@NotNull JsonNode jsonNode) {
        List<OptionModel> list = new ArrayList<>();
        for (JsonNode child : jsonNode) {
            list.add(OptionModel.key(child.asText()));
        }
        return list;
    }

    public static @NotNull List<OptionModel> range(int min, int max) {
        return IntStream.range(min, max).mapToObj(value -> OptionModel.key(String.valueOf(value))).collect(Collectors.toList());
    }

    public static <T> @NotNull List<OptionModel> list(@NotNull Collection<T> list, @NotNull Function<T, String> keyFn,
                                                      @NotNull Function<T, String> valueFn) {
        return list.stream().map(e -> OptionModel.of(keyFn.apply(e), valueFn.apply(e))).collect(Collectors.toList());
    }

    public static @NotNull List<OptionModel> list(@NotNull Map<String, String> map) {
        return map.entrySet().stream().map(e -> OptionModel.of(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    public static @NotNull List<OptionModel> entityList(@NotNull Collection<? extends BaseEntity> list, @NotNull Context context) {
        return entityList(list, null, context);
    }

    public static @NotNull List<OptionModel> entityList(@NotNull Class<? extends BaseEntity> entityClass, @NotNull Context context) {
        List<? extends BaseEntity> list = context.db().findAll(entityClass);
        return entityList(list, null, context);
    }

    public static @NotNull OptionModel entity(
            @NotNull BaseEntity entity,
            @Nullable BiConsumer<BaseEntity, OptionModel> configurator,
            @NotNull Context context) {
        return entityList(List.of(entity), configurator, context).get(0);
    }

    public static @NotNull List<OptionModel> entityList(
            @NotNull Collection<? extends BaseEntity> list,
            @Nullable BiConsumer<BaseEntity, OptionModel> configurator,
            @NotNull Context context) {
        return list.stream().map(entity -> {
                    OptionModel model = OptionModel.of(entity.getEntityID(), entity.getTitle());
                    if (entity instanceof HasStatusAndMsg status) {
                        model.setStatus(status);
                    }
                    if (entity instanceof SelectionConfiguration sc) {
                        model.setIcon(sc.getSelectionIcon());
                        model.setDescription(sc.getSelectionDescription());
                    }
                    entity.configureOptionModel(model, context);
                    if (configurator != null) {
                        configurator.accept(entity, model);
                    }
                    return model;
                })
                .sorted().collect(Collectors.toList());
    }

    public static @NotNull List<OptionModel> simpleNamelist(@NotNull Collection list) {
        List<OptionModel> optionModels = new ArrayList<>();
        for (Object o : list) {
            OptionModel optionModel = OptionModel.key(o.getClass().getSimpleName());
            if (o instanceof HasDescription description) {
                optionModel.setDescription(description.getDescription());
            }
            if (o instanceof HasIcon icon && icon.getIcon() != null) {
                optionModel.setIcon(new Icon(icon.getIcon(), icon.getColor()));
            }
            optionModels.add(optionModel);
        }
        return optionModels;
    }

    public static void sort(@NotNull List<OptionModel> options) {
        sort(options, DEFAULT_COMPARATOR);
    }

    public static void sort(@NotNull List<OptionModel> options, Comparator<OptionModel> comparator) {
        options.sort(comparator);
        for (OptionModel option : options) {
            if (option.hasChildren()) {
                OptionModel.sort(option.getChildren());
            }
        }
    }

    public static @Nullable OptionModel getByKey(@NotNull Collection<OptionModel> optionModels, @NotNull String key) {
        return optionModels.stream().filter(o -> o.getKey().equals(key)).findAny().orElse(null);
    }

    public OptionModel setRemovable(boolean removable) {
        json(json -> json.put("removable", removable));
        return this;
    }

    public @NotNull OptionModel setDisabled(Boolean disabled) {
        this.disabled = Boolean.TRUE.equals(disabled) ? true : null;
        return this;
    }

    public @NotNull OptionModel setStatus(@Nullable HasStatusAndMsg statusEntity) {
        this.status = statusEntity == null ? null : statusEntity.getStatus();
        return this;
    }

    public @NotNull OptionModel setIcon(@Nullable Icon icon) {
        if (icon != null) {
            this.icon = icon.getIcon();
            this.color = icon.getColor();
        }
        return this;
    }

    public @NotNull OptionModel setIcon(@Nullable String icon) {
        this.icon = icon;
        return this;
    }

    public @Nullable String getTitle() {
        return key.equals(title) ? null : title;
    }

    public @Nullable ObjectNode getJson() {
        return json.isEmpty() ? null : json;
    }

    public boolean has(@NotNull String key) {
        return json.has(key);
    }

    public @NotNull OptionModel setDescription(@Nullable String description) {
        if (StringUtils.isNotEmpty(description)) {
            json(json -> json.put("description", description));
        }
        return this;
    }

    @JsonIgnore
    public @NotNull Collection<OptionModel> getOrCreateChildren() {
        return children == null ? Collections.emptyList() : children;
    }

    @JsonIgnore
    public @NotNull String getTitleOrKey() {
        return title == null ? key : title;
    }

    @JsonIgnore
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public @NotNull OptionModel addChildIfHasSubChildren(@Nullable OptionModel child) {
        if (child != null && child.hasChildren()) {
            addChild(child);
        }
        return this;
    }

    public @Nullable OptionModel findByKey(@NotNull String key) {
        if (this.key.equals(key)) {
            return this;
        }
        if (this.children != null) {
            for (OptionModel child : this.children) {
                OptionModel found = child.findByKey(key);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    public @NotNull OptionModel setChildren(@NotNull Collection<OptionModel> children) {
        for (OptionModel child : children) {
            this.addChild(child);
        }
        return this;
    }

    public @NotNull OptionModel addChild(@Nullable OptionModel child) {
        if (child != null) {
            if (this.children == null) {
                children = new ArrayList<>();
            }
            child.key = this.key.isEmpty() ? child.key : this.key + LEVEL_DELIMITER + child.key;
            children.add(child);

            modifyChildrenKeys(this.key, child);
        }
        return this;
    }

    public void modifyChildrenKeys(@NotNull String key, @NotNull OptionModel child) {
        if (child.children != null) {
            for (OptionModel optionModel : child.children) {
                optionModel.key = key + LEVEL_DELIMITER + optionModel.key;
                modifyChildrenKeys(key, optionModel);
            }
        }
    }

    public @NotNull OptionModel json(@NotNull Consumer<ObjectNode> consumer) {
        consumer.accept(json);
        return this;
    }

    @Override
    @SneakyThrows
    public String toString() {
        return OBJECT_MAPPER.writeValueAsString(this);
    }

    @Override
    public int compareTo(@NotNull OptionModel other) {
        return getTitleOrKey().compareTo(other.getTitleOrKey());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OptionModel optionModel)) {
            return false;
        }
        return Objects.equals(key, optionModel.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    /**
     * Specify description for any pojo to allow it show on UI when convert to OptionModel
     */
    public interface HasDescription {

        @Nullable
        String getDescription();
    }

    public interface HasIcon {

        @Nullable
        String getIcon();

        @Nullable
        String getColor();
    }

    public interface KeyValueEnum {

        default @NotNull String getKey() {
            return ((Enum) this).name();
        }

        default @NotNull String getValue() {
            return this.toString();
        }
    }
}
