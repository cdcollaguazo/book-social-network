package com.cdcollaguazo.bsn.infra.construct;

import com.cdcollaguazo.bsn.infra.config.Config;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.efs.AccessPoint;
import software.amazon.awscdk.services.efs.Acl;
import software.amazon.awscdk.services.efs.PosixUser;
import software.amazon.awscdk.services.elasticloadbalancingv2.*;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class ApiConstruct extends Construct {

    private static final String CONTEXT = "bsn";

    public ApiConstruct(Construct scope, String id, ApiConstructProps props, Config config) {
        super(scope, id);

        // EFS Access Point
        AccessPoint efsAccessPoint = AccessPoint.Builder.create(this, "EfsAccessPoint")
                .fileSystem(props.efs())
                .path("/bsn/images")
                .createAcl(Acl.builder()
                        .ownerUid("1000")
                        .ownerGid("1000")
                        .permissions("755")
                        .build())
                .posixUser(PosixUser.builder()
                        .uid("1000")
                        .gid("1000")
                        .build())
                .build();

        // Volume
        Volume bsnVolume = Volume.builder()
                .name("bsn-api")
                .efsVolumeConfiguration(EfsVolumeConfiguration.builder()
                        .fileSystemId(props.efs().getFileSystemId())
                        .authorizationConfig(AuthorizationConfig.builder()
                                .accessPointId(efsAccessPoint.getAccessPointId())
                                .build())
                        .rootDirectory("/")
                        .build())
                .build();

        // Task Definition
        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder.create(this, "TaskDefinition")
                .family("bsn-api")
                .cpu(1024)
                .memoryLimitMiB(3072)
                .volumes(List.of(bsnVolume))
                .build();

        String jdbcUrl = "jdbc:postgresql://" + props.rdsHost() + ":" + props.rdsPort() + "/keycloak";

        // Container
        ContainerDefinition apiContainer = taskDefinition.addContainer("Container", ContainerDefinitionOptions.builder()
                .containerName("bsn-api")
                .essential(true)
                .image(ContainerImage.fromRegistry(config.apiImage()))
                .portMappings(List.of(
                        PortMapping.builder()
                                .containerPort(8080)
                                .hostPort(8080)
                                .protocol(software.amazon.awscdk.services.ecs.Protocol.TCP)
                                .build()
                ))
                .environment(
                        Map.of(
                                "DB_URL", jdbcUrl,
                                "JWT_ISSUER_URI", config.jwtIssuerUri(),
                                "JPA_DDL_AUTO", "validate"
                        )
                )
                .secrets(
                        Map.of(
                                "DB_USER", Secret.fromSecretsManager(props.apiDmlSecret(), "username"),
                                "DB_PASSWORD", Secret.fromSecretsManager(props.apiDmlSecret(), "password")
                        )
                )
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .streamPrefix("api")
                        .logRetention(RetentionDays.ONE_WEEK)
                        .build()))
                .build());

        apiContainer.addMountPoints(MountPoint.builder()
                .sourceVolume(bsnVolume.getName())
                .containerPath("/app/images")
                .readOnly(false)
                .build());

        // ECS
        FargateService ecs = FargateService.Builder.create(this, "Ecs")
                .serviceName("bsn-api")
                .cluster(props.ecsCluster())
                .taskDefinition(taskDefinition)
                .desiredCount(1)
                .availabilityZoneRebalancing(AvailabilityZoneRebalancing.ENABLED)
                .healthCheckGracePeriod(Duration.seconds(0))
                .deploymentStrategy(DeploymentStrategy.ROLLING)
                .minHealthyPercent(100)
                .maxHealthyPercent(200)
                .platformVersion(FargatePlatformVersion.LATEST)
                .enableExecuteCommand(false)
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                        .build())
                .securityGroups(List.of(props.ecsSg()))
                .assignPublicIp(false)
                .build();

        // ALB
        ApplicationTargetGroup targetGroup = ApplicationTargetGroup.Builder.create(this, "TargetGroup")
                .targetGroupName("bsn-api")
                .targetType(TargetType.IP)
                .protocol(ApplicationProtocol.HTTP)
                .port(8080)
                .ipAddressType(TargetGroupIpAddressType.IPV4)
                .vpc(props.vpc())
                .protocolVersion(ApplicationProtocolVersion.HTTP1)
                .healthCheck(HealthCheck.builder()
                        .protocol(software.amazon.awscdk.services.elasticloadbalancingv2.Protocol.HTTP)
                        .port("8080")
                        .path("/api/v1/ping")
                        .healthyThresholdCount(3)
                        .unhealthyThresholdCount(2)
                        .timeout(Duration.seconds(25))
                        .interval(Duration.seconds(30))
                        .healthyHttpCodes("200")
                        .build())
                .build();

        ecs.attachToApplicationTargetGroup(targetGroup);

        ApplicationListenerRule.Builder.create(this, "ALBListenerRule")
                .listener(props.albListener())
                .priority(200)
                .conditions(List.of(
                                ListenerCondition.pathPatterns(
                                        List.of("/" + CONTEXT , "/" + CONTEXT + "/*"))
                        )
                )
                .targetGroups(List.of(targetGroup))
                .build();
    }

}
