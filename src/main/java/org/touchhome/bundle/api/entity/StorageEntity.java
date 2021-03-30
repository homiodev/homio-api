package org.touchhome.bundle.api.entity;

import lombok.Getter;
import org.json.JSONObject;
import org.touchhome.bundle.api.converter.JSONObjectConverter;
import org.touchhome.bundle.api.fs.BaseFileSystemEntity;
import org.touchhome.bundle.api.fs.VendorFileSystem;
import org.touchhome.bundle.api.ui.UISidebarMenu;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@UISidebarMenu(icon = "fas fa-database", order = 200, bg = "#8B2399", allowCreateNewItems = true, overridePath = "storage")
public abstract class StorageEntity<T extends StorageEntity, FS extends VendorFileSystem> extends BaseEntity<T> implements HasJsonData<T>,
        BaseFileSystemEntity<T, FS> {
    @Lob
    @Getter
    @Column(length = 1048576)
    @Convert(converter = JSONObjectConverter.class)
    private JSONObject jsonData = new JSONObject();
}
