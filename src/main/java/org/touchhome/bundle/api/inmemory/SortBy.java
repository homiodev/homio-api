package org.touchhome.bundle.api.inmemory;

import lombok.Getter;

@Getter
public class SortBy {
    private final String orderField;
    private final boolean asceding;
    private SortBy(String orderField, boolean asceding) {
        this.orderField = orderField;
        this.asceding = asceding;
    }

    public static SortBy sortAsc(String orderField) {
        return new SortBy(orderField, true);
    }

    public static SortBy sortDesc(String orderField) {
        return new SortBy(orderField, false);
    }
}
