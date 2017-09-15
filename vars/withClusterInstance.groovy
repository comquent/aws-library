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


def call(params = null, body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config

    def INSTANCE_ID

    withCredentials([
        usernamePassword(credentialsId: params.credentials, usernameVariable: 'accessKey', passwordVariable: 'secretAccessKey')
    ]) {

        def credentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretAccessKey))
        AmazonEC2Client ec2Client = AmazonEC2ClientBuilder.standard().withCredentials(credentials).build()

        RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
        runInstancesRequest.withImageId('ami-9877a5f7').withInstanceType('t2.nano')
                .withMinCount(1).withMaxCount(1)
                .withKeyName('Jenkins Training')
                .withSecurityGroups(['Jenkins Master'])

        RunInstancesResult result = ec2Client.runInstances(runInstancesRequest)
        INSTANCE_ID = result.reservation.instances.first().instanceId

        echo "Instance ID: {INSTANCE_ID}"

        def PUBLIC_DNS_NAME

        timeout(5) {
            def state
            waitUntil {
                sleep(time: 5)

                def credentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretAccessKey))
                AmazonEC2Client ec2Client = AmazonEC2ClientBuilder.standard().withCredentials(credentials).build()

                DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest()
                describeInstancesRequest.setInstanceIds([INSTANCE_ID])

                DescribeInstancesResult describeInstancesResult = ec2Client.describeInstances(describeInstancesRequest)
                instance = describeInstancesResult.reservations.first().instances.first()
                state = instance.state
                PUBLIC_DNS_NAME = instance.publicDnsName
            }
            echo "State is ${state.name}"
            return state.code == 16
        }

        echo "Public DNS name: ${PUBLIC_DNS_NAME}"


        body()


        def credentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretAccessKey))
        AmazonEC2Client ec2Client = AmazonEC2ClientBuilder.standard().withCredentials(credentials).build()

        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest([INSTANCE_ID])

        TerminateInstancesResult terminateInstancesResult = ec2Client.terminateInstances(terminateInstancesRequest)
        List <InstanceStateChange> instanceStateChange = terminateInstancesResult.terminatingInstances
        def state = instanceStateChange.currentState
        echo "State is {state.name} / ${state.code}"
    }
}
