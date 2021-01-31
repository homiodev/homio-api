package org.touchhome.bundle.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazecast.jSerialComm.SerialPort;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.touchhome.bundle.api.entity.BaseEntity;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Getter
@Accessors(chain = true)
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OptionModel implements Comparable<OptionModel> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String key;
    private String title;

    @Setter
    private String imageRef;

    private JSONObject json = new JSONObject();

    private Collection<OptionModel> children;

    private OptionModel(Object key, Object title) {
        this.key = String.valueOf(key);
        this.title = String.valueOf(title);
    }

    public static OptionModel key(String key) {
        OptionModel optionModel = new OptionModel();
        optionModel.key = key;
        return optionModel;
    }

    public static OptionModel separator() {
        return OptionModel.key("~~~sep~~~");
    }

    public static OptionModel of(String key, String title) {
        return new OptionModel(key, title);
    }

    public static List<OptionModel> listWithEmpty(Class<? extends KeyValueEnum> enumClass) {
        return withEmpty(list(enumClass));
    }

    public static List<OptionModel> enumWithEmpty(Class<? extends Enum> enumClass) {
        return withEmpty(enumList(enumClass));
    }

    public static List<OptionModel> withEmpty(List<OptionModel> list) {
        list.add(OptionModel.of("", "no_value"));
        return list;
    }

    public static List<OptionModel> listOfPorts(boolean withEmpty) {
        List<OptionModel> optionModels = Arrays.stream(SerialPort.getCommPorts()).map(p ->
                new OptionModel(p.getSystemPortName(), p.getSystemPortName() + "/" + p.getDescriptivePortName())).collect(Collectors.toList());
        return withEmpty ? withEmpty(optionModels) : optionModels;
    }

    public static List<OptionModel> enumList(Class<? extends Enum> enumClass) {
        if (HasDescription.class.isAssignableFrom(enumClass)) {
            return Stream.of(enumClass.getEnumConstants()).map(n -> OptionModel.of(n.name(), n.toString())
                    .json(json -> json.put("description", ((HasDescription) n).getDescription()))).collect(Collectors.toList());
        }
        return Stream.of(enumClass.getEnumConstants()).map(n -> OptionModel.of(n.name(), n.toString())).collect(Collectors.toList());
    }

    public static List<OptionModel> list(Class<? extends KeyValueEnum> enumClass) {
        return Stream.of(enumClass.getEnumConstants()).map(n -> OptionModel.of(n.getKey(), n.getValue())).collect(Collectors.toList());
    }

    public static List<OptionModel> list(String... values) {
        return Stream.of(values).map(v -> OptionModel.of(v, v)).collect(Collectors.toList());
    }

    public static List<OptionModel> list(OptionModel... optionModels) {
        return Stream.of(optionModels).collect(Collectors.toList());
    }

    public static List<OptionModel> range(int min, int max) {
        return IntStream.range(min, max).mapToObj(value -> OptionModel.key(String.valueOf(value))).collect(Collectors.toList());
    }

    public static <T> List<OptionModel> list(Collection<T> list, Function<T, String> keyFn, Function<T, String> valueFn) {
        return list.stream().map(e -> OptionModel.of(keyFn.apply(e), valueFn.apply(e))).collect(Collectors.toList());
    }

    public static List<OptionModel> list(Map<String, String> map) {
        return map.entrySet().stream().map(e -> OptionModel.of(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    public static <T extends BaseEntity> List<OptionModel> list(Collection<T> list) {
        return list.stream().map(e -> OptionModel.of(e.getEntityID(), StringUtils.defaultIfEmpty(e.getName(), e.getTitle()))).collect(Collectors.toList());
    }

    public static List<OptionModel> simpleNamelist(Collection list) {
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
    public String getTitleOrKey() {
        return title == null ? key : title;
    }

    @JsonIgnore
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public void addChildIfHasSubChildren(OptionModel child) {
        if (child != null && child.hasChildren()) {
            addChild(child);
        }
    }

    public OptionModel findByKey(String key) {
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

    public void addChild(OptionModel child) {
        if (child != null) {
            if (this.children == null) {
                children = new ArrayList<>();
            }
            child.key = this.key + "~~~" + child.key;
            children.add(child);

            modifyChildrenKeys(this.key, child);
        }
    }

    public void modifyChildrenKeys(String key, OptionModel child) {
        if (child.children != null) {
            for (OptionModel optionModel : child.children) {
                optionModel.key = key + "~~~" + optionModel.key;
                modifyChildrenKeys(key, optionModel);
            }
        }
    }

    public OptionModel json(Consumer<JSONObject> consumer) {
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
