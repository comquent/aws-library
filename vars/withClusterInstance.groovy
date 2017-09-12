@GrabResolver(name='maven-central', root='https://repo1.maven.org/maven2')
@Grab('com.amazonaws:aws-java-sdk')
import com.amazonaws.services.s3.*

def call(params = null, body) {
def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    
    echo "Working on ${params}"
    AmazonEC2Client ec2Client = AmazonEC2ClientBuilder.defaultClient()
    RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
    runInstancesRequest.withImageId('ami-9877a5f7').withInstanceType('m1.small')
        .withMinCount(1).withMaxCount(1).
        .withKeyName('Jenkins Training')
        .withSecurityGroups('Jenkins Master')
    RunInstancesResult result = ec2Client.runInstances(runInstancesRequest)
    
    
    body()
    
    
}

