package com.cdcollaguazo.bsn.infra.construct;

import com.cdcollaguazo.bsn.infra.config.Config;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.rds.DatabaseSecret;
import software.amazon.awscdk.services.secretsmanager.ISecret;
import software.constructs.Construct;

import java.util.Map;

public class DatabaseMigrationConstruct extends Construct {

    private final ISecret apiDmlSecret;

    public DatabaseMigrationConstruct(Construct scope, String id, ISecret rdsSecret, String rdsEndpointAddress,
                                      Config config) {
        super(scope, id);

        // Database Secrets
        ISecret apiDdlSecret = DatabaseSecret.Builder.create(this, "ApiDdlSecret")
                .username(config.apiDdlUser())
                .secretName("bsn-api-ddl")
                .build();

        apiDmlSecret = DatabaseSecret.Builder.create(this, "ApiDmlSecret")
                .username(config.apiDmlUser())
                .secretName("bsn-api-dml")
                .build();

        // Migration Task Definition
        FargateTaskDefinition migrationTask = FargateTaskDefinition.Builder.create(this, "MigrationTask")
                .family("bsn-migration")
                .cpu(1024)
                .memoryLimitMiB(3072)
                .build();

        // Migration Container
        migrationTask.addContainer("MigrationContainer", ContainerDefinitionOptions.builder()
                .containerName("bsn-migration")
                .essential(true)
                .image(ContainerImage.fromRegistry(config.migrationImage()))
                .environment(
                        Map.of(
                                "HOST", rdsEndpointAddress
                        )
                )
                .secrets(
                        Map.of(
                                "ROOT_DB_USER", Secret.fromSecretsManager(rdsSecret, "username"),
                                "ROOT_DB_PASSWORD", Secret.fromSecretsManager(rdsSecret, "password"),
                                "API_DML_USER", Secret.fromSecretsManager(apiDmlSecret, "username"),
                                "API_DML_PASSWORD", Secret.fromSecretsManager(apiDmlSecret, "password"),
                                "API_DDL_USER", Secret.fromSecretsManager(apiDdlSecret, "username"),
                                "API_DDL_PASSWORD", Secret.fromSecretsManager(apiDdlSecret, "password")
                        )
                )
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .streamPrefix("migration")
                        .logRetention(RetentionDays.ONE_WEEK)
                        .build()))
                .build());
    }

    public ISecret getApiDmlSecret() {
        return apiDmlSecret;
    }

}
