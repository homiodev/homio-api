package org.touchhome.bundle.api.model;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileModel implements Comparable<FileModel> {
    private String name;
    private String content;
    private FileContentType contentType;
    // for now only readOnly supports because don't where to pass save handler yet
    private boolean readOnly;

    @Override
    public int compareTo(FileModel o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileModel fileModel = (FileModel) o;
        return name.equals(fileModel.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
