package org.touchhome.bundle.api.video;

import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.touchhome.bundle.api.EntityContext;

import java.net.URI;
import java.nio.file.Path;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

public interface VideoPlaybackStorage {
    LinkedHashMap<Long, Boolean> getAvailableDaysPlaybacks(EntityContext entityContext, String profile, Date from, Date to) throws Exception;

    List<PlaybackFile> getPlaybackFiles(EntityContext entityContext, String profile, Date from, Date to) throws Exception;

    DownloadFile downloadPlaybackFile(EntityContext entityContext, String profile, String fileId, Path path) throws Exception;

    URI getPlaybackVideoURL(EntityContext entityContext, String fileId) throws Exception;

    @AllArgsConstructor
    class DownloadFile {
        public Resource stream;
        public long size;
        public String name;
    }

    @AllArgsConstructor
    class PlaybackFile {
        public String id;
        public String name;
        public Date startTime;
        public Date endTime;
        public int size;
        public String type;
    }
}
