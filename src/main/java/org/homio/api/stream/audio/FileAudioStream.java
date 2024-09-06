package org.homio.api.stream.audio;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

@Getter
public class FileAudioStream extends FileSystemResource implements AudioStream {

    private final @NotNull AudioFormat format;

    public FileAudioStream(@NotNull File file) {
        this(file, AudioStream.evaluateFormat(file.getName()));
    }

    public FileAudioStream(@NotNull File file, @NotNull AudioFormat format) {
        super(file);
        this.format = format;
    }
}
