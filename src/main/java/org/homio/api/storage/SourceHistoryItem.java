package org.homio.api.storage;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
