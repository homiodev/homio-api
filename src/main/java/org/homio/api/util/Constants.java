package org.homio.api.util;

public class Constants {

    public static final String PRIMARY_DEVICE = "primary";

    public static final String[] SYSTEM_ADDONS =
            {"telegram", "mqtt", "z2m", "cloud", "bluetooth", "camera"};

    public static final String ADMIN_ROLE = "ROLE_ADMIN";
    public static final String ROLE_ADMIN_AUTHORIZE = "hasRole('" + ADMIN_ROLE + "')";
}
