package org.homio.api.storage;

import com.mongodb.BasicDBObject;
import lombok.Getter;
import org.bson.conversions.Bson;

@Getter
public class SortBy {

    private final String orderField;
    private final boolean asc;

    private SortBy(String orderField, boolean asc) {
        this.orderField = orderField;
        this.asc = asc;
    }

    public static SortBy sortAsc(String orderField) {
        return new SortBy(orderField, true);
    }

    public static SortBy sortDesc(String orderField) {
        return new SortBy(orderField, false);
    }

    public Bson toBson() {
        return new BasicDBObject(orderField, asc ? 1 : -1);
    }
}
