package org.homio.api.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.homio.api.model.Icon;

import java.util.List;

@Getter
@Accessors(chain = true)
@AllArgsConstructor
public class SourceHistory {

  private final int count;
  private Float min;
  private Float max;
  private Float median;
  @Setter private Icon icon;
  @Setter private String name;
  @Setter private String description;
  @Setter private List<String> attributes;

  public SourceHistory(int count, Float min, Float max, Float median) {
    this.count = count;
    this.min = min;
    this.max = max;
    this.median = median;
  }
}
