@GrabResolver(name='maven-central', root='https://repo1.maven.org/maven2')
@Grab('com.amazonaws:aws-java-sdk:1.11.119')
@GrabExclude('commons-codec:commons-codec')
@GrabExclude('com.fasterxml.jackson.core:jackson-databind')
@Grab('bullshitorg.jenkins-ci.plugins:jackson2-api:2.5.4')
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import com.amazonaws.services.ec2.model.RunInstancesRequest
import com.amazonaws.services.ec2.model.RunInstancesResult

def call(params = null, body) {
def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    
    echo "Working on ${params}"
    AmazonEC2Client ec2Client = AmazonEC2ClientBuilder.defaultClient()
    RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
    runInstancesRequest.withImageId('ami-9877a5f7').withInstanceType('m1.small')
        .withMinCount(1).withMaxCount(1)
        .withKeyName('Jenkins Training')
        .withSecurityGroups('Jenkins Master')
    RunInstancesResult result = ec2Client.runInstances(runInstancesRequest)
    
    
    body()
    
    
}

