package org.homio.api.storage;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Accessors(chain = true)
@AllArgsConstructor
public class SourceHistory {
    private final int count;
    private Float min;
    private Float max;
    private Float median;
    @Setter
    private String icon;
    @Setter
    private String iconColor;
    @Setter
    private String name;
    @Setter
    private String description;
    @Setter
    private List<String> attributes;

    public SourceHistory(int count, Float min, Float max, Float median) {
        this.count = count;
        this.min = min;
        this.max = max;
        this.median = median;
    }
}
