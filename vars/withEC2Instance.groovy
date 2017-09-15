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
 * Wait for the instance to be in a full running state.
 * 
 * @param instanceId
 * 
 * @return
 * The public DNS name of the instance
 */
def waitOnEC2Instance(instanceId) {
    def publicDnsName

    echo "Waiting until instance is up"
    timeout(5) {
        waitUntil {
            DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest()
            describeInstancesRequest.setInstanceIds([instanceId])

            DescribeInstancesResult describeInstancesResult = getEC2Client().describeInstances(describeInstancesRequest)
            def instance = describeInstancesResult.reservations.first().instances.first()
            def state = instance.state
            publicDnsName = instance.getPublicDnsName()
            echo "... State: ${state.name} (${state.code})"
            if (state.code == 16) {
                return true
            }
            sleep(time: 5)
            return false
        }
    }
    echo "    Public DNS name: ${publicDnsName}"
    publicDnsName
}



/**
 * Call on the object.
 */
def call(params = null, body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config

    // Make methods in body available
    body.waitOnEC2Instance = this.&waitOnEC2Instance
    body.withEC2Instance = this.&call

    def instanceId

    withCredentials([
        usernamePassword(credentialsId: params.credentials, usernameVariable: 'accessKey', passwordVariable: 'secretAccessKey')
    ]) {

        echo "Creating EC2 instance"

        RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
        runInstancesRequest.withImageId('ami-9877a5f7').withInstanceType('t2.nano')
        .withMinCount(1).withMaxCount(1)
        .withKeyName('Jenkins Training')
        .withSecurityGroups(['Jenkins Master'])

        RunInstancesResult result = getEC2Client().runInstances(runInstancesRequest)
        instanceId = result.reservation.instances.first().instanceId

        echo "    Instance ID: ${instanceId}"

        body.INSTANCE_ID = instanceId
        body.SSH_PRIVATE_KEY = 'not yet implemented'

        if (params?.waitOn in [null, true]) {
            body.PUBLIC_DNS_NAME = waitOnEC2Instance(instanceId)
        }

        body()

        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest([instanceId])

        TerminateInstancesResult terminateInstancesResult = getEC2Client().terminateInstances(terminateInstancesRequest)
        List <InstanceStateChange> instanceStateChange = terminateInstancesResult.terminatingInstances
        def state = instanceStateChange.currentState
        echo "Terminating instance ID ${instanceId} has been triggered"
    }
}
