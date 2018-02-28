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
 * Call on the object.
 */
def call(params = null, body) {
    def config = [:]
    body.resolveStrategy = Closure.OWNER_FIRST
    body.delegate = config

    def instanceId = this.create()
    body.INSTANCE_ID = instanceId

    if (params?.waitOn in [null, true]) {
        body.PUBLIC_DNS_NAME = this.waitOn(instanceId)
        body.PRIVATE_DNS_NAME = this.privateDnsName(instanceId)
    }

    // Call closure
    try {
        body()
    }
    catch (e) {
        error e.message
    }
    finally {
        if (params?.terminate in [null, true]) {
            this.terminate(instanceId)
        }
    }
    
}


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
def create() {
    echo "Creating EC2 instance"

    RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
    runInstancesRequest.withImageId('ami-89e033e6').withInstanceType('t2.nano')
            .withMinCount(1).withMaxCount(1)
            .withKeyName('Voxxed Days Workshop')
            .withSecurityGroups(['Jenkins Training'])

    RunInstancesResult result = getEC2Client().runInstances(runInstancesRequest)
    instanceId = result.reservation.instances.first().instanceId
    echo "    Instance ID: ${instanceId}"
    instanceId
}


def privateDnsName(instanceId) {
    DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest()
    describeInstancesRequest.setInstanceIds([instanceId])

    DescribeInstancesResult describeInstancesResult = getEC2Client().describeInstances(describeInstancesRequest)
    def instance = describeInstancesResult.reservations.first().instances.first()
    instance.privateDnsName
}


/**
 * Wait for the instance to be in a full running state and accepts
 * SSH connections.
 * 
 * @param instanceId
 * 
 * @return
 * The public DNS name of the instance
 */
def waitOn(instanceId) {
    def publicDnsName

    echo "Waiting until instance ${instanceId} is up and accepts SSH connections"
    timeout(5) {
        waitUntil {
            DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest()
            describeInstancesRequest.setInstanceIds([instanceId])

            DescribeInstancesResult describeInstancesResult = getEC2Client().describeInstances(describeInstancesRequest)
            def instance = describeInstancesResult.reservations.first().instances.first()
            def state = instance.state
            publicDnsName = instance.publicDnsName
            echo "... State: ${state.name} (${state.code})"
            if (state.code == 16) {
                return true
            }
            sleep(time: 5)
            return false
        }
        waitUntil {
            try {
                Socket s = new Socket(publicDnsName, 22)
                s.close()
                echo "... SSH port active"
                return true
            }
            catch(ConnectException e) {
                echo "... SSH port not active"
                sleep(time: 5)
                return false
            }
        }
    }
    echo "    Public DNS name: ${publicDnsName}"
    publicDnsName
}


/**
 * Terminate an EC2 instance.
 * 
 * @param instanceId
 */
def terminate(instanceId) {
    TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest([instanceId])

    TerminateInstancesResult terminateInstancesResult = getEC2Client().terminateInstances(terminateInstancesRequest)
    List <InstanceStateChange> instanceStateChange = terminateInstancesResult.terminatingInstances
    def state = instanceStateChange.currentState
    echo "Terminating instance ID ${instanceId} has been triggered"
}
