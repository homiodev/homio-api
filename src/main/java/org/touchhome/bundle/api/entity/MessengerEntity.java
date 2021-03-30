package org.touchhome.bundle.api.entity;

import lombok.Getter;
import org.json.JSONObject;
import org.touchhome.bundle.api.converter.JSONObjectConverter;
import org.touchhome.bundle.api.ui.UISidebarMenu;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@UISidebarMenu(icon = "fab fa-facebook-messenger", order = 200, bg = "#A16427", allowCreateNewItems = true, overridePath = "messenger")
public abstract class MessengerEntity<T extends MessengerEntity> extends BaseEntity<T> implements HasJsonData<T> {
    @Lob
    @Getter
    @Column(length = 1048576)
    @Convert(converter = JSONObjectConverter.class)
    private JSONObject jsonData = new JSONObject();
}
