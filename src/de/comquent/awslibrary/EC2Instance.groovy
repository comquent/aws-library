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

}
