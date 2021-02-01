package com.alipay.sofa.registry.util;

public final class SystemUtils {
    private SystemUtils() {
    }

    public static int getEnvInteger(String name, int def) {
        String v = System.getenv(name);
        return v == null ? def : Integer.valueOf(v);
    }

    public static String getEnv(String name, String def) {
        String v = System.getenv(name);
        return v == null ? def : v;
    }
}
