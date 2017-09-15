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

AmazonEC2Client getEC2Client() {
    def credentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretAccessKey))
    echo accessKey
    echo secretAccessKey
    AmazonEC2ClientBuilder.standard().withCredentials(credentials).build()
}

def call(params = null, body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config

    def INSTANCE_ID

    withCredentials([
        usernamePassword(credentialsId: params.credentials, usernameVariable: 'accessKey', passwordVariable: 'secretAccessKey')
    ]) {

        RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
        runInstancesRequest.withImageId('ami-9877a5f7').withInstanceType('t2.nano')
                .withMinCount(1).withMaxCount(1)
                .withKeyName('Jenkins Training')
                .withSecurityGroups(['Jenkins Master'])

        RunInstancesResult result = getEC2Client().runInstances(runInstancesRequest)
        INSTANCE_ID = result.reservation.instances.first().instanceId

        echo "Instance ID: ${INSTANCE_ID}"

        def PUBLIC_DNS_NAME

        timeout(5) {
            waitUntil {
                sleep(time: 5)

                DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest()
                describeInstancesRequest.setInstanceIds([INSTANCE_ID])

                DescribeInstancesResult describeInstancesResult = getEC2Client().describeInstances(describeInstancesRequest)
                def instance = describeInstancesResult.reservations.first().instances.first()
                def state = instance.state
                echo "State is ${state.name}"
                return state.code == 16
                PUBLIC_DNS_NAME = instance.publicDnsName
            }
        }

        echo "Public DNS name: ${PUBLIC_DNS_NAME}"


        body()


        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest([INSTANCE_ID])

        TerminateInstancesResult terminateInstancesResult = getEC2Client().terminateInstances(terminateInstancesRequest)
        List <InstanceStateChange> instanceStateChange = terminateInstancesResult.terminatingInstances
        def state = instanceStateChange.currentState
        echo "State is ${state.name} / ${state.code}"
    }
}
