package org.touchhome.bundle.api.condition;

import org.touchhome.bundle.api.EntityContext;

import java.util.function.Predicate;

public class TrueCondition implements Predicate<EntityContext> {
    @Override
    public boolean test(EntityContext entityContext) {
        return true;
    }
}
