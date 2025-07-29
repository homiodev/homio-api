package org.homio.api.entity;

import org.homio.api.model.HasEntityIdentifier;

/**
 * Base interface for classes that may be fetched by UI and getting fields for ui view/editing API:
 * http://[host:port]/rest/item/[class implements EntityFieldMetadata]/context
 */
public interface EntityFieldMetadata extends HasEntityIdentifier {}
