package org.touchhome.bundle.api;

/**
 * Interface may be implemented and engine calls postConstruct() and onContextUpdate() at startup
 * Also onContextUpdate calls every time when new external been added/removed
 */
public interface BeanPostConstruct {
    default void postConstruct(EntityContext entityContext) throws Exception {

    }

    default void onContextUpdate(EntityContext entityContext) throws Exception {

    }
}
