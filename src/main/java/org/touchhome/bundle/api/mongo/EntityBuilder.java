package org.touchhome.bundle.api.mongo;

import dev.morphia.annotations.CappedAt;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.builders.AnnotationBuilder;

public class EntityBuilder extends AnnotationBuilder<Entity> implements Entity {
    /**
     * Creates a new instance
     *
     * @return the new instance
     */
    public static EntityBuilder builder() {
        return new EntityBuilder();
    }

    @Override
    public Class<Entity> annotationType() {
        return Entity.class;
    }

    /**
     * Sets the discriminator value
     *
     * @param discriminator the discriminator to use
     * @return this
     */
    public EntityBuilder discriminator(String discriminator) {
        put("discriminator", discriminator);
        return this;
    }

    /**
     * Sets the discriminator key
     *
     * @param discriminatorKey the discriminator key to use
     * @return this
     */
    public EntityBuilder discriminatorKey(String discriminatorKey) {
        put("discriminatorKey", discriminatorKey);
        return this;
    }

    /**
     * Toggles whether or not to use the discriminator
     *
     * @param use true to use the discriminator
     * @return this
     */
    public EntityBuilder useDiscriminator(boolean use) {
        put("useDiscriminator", use);
        return this;
    }

    @Override
    public String value() {
        return get("value");
    }

    public EntityBuilder useCollection(String collection) {
        put("value", collection);
        return this;
    }

    @Override
    public CappedAt cap() {
        return get("cap");
    }

    public EntityBuilder useCap(CappedAt cap) {
        put("cap", cap);
        return this;
    }

    @Override
    public String concern() {
        return get("concern");
    }

    public EntityBuilder useConcern(String concern) {
        put("concern", concern);
        return this;
    }

    @Override
    public boolean useDiscriminator() {
        return get("useDiscriminator");
    }

    @Override
    public String discriminatorKey() {
        return get("discriminatorKey");
    }

    @Override
    public String discriminator() {
        return get("discriminator");
    }
}
