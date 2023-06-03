package org.homio.api.video;

import org.json.JSONObject;
import org.springframework.core.io.Resource;

public record DownloadFile(Resource stream, long size, String name, JSONObject metadata) {
}
