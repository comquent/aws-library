import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import com.amazonaws.services.ec2.model.RunInstancesRequest
import com.amazonaws.services.ec2.model.RunInstancesResult
import com.amazonaws.services.ec2.model.DescribeInstancesRequest
import com.amazonaws.services.ec2.model.DescribeInstancesResult
import com.amazonaws.services.ec2.model.Reservation
import com.amazonaws.services.ec2.model.TerminateInstancesRequest
import com.amazonaws.services.ec2.model.TerminateInstancesResult
import com.amazonaws.services.ec2.model.InstanceStateChange

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.domains.DomainRequirement



/**
 * Helper method for common code.
 */
AmazonEC2Client getEC2Client() {
    def credentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretAccessKey))
    AmazonEC2ClientBuilder.standard().withCredentials(credentials).build()
}



/**
 * Create an EC2 instance.
 * 
 * @return
 * The Id of the instance
 */
def createEC2Instance() {
    echo "Creating EC2 instance"
    return "blabla"
}



/**
 * Wait for the instance to be in a full running state.
 * 
 * @param instanceId
 * 
 * @return
 * The public DNS name of the instance
 */
def waitOnEC2Instance(instanceId) {
    echo "Waiting until instance is up"
}



/**
 * Terminate an EC2 instance.
 * 
 * @param instanceId
 */
def terminateEC2Instance(instanceId) {
    echo "Terminating instance ID ${instanceId} has been triggered"
}



/**
 * Call on the object.
 */
def call(params = null, body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config

    // Make methods in closure available
    body.waitOnEC2Instance = this.&waitOnEC2Instance
    body.createEC2Instance = this.&createEC2Instance
    body.terminateEC2Instance = this.&terminateEC2Instance
    body.withEC2Instance = this.&call

    withCredentials([
        usernamePassword(credentialsId: params.credentials, usernameVariable: 'accessKey', passwordVariable: 'secretAccessKey')
    ]) {
        def instanceId = createEC2Instance()
        body.INSTANCE_ID = instanceId

        body.PUBLIC_DNS_NAME = waitOnEC2Instance(instanceId)

        // Call closure
        body()

        terminateEC2Instance(instanceId)
    }
}
