package org.homio.bundle.api.video;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.JSONObject;
import org.springframework.core.io.Resource;

@Getter
@AllArgsConstructor
public class DownloadFile {
    private final Resource stream;
    private final long size;
    private final String name;
    private final JSONObject metadata;
}
