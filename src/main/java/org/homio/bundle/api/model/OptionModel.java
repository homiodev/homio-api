package org.homio.bundle.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fazecast.jSerialComm.SerialPort;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.homio.bundle.api.entity.BaseEntity;
import org.homio.bundle.api.entity.HasStatusAndMsg;
import org.homio.bundle.api.fs.TreeNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Accessors(chain = true)
@NoArgsConstructor
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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final ObjectNode json = OBJECT_MAPPER.createObjectNode();
    @Getter
    private @NotNull String key;
    @Setter
    private String title;
    @Setter
    @Getter
    private String icon;
    @Setter
    @Getter
    private String color;
    @Getter
    private List<OptionModel> children;

    public OptionModel setDisabled(boolean disabled) {
        json.put("disabled", disabled);
        return this;
    }

    public OptionModel setStatus(HasStatusAndMsg statusEntity) {
        if (statusEntity.getStatus().isOnline()) {
            icon = "fas fa-circle-check";
        } else {
            icon = "fas fa-circle-xmark";
            setDisabled(true);
        }
        return this;
    }

    private OptionModel(@NotNull Object key, @Nullable Object title) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        this.key = key.toString();
        this.title = title == null ? null : title.toString();
    }

    public static OptionModel key(@NotNull String key) {
        return new OptionModel(key, null);
    }

    public static OptionModel separator() {
        return OptionModel.key("~~~sep~~~");
    }

    public static OptionModel of(@NotNull String key) {
        return new OptionModel(key, key);
    }

    public static OptionModel of(@NotNull String key, @Nullable String title) {
        return new OptionModel(key, title);
    }

    public static List<OptionModel> listWithEmpty(@NotNull Class<? extends KeyValueEnum> enumClass) {
        return withEmpty(list(enumClass));
    }

    public static List<OptionModel> enumWithEmpty(@NotNull Class<? extends Enum> enumClass) {
        return withEmpty(enumList(enumClass));
    }

    public static List<OptionModel> withEmpty(@NotNull List<OptionModel> list) {
        list.add(0, OptionModel.of("", "Empty"));
        return list;
    }

    public static OptionModel of(@NotNull TreeNode item) {
        OptionModel model = OptionModel.of(item.getId(), item.getName()).json(
                json -> json.put("dir", item.getAttributes().isDir())
                        .put("size", item.getAttributes().getSize())
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

    public static List<OptionModel> listOfPorts(boolean withEmpty) {
        List<OptionModel> optionModels = Arrays.stream(SerialPort.getCommPorts()).map(p ->
                        new OptionModel(p.getSystemPortName(), p.getSystemPortName() + "/" + p.getDescriptivePortName()))
                .collect(Collectors.toList());
        return withEmpty ? withEmpty(optionModels) : optionModels;
    }

    public static List<OptionModel> enumList(@NotNull Class<? extends Enum> enumClass) {
        if (HasDescription.class.isAssignableFrom(enumClass)) {
            return Stream.of(enumClass.getEnumConstants()).map(n -> OptionModel.of(n.name(), n.toString())
                    .setDescription(((HasDescription) n).getDescription())).collect(Collectors.toList());
        }
        return Stream.of(enumClass.getEnumConstants()).map(n -> OptionModel.of(n.name(), n.toString()))
                .collect(Collectors.toList());
    }

    public static List<OptionModel> list(@NotNull Class<? extends KeyValueEnum> enumClass) {
        return Stream.of(enumClass.getEnumConstants()).map(n -> OptionModel.of(n.getKey(), n.getValue()))
                .collect(Collectors.toList());
    }

    public static List<OptionModel> list(@NotNull String... values) {
        return list(Arrays.asList(values));
    }

    public static List<OptionModel> list(@NotNull Collection<String> values) {
        List<OptionModel> models = new ArrayList<>();
        for (String value : values) {
            if (value.contains("..")) { // 1..12;Value %s
                String[] items = value.split("\\.\\.");
                String[] toAndDefinition = items[1].split(";");
                String title = toAndDefinition.length == 2 ? toAndDefinition[1] : "%s";
                for (int i = Integer.parseInt(items[0]); i <= Integer.parseInt(toAndDefinition[0]); i++) {
                    models.add(OptionModel.of(String.valueOf(i), String.format(title, i)));
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

    public static List<OptionModel> listWithEmpty(@NotNull Collection<String> values) {
        return withEmpty(list(values));
    }

    public static List<OptionModel> list(@NotNull OptionModel... optionModels) {
        return Stream.of(optionModels).collect(Collectors.toList());
    }

    public static List<OptionModel> list(@NotNull JsonNode jsonNode) {
        List<OptionModel> list = new ArrayList<>();
        for (JsonNode child : jsonNode) {
            list.add(OptionModel.key(child.asText()));
        }
        return list;
    }

    public static List<OptionModel> range(int min, int max) {
        return IntStream.range(min, max).mapToObj(value -> OptionModel.key(String.valueOf(value))).collect(Collectors.toList());
    }

    public static <T> List<OptionModel> list(@NotNull Collection<T> list, @NotNull Function<T, String> keyFn,
                                             @NotNull Function<T, String> valueFn) {
        return list.stream().map(e -> OptionModel.of(keyFn.apply(e), valueFn.apply(e))).collect(Collectors.toList());
    }

    public static List<OptionModel> list(@NotNull Map<String, String> map) {
        return map.entrySet().stream().map(e -> OptionModel.of(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    public static List<OptionModel> entityList(@NotNull Collection<? extends BaseEntity> list) {
        return list.stream().map(entity -> {
                       OptionModel model = OptionModel.of(
                           entity.getEntityID(),
                           entity.getTitle());
                       entity.configureOptionModel(model);
                       return model;
                   })
                   .collect(Collectors.toList());
    }

    public static List<OptionModel> simpleNamelist(@NotNull Collection list) {
        List<OptionModel> optionModels = new ArrayList<>();
        for (Object o : list) {
            OptionModel optionModel = OptionModel.key(o.getClass().getSimpleName());
            if (o instanceof HasDescription) {
                optionModel.json.put("description", ((HasDescription) o).getDescription());
            }
            optionModels.add(optionModel);
        }
        return optionModels;
    }

    public static void sort(List<OptionModel> options) {
        sort(options, DEFAULT_COMPARATOR);
    }

    public static void sort(List<OptionModel> options, Comparator<OptionModel> comparator) {
        options.sort(comparator);
        for (OptionModel option : options) {
            if (option.hasChildren()) {
                OptionModel.sort(option.getChildren());
            }
        }
    }

    public String getTitle() {
        return key.equals(title) ? null : title;
    }

    public ObjectNode getJson() {
        return json.isEmpty() ? null : json;
    }

    public boolean has(String key) {
        return json.has(key);
    }

    public OptionModel setDescription(String description) {
        if (StringUtils.isNotEmpty(description)) {
            json(json -> json.put("description", description));
        }
        return this;
    }

    @JsonIgnore
    public Collection<OptionModel> getOrCreateChildren() {
        return children == null ? Collections.emptyList() : children;
    }

    @JsonIgnore
    public String getTitleOrKey() {
        return title == null ? key : title;
    }

    @JsonIgnore
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public OptionModel addChildIfHasSubChildren(@Nullable OptionModel child) {
        if (child != null && child.hasChildren()) {
            addChild(child);
        }
        return this;
    }

    public OptionModel findByKey(@NotNull String key) {
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

    public OptionModel setChildren(Collection<OptionModel> children) {
        for (OptionModel child : children) {
            this.addChild(child);
        }
        return this;
    }

    public OptionModel addChild(@Nullable OptionModel child) {
        if (child != null) {
            if (this.children == null) {
                children = new ArrayList<>();
            }
            child.key = this.key.isEmpty() ? child.key : this.key + "~~~" + child.key;
            children.add(child);

            modifyChildrenKeys(this.key, child);
        }
        return this;
    }

    public void modifyChildrenKeys(@NotNull String key, @NotNull OptionModel child) {
        if (child.children != null) {
            for (OptionModel optionModel : child.children) {
                optionModel.key = key + "~~~" + optionModel.key;
                modifyChildrenKeys(key, optionModel);
            }
        }
    }

    public OptionModel json(@NotNull Consumer<ObjectNode> consumer) {
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
        return this.title.compareTo(other.title);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OptionModel)) {
            return false;
        }
        OptionModel optionModel = (OptionModel) o;
        return Objects.equals(key, optionModel.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
