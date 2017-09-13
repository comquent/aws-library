import de.comquent.awslibrary.EC2Instance

def call(params = null, body) {
def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config

    def instance = new EC2Instance()
    
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
