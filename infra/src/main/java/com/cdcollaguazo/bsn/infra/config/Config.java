package com.cdcollaguazo.bsn.infra.config;

public record Config(
        String platformName,
        String migrationImage,
        String apiImage,
        String apiDdlUser,
        String apiDmlUser,
        String jwtIssuerUri
) {
}
