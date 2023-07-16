package org.homio.api.ui.field.model;

/**
 * Specify link for ui to able to open it in new tab
 *
 * @param href  - full link
 * @param title - title
 */
public record HrefModel(String href, String title) {
}
