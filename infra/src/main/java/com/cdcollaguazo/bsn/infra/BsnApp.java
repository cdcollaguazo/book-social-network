package com.cdcollaguazo.bsn.infra;

import com.cdcollaguazo.bsn.infra.config.Config;
import com.cdcollaguazo.bsn.infra.config.ConfigLoader;
import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;
import software.amazon.awscdk.BootstraplessSynthesizer;
import software.amazon.awscdk.StackProps;

public class BsnApp {

    public static void main(String[] args) {
        System.out.println("Initializing Infrastructure...");

        App app = new App(AppProps.builder().outdir("./cdk.out").build());

        // Add BootstraplessSynthesizer since we don't need to upload any assets
        // Only template creation is needed
        StackProps props = StackProps.builder()
                .synthesizer(new BootstraplessSynthesizer())
                .build();

        Config config = ConfigLoader.loadConfig();

        BsnMigrationStack bsnMigrationStack = new BsnMigrationStack(app, "BsnMigration", props, config);

        new BsnServiceStack(app, "BsnService", props, bsnMigrationStack.getApiDmlSecret(), config);

        app.synth();
    }

}
