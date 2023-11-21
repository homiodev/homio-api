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

    private @NotNull String name;
    private @NotNull String content;
    private @NotNull final FileContentType contentType;
    private @Nullable @JsonIgnore Consumer<String> saveHandler;

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
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        FileModel fileModel = (FileModel) o;
        return name.equals(fileModel.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public boolean isReadOnly() {
        return saveHandler == null;
    }
}
