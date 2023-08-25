package org.homio.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Accessors(chain = true)
@RequiredArgsConstructor
public class FileModel implements Comparable<FileModel> {

    private @NotNull final String name;
    private @NotNull final String content;
    private @NotNull final FileContentType contentType;
    private @Nullable @Setter @JsonIgnore Consumer<String> saveHandler;

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
