package com.cdcollaguazo.bsn.infra;

import com.cdcollaguazo.bsn.infra.config.Config;
import com.cdcollaguazo.bsn.infra.construct.ApiConstruct;
import com.cdcollaguazo.bsn.infra.construct.ApiConstructProps;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ClusterAttributes;
import software.amazon.awscdk.services.ecs.ICluster;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationListener;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationListenerAttributes;
import software.amazon.awscdk.services.elasticloadbalancingv2.IApplicationListener;
import software.amazon.awscdk.services.secretsmanager.ISecret;
import software.amazon.awscdk.services.ssm.StringParameter;
import software.constructs.Construct;

import java.util.List;

public class BsnServiceStack extends Stack {

    private final String platformName;

    public BsnServiceStack(Construct scope, String id, StackProps props, ISecret apiDmlSecret, Config config) {
        super(scope, id, props);

         platformName = config.platformName();

        String vpcId = getValueForParameter("vpc", "vpc-id");

        String az1 = getValueForParameter("vpc", "az-1");
        String az2 = getValueForParameter("vpc", "az-2");

        String privateSubnet1Id = getValueForParameter("vpc", "private-subnet-1-id");
        String privateSubnet2Id = getValueForParameter("vpc", "private-subnet-2-id");

         IVpc vpc = Vpc.fromVpcAttributes(this, "Vpc", VpcAttributes.builder()
                 .vpcId(vpcId)
                 .availabilityZones(List.of(az1, az2))
                 .privateSubnetIds(List.of(privateSubnet1Id, privateSubnet2Id))
                 .build());

        String ecsSgId = getValueForParameter("vpc", "ecs-sg-id");
        ISecurityGroup ecsSg = SecurityGroup.fromSecurityGroupId(this, "EcsSg", ecsSgId);

        String albSgId = getValueForParameter("vpc", "alb-sg-id");
        ISecurityGroup albSg = SecurityGroup.fromSecurityGroupId(this, "AlbSg", albSgId);

        String ecsClusterArn = getValueForParameter("ecs", "cluster-arn");
        String ecsClusterName = getValueForParameter("ecs", "cluster-name");
        ICluster ecsCluster = Cluster.fromClusterAttributes(this, "EcsCluster", ClusterAttributes.builder()
                .vpc(vpc)
                .clusterArn(ecsClusterArn)
                .clusterName(ecsClusterName)
                .build());

        String albHttpListenerArn = getValueForParameter("alb", "http-listener-arn");
        IApplicationListener albListener = ApplicationListener.fromApplicationListenerAttributes(this, "AlbListener", ApplicationListenerAttributes.builder()
                .listenerArn(albHttpListenerArn)
                .securityGroup(albSg)
                .build());

        String rdsHost = getValueForParameter("rds", "instance-host");
        String rdsPort = getValueForParameter("rds", "instance-port");

        String efsId = getValueForParameter("efs", "file-system-id");

        ApiConstructProps apiConstructProps = new ApiConstructProps(vpc, ecsSg, ecsCluster, albListener, apiDmlSecret,
                efsId, rdsHost, rdsPort);

        new ApiConstruct(this, "Api", apiConstructProps, config);
    }

    private String getValueForParameter(String module, String parameter) {
        return StringParameter.valueForStringParameter(this,
                "/" + platformName + "/" + module + "/" + parameter);
    }

}
