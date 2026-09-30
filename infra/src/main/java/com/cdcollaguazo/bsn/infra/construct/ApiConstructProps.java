package com.cdcollaguazo.bsn.infra.construct;

import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ecs.ICluster;
import software.amazon.awscdk.services.efs.IFileSystem;
import software.amazon.awscdk.services.elasticloadbalancingv2.IApplicationListener;
import software.amazon.awscdk.services.secretsmanager.ISecret;

public record ApiConstructProps(
        IVpc vpc,
        ISecurityGroup ecsSg,
        ICluster ecsCluster,
        IApplicationListener albListener,
        ISecret apiDmlSecret,
        IFileSystem efs,
        String rdsHost,
        String rdsPort
) {
}
