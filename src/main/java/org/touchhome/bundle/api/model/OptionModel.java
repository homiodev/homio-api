package org.touchhome.bundle.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazecast.jSerialComm.SerialPort;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.common.fs.TreeNode;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

    private final JSONObject json = new JSONObject();

    @Getter
    private List<OptionModel> children;

    private OptionModel(@NotNull Object key, @Nullable Object title) {
        this.key = key == null ? null : key.toString();
        this.title = String.valueOf(title);
    }

    public String getTitle() {
        return key.equals(title) ? null : title;
    }

    public JSONObject getJson() {
        return json.isEmpty() ? null : json;
    }

    public boolean has(String key) {
        return json.has(key);
    }

    public JSONObject put(String key, Object value) {
        return json.put(key, value);
    }

    public void putIfAbsent(String key, Object value) {
        if (!json.has(key)) {
            json.put(key, value);
        }
    }

    public static OptionModel key(@NotNull String key) {
        OptionModel optionModel = new OptionModel();
        optionModel.key = key;
        return optionModel;
    }

    public OptionModel setDescription(String description) {
        if (StringUtils.isNotEmpty(description)) {
            json(json -> json.put("description", description));
        }
        return this;
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
        return Stream.of(values).map(v -> OptionModel.of(v, v)).collect(Collectors.toList());
    }

    public static List<OptionModel> list(@NotNull Collection<String> values) {
        return values.stream().map(v -> OptionModel.of(v, v)).collect(Collectors.toList());
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

    public static List<OptionModel> list(@NotNull Collection<? extends BaseEntity>... lists) {
        List<OptionModel> res = new ArrayList<>();
        for (Collection<? extends BaseEntity> list : lists) {
            res.addAll(
                    list.stream().map(e -> OptionModel.of(e.getEntityID(), StringUtils.defaultIfEmpty(e.getName(), e.getTitle())))
                            .collect(Collectors.toList()));
        }
        return res;
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
            child.key = this.key == null ? child.key : this.key + "~~~" + child.key;
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

    public OptionModel json(@NotNull Consumer<JSONObject> consumer) {
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
}
