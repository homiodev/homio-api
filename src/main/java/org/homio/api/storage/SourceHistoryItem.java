package org.homio.api.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public class SourceHistoryItem implements Comparable<SourceHistoryItem> {
    private long timestamp;
    private Object value;

    /**
     * Desc sorting
     */
    @Override
    public int compareTo(@NotNull SourceHistoryItem o) {
        return Long.compare(o.timestamp, this.timestamp);
    }
}
