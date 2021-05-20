package org.touchhome.bundle.api.workspace.scratch;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.model.KeyValueEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public abstract class MenuBlock {
    @JsonIgnore
    private final String name;

    protected boolean multiSelect;
    protected String uiDelimiter;

    public static ServerMenuBlock ofServer(String name, String url, String firstKey, String firstValue, Integer... clusters) {
        return new ServerMenuBlock(name, url, firstKey, firstValue, clusters);
    }

    public static ServerMenuBlock ofServer(String name, String url, String firstKey, String firstValue) {
        return new ServerMenuBlock(name, url, firstKey, firstValue, null);
    }

    public static ServerMenuBlock ofServer(String name, String url) {
        return new ServerMenuBlock(name, url, "-", "-", null);
    }

    public static ServerMenuBlock ofServerItems(String name, Class<? extends BaseEntity> itemClass) {
        return new ServerMenuBlock(name, "rest/item/type/" + itemClass.getSimpleName(), "-", "-", null);
    }

    public static ServerMenuBlock ofServerItems(String name, Class<? extends BaseEntity> itemClass, String firstKey, String firstValue) {
        return new ServerMenuBlock(name, "rest/item/type/" + itemClass.getSimpleName(), firstKey, firstValue, null);
    }

    public static <T extends Enum> StaticMenuBlock<T> ofStatic(String name, Class<T> enumClass, T defaultValue) {
        return new StaticMenuBlock(name, null, enumClass).addEnum(enumClass).setDefaultValue(defaultValue);
    }

    public static <T extends KeyValueEnum> StaticMenuBlock<T> ofStaticKV(String name, Class<T> enumClass, T defaultValue) {
        return new StaticMenuBlock(name, null, enumClass).addEnumKVE(enumClass).setDefaultValue(defaultValue);
    }

    public static <T extends Enum> StaticMenuBlock<T> ofStatic(String name, Class<T> enumClass, T defaultValue, Predicate<T> filter) {
        return new StaticMenuBlock(name, null, enumClass).addEnum(enumClass, filter).setDefaultValue(defaultValue);
    }

    public static StaticMenuBlock<String> ofStaticList(String name, Map<String, String> items, String defaultValue) {
        return new StaticMenuBlock(name, items, String.class).setDefaultValue(defaultValue);
    }

    public abstract Object getDefaultValue();

    @Getter
    @Accessors(chain = true)
    public static class ServerMenuBlock extends MenuBlock {
        private final boolean acceptReporters = true;
        private final boolean async = true;
        private final MenuBlockFunction items;
        @JsonIgnore
        private final Integer[] clusters;

        ServerMenuBlock(String name, String url, String keyName, String valueName, String firstKey, String firstValue, Integer[] clusters) {
            super(name);
            this.clusters = clusters;
            this.items = new MenuBlockFunction(url, keyName, valueName, new String[]{firstKey, firstValue});
        }

        ServerMenuBlock(String name, String url, String firstKey, String firstValue, Integer[] clusters) {
            this(name, url, null, null, firstKey, firstValue, clusters);
        }

        public <T extends BaseEntity> ServerMenuBlock setDefault(T defaultEntity) {
            if (defaultEntity != null) {
                this.items.firstKV[0] = defaultEntity.getTitle();
                this.items.firstKV[1] = defaultEntity.getEntityID();
            }
            return this;
        }

        public ServerMenuBlock setMultiSelect(String uiDelimiter) {
            this.multiSelect = true;
            return setUIDelimiter(uiDelimiter);
        }

        public ServerMenuBlock setUIDelimiter(String uiDelimiter) {
            this.uiDelimiter = uiDelimiter;
            return this;
        }

        public ServerMenuBlock setDependency(MenuBlock... dependencies) {
            this.items.dependencies = Stream.of(dependencies).map(MenuBlock::getName).collect(Collectors.toList());
            return this;
        }

        @Override
        public Object getDefaultValue() {
            return this.items.firstKV[1];
        }

        @Getter
        @RequiredArgsConstructor
        static class MenuBlockFunction {
            private final String url;
            private final String keyName;
            private final String valueName;
            private final String[] firstKV;
            public List<String> dependencies;
        }
    }

    @Getter
    @Accessors(chain = true)
    public static class StaticMenuBlock<T> extends MenuBlock {
        private boolean acceptReporters = true;
        private List<StaticMenuItem> items = new ArrayList<>();
        private Map<String, List> subMenu;
        @Setter
        private Object defaultValue;
        private Class<T> typeClass;

        StaticMenuBlock(String name, Map<String, String> map, Class<T> typeClass) {
            super(name);
            this.typeClass = typeClass;
            if (map != null) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    this.items.add(new StaticMenuItem(entry.getKey(), entry.getValue()));
                }
            }
        }

        public StaticMenuBlock<T> setMultiSelect(String uiDelimiter) {
            this.multiSelect = true;
            this.uiDelimiter = uiDelimiter;
            return this;
        }

        public StaticMenuBlock add(String key, Object value) {
            this.items.add(new StaticMenuItem(key, value.toString()));
            return this;
        }

        private StaticMenuBlock addEnum(Class<? extends Enum> enumClass) {
            for (Enum item : enumClass.getEnumConstants()) {
                this.items.add(new StaticMenuItem(item.name(), item.toString()));
            }
            return this;
        }

        private StaticMenuBlock addEnumKVE(Class<? extends KeyValueEnum> enumClass) {
            for (KeyValueEnum item : enumClass.getEnumConstants()) {
                this.items.add(new StaticMenuItem(item.getKey(), item.getValue()));
            }
            return this;
        }

        private <T extends Enum> StaticMenuBlock addEnum(Class<T> enumClass, Predicate<T> filter) {
            for (T item : enumClass.getEnumConstants()) {
                if (filter.test(item)) {
                    this.items.add(new StaticMenuItem(item.name(), item.toString()));
                }
            }
            return this;
        }

        public <T extends Enum, S extends Enum> void subMenu(T key, Class<S> subMenu) {
            if (this.subMenu == null) {
                this.subMenu = new HashMap<>();
            }
            this.subMenu.put(key.name(), Stream.of(subMenu.getEnumConstants()).map(Enum::name).collect(Collectors.toList()));

        }

        public String getFirstValue() {
            return this.items.isEmpty() ? null : this.items.get(0).getText();
        }

        @Getter
        @AllArgsConstructor
        private static class StaticMenuItem {
            private String value;
            private String text;
        }
    }
}
