package org.homio.api.stream.video;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.homio.api.stream.StreamFormat;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;

@Getter
@RequiredArgsConstructor
public class VideoFormat implements StreamFormat {

    public static final VideoFormat MP4 = new VideoFormat(new MediaType("video", "mp4"));
    public static final VideoFormat AVI = new VideoFormat(new MediaType("video", "avi"));

    private final @NotNull MimeType mimeType;
}