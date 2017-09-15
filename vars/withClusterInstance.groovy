import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import com.amazonaws.services.ec2.model.RunInstancesRequest
import com.amazonaws.services.ec2.model.RunInstancesResult
import com.amazonaws.services.ec2.model.DescribeInstancesRequest
import com.amazonaws.services.ec2.model.DescribeInstancesResult
import com.amazonaws.services.ec2.model.Reservation

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

    withCredentials([usernamePassword(credentialsId: params.credentials, usernameVariable: 'accessKey', passwordVariable: 'secretAccessKey')]) {

        def credentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretAccessKey))
        AmazonEC2Client ec2Client = AmazonEC2ClientBuilder.standard().withCredentials(credentials).build()

        RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
        runInstancesRequest.withImageId('ami-9877a5f7').withInstanceType('t2.nano')
                .withMinCount(1).withMaxCount(1)
                .withKeyName('Jenkins Training')
                .withSecurityGroups(['Jenkins Master'])

        RunInstancesResult result = ec2Client.runInstances(runInstancesRequest)
        INSTANCE_ID = result.reservation.instances.first().instanceId
    }

    def PUBLIC_DNS_NAME
    def state

    timeout(5) {
        waitUntil {
            sleep(time: 10)
            withCredentials([usernamePassword(credentialsId: params.credentials, usernameVariable: 'accessKey', passwordVariable: 'secretAccessKey')]) {

                def credentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretAccessKey))
                AmazonEC2Client ec2Client = AmazonEC2ClientBuilder.standard().withCredentials(credentials).build()

                DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest()
                describeInstancesRequest.setInstanceIds([INSTANCE_ID])

                DescribeInstancesResult describeInstancesResult = ec2Client.describeInstances(describeInstancesRequest)
                instance = describeInstancesResult.reservations.first().instance.first()
                state = instance.state
                PUBLIC_DNS_NAME = instance.publicDnsName
            }
            echo "State is ${state.getName()}"
            return state.getCode() == 16
        }
    }

    echo "Public DNS name: ${PUBLIC_DNS_NAME}"
    echo "State: ${state}"

    body()

}
