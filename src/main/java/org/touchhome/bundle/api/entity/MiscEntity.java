package org.touchhome.bundle.api.entity;

import lombok.Getter;
import org.json.JSONObject;
import org.touchhome.bundle.api.converter.JSONObjectConverter;
import org.touchhome.bundle.api.ui.UISidebarMenu;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@UISidebarMenu(icon = "fas fa-puzzle-piece", order = 100, bg = "#939E18", allowCreateNewItems = true, overridePath = "misc")
public abstract class MiscEntity<T extends MiscEntity> extends BaseEntity<T> implements HasJsonData<T> {
    @Lob
    @Getter
    @Column(length = 1048576)
    @Convert(converter = JSONObjectConverter.class)
    private JSONObject jsonData = new JSONObject();
}
