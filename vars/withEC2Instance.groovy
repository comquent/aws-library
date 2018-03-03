import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import com.amazonaws.services.ec2.model.RunInstancesRequest
import com.amazonaws.services.ec2.model.RunInstancesResult
import com.amazonaws.services.ec2.model.DescribeInstancesRequest
import com.amazonaws.services.ec2.model.DescribeInstancesResult
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest
import com.amazonaws.services.ec2.model.Reservation
import com.amazonaws.services.ec2.model.TerminateInstancesRequest
import com.amazonaws.services.ec2.model.TerminateInstancesResult
import com.amazonaws.services.ec2.model.InstanceStateChange
import com.amazonaws.services.ec2.waiters.AmazonEC2Waiters
import com.amazonaws.waiters.Waiter
import com.amazonaws.waiters.WaiterParameters

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

    // instance parameter
    def String imageId = params?.imageId != null ? params?.imageId : "ami-4b4e2224"
    def String instanceType = params?.instanceType != null ? params?.instanceType : "t2.nano"
    
    def instanceId = this.create(imageId, instanceType)
    body.INSTANCE_ID = instanceId
    body.PUBLIC_DNS_NAME = this.publicDnsName(instanceId)
    body.PRIVATE_DNS_NAME = this.privateDnsName(instanceId)

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
def create(String imageId = "ami-4b4e2224", String instanceType = "t2.nano") {
    echo "Creating EC2 instance"
    println " * imageId      = " + imageId
    println " * instanceType = " + instanceType

    RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
    runInstancesRequest.withImageId(imageId).withInstanceType(instanceType)
            .withMinCount(1).withMaxCount(1)
            .withKeyName('Jenkins Training')
            .withSecurityGroups(['Jenkins Master'])

    AmazonEC2Client client = getEC2Client();
  
    RunInstancesResult result = client.runInstances(runInstancesRequest)
    instanceId = result.reservation.instances.first().instanceId

    echo "InstanceID = " + instanceId
    DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest()
    describeInstancesRequest.setInstanceIds([instanceId])
    
    DescribeInstanceStatusRequest describeInstanceStatusRequest = new DescribeInstanceStatusRequest()
    describeInstanceStatusRequest.setInstanceIds([instanceId])
    
    AmazonEC2Waiters ec2waiter = new AmazonEC2Waiters(client)
    try{
        Waiter<DescribeInstancesRequest> waiterExists = ec2waiter.instanceExists();   
        waiterExists.run(new WaiterParameters<>(describeInstancesRequest))
        echo "Instance ID: ${instanceId} exist"
        
        Waiter<DescribeInstancesRequest> waiterRunning = ec2waiter.instanceRunning();   
        waiterRunning.run(new WaiterParameters<>(describeInstancesRequest))
        echo "Instance ID: ${instanceId} is running"

        Waiter<DescribeInstanceStatusRequest> waiterStatusOk = ec2waiter.instanceStatusOk();   
        waiterStatusOk.run(new WaiterParameters<>(describeInstanceStatusRequest))
        echo "Instance ID: ${instanceId} Status OK"
    }
    catch(Exception e){
        error "ERROR: " + e.message;
    }
    
    instanceId
}

def publicDnsName(instanceId) {
    DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest()
    describeInstancesRequest.setInstanceIds([instanceId])

    DescribeInstancesResult describeInstancesResult = getEC2Client().describeInstances(describeInstancesRequest)
    def instance = describeInstancesResult.reservations.first().instances.first()
    instance.publicDnsName
}

def privateDnsName(instanceId) {
    DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest()
    describeInstancesRequest.setInstanceIds([instanceId])

    DescribeInstancesResult describeInstancesResult = getEC2Client().describeInstances(describeInstancesRequest)
    def instance = describeInstancesResult.reservations.first().instances.first()
    instance.privateDnsName
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
