package org.homio.api.storage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class SourceHistoryItem implements Comparable<SourceHistoryItem> {

    private final long timestamp;
    private final Object value;

    public SourceHistoryItem(long timestamp, Object value) {
        this.timestamp = timestamp;
        this.value = value instanceof Double ? BigDecimal.valueOf((Double) value).setScale(2, RoundingMode.DOWN) : value;
    }

    /**
     * Desc sorting
     */
    @Override
    public int compareTo(@NotNull SourceHistoryItem o) {
        return Long.compare(o.timestamp, this.timestamp);
    }
}
