package org.touchhome.bundle.api.inmemory;

import dev.morphia.query.Type;
import dev.morphia.query.experimental.filters.Filter;

public class TypeFilter extends Filter {

    public TypeFilter(String field, Type type) {
        super("$type", field, type.val());
    }
}
