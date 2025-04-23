package org.homio.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@Accessors(chain = true)
public class FileModel implements Comparable<FileModel> {

    private @NotNull
    final FileContentType contentType;
    private @NotNull String name;
    private @NotNull String content;
    private @Nullable
    @JsonIgnore Consumer<String> saveHandler;

    public FileModel(@NotNull String name, @NotNull String content, @NotNull FileContentType contentType) {
        this.name = name;
        this.content = content;
        this.contentType = contentType;
    }

    @Override
    public int compareTo(FileModel o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FileModel fileModel = (FileModel) o;
        return contentType == fileModel.contentType && Objects.equals(name, fileModel.name) && Objects.equals(content, fileModel.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, content, contentType);
    }

    public boolean isReadOnly() {
        return saveHandler == null;
    }
}
