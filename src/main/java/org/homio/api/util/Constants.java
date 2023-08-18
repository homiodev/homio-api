package org.homio.api.util;

public class Constants {

    public static final String PRIMARY_DEVICE = "primary";
    public static final String PRIMARY_COLOR = "primary";
    public static final String DANGER_COLOR = "danger";

    public static final String[] SYSTEM_ADDONS =
            {"telegram", "mqtt", "z2m", "cloud", "bluetooth", "camera"};
    public static final String ADMIN_ROLE = "ROLE_ADMIN";
    public static final String PRIVILEGED_USER_ROLE = "ROLE_PRIVILEGED_USER";
    public static final String GUEST_ROLE = "ROLE_GUEST";

    public static final String ADMIN_ROLE_AUTHORIZE = "hasRole('" + ADMIN_ROLE + "')";
    public static final String PRIVILEGED_USER_ROLE_AUTHORIZE = "hasRole('" + PRIVILEGED_USER_ROLE + "')";
}
