package com.cdcollaguazo.bsn.infra.config;

public class ConfigLoader {

    private ConfigLoader() {}

    public static Config loadConfig() {
        return new Config(
                required("PLATFORM_NAME"),
                required("MIGRATION_IMAGE"),
                required("API_IMAGE"),
                required("BSN_DDL_USER"),
                required("BSN_DML_USER"),
                required("JWT_ISSUER_URI")
                );
    }

    private static String required(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing environment variable: " + name);
        }

        return value;
    }

}
