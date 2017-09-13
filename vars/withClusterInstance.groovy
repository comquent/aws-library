import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import com.amazonaws.services.ec2.model.RunInstancesRequest
import com.amazonaws.services.ec2.model.RunInstancesResult

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicSessionCredentials

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.domains.DomainRequirement


def call(params = null, body) {
def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config

    echo "Working on ${params}"

    StandardUsernamePasswordCredentials usernamePasswordCredentials =
        CredentialsProvider.findCredentialById('aws-credentials',
		    StandardUsernamePasswordCredentials.class, currentBuild.rawBuild, Collections.<DomainRequirement>emptyList())
        
	def accessKey = usernamePasswordCredentials.getUsername()
    def secretAccessKey = usernamePasswordCredentials.getPassword().getPlainText()
    def sessionToken = null
                    
    def credentials = new AWSStaticCredentialsProvider(new BasicSessionCredentials(accessKey, secretAccessKey, sessionToken))
    
    AmazonEC2Client ec2Client = AmazonEC2ClientBuilder.standard().withCredentials(credentials).build()
    RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
    runInstancesRequest.withImageId('ami-9877a5f7').withInstanceType('m1.small')
        .withMinCount(1).withMaxCount(1)
        .withKeyName('Jenkins Training')
        .withSecurityGroups('Jenkins Master')
    RunInstancesResult result = ec2Client.runInstances(runInstancesRequest)
	println result
	
    
    body()
    
}
