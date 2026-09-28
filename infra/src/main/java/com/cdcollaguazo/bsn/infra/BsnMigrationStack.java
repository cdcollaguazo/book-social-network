package com.cdcollaguazo.bsn.infra;

import com.cdcollaguazo.bsn.infra.config.Config;
import com.cdcollaguazo.bsn.infra.construct.DatabaseMigrationConstruct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.secretsmanager.ISecret;
import software.amazon.awscdk.services.secretsmanager.Secret;
import software.amazon.awscdk.services.ssm.StringParameter;
import software.constructs.Construct;

public class BsnMigrationStack extends Stack {

    private final ISecret apiDmlSecret;
    private final String platformName;

    public BsnMigrationStack(Construct scope, String id, StackProps props, Config config) {
        super(scope, id, props);

        platformName = config.platformName();

        String apiDmlSecretArn = StringParameter.valueForStringParameter(this, buildParameterName("secret-arn"));
        ISecret rdsSecret = Secret.fromSecretCompleteArn(this, "ApiDmlSecret", apiDmlSecretArn);

        String rdsEndpointAddress = StringParameter.valueForStringParameter(this,
                buildParameterName("instance-host"));

        DatabaseMigrationConstruct databaseMigrationConstruct = new DatabaseMigrationConstruct(this,
                "DatabaseMigration", rdsSecret, rdsEndpointAddress, config);

        apiDmlSecret = databaseMigrationConstruct.getApiDmlSecret();
    }

    private String buildParameterName(String parameter) {
        return "/" + platformName + "/rds/" + parameter;
    }

    public ISecret getApiDmlSecret() {
        return apiDmlSecret;
    }

}
