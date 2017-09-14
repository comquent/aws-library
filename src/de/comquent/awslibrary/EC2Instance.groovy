package de.comquent.awslibrary

@Grab('org.jenkins-ci.plugins:jackson2-api:2.5.4')
@Grab('com.amazonaws:aws-java-sdk:1.11.119')
@GrabExclude(group = 'commons-codec', module = 'commons-codec')
@GrabExclude(group = 'com.fasterxml.jackson.core', module = 'jackson-databind')
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import com.amazonaws.services.ec2.model.RunInstancesRequest
import com.amazonaws.services.ec2.model.RunInstancesResult

class EC2Instance {

  public void create() {
    AmazonEC2Client ec2Client = AmazonEC2ClientBuilder.defaultClient()
    RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
    runInstancesRequest.withImageId('ami-9877a5f7').withInstanceType('m1.small')
        .withMinCount(1).withMaxCount(1)
        .withKeyName('Jenkins Training')
        .withSecurityGroups('Jenkins Master')
    RunInstancesResult result = ec2Client.runInstances(runInstancesRequest)
  }

}
