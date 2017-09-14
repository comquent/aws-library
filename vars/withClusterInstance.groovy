import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import com.amazonaws.services.ec2.model.RunInstancesRequest
import com.amazonaws.services.ec2.model.RunInstancesResult
import com.amazonaws.services.ec2.model.DescribeInstancesRequest

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.domains.DomainRequirement


def call(params = null, body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config

    echo "Credentials: ${params.credentials}"

    withCredentials([usernamePassword(credentialsId: params.credentials, usernameVariable: 'accessKey', passwordVariable: 'secretAccessKey')]) {

        println accessKey
        println secretAccessKey

        def credentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretAccessKey))

        AmazonEC2Client ec2Client = AmazonEC2ClientBuilder.standard().withCredentials(credentials).build()
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
        println "runInstancesRequest"
        runInstancesRequest.withImageId('ami-9877a5f7').withInstanceType('t2.small')
                .withMinCount(1).withMaxCount(1)
                .withKeyName('Jenkins Training')
                .withSecurityGroups(['Jenkins Master'])
        println "runInstances"
        RunInstancesResult result = ec2Client.runInstances(runInstancesRequest)
        println result
    }
            
        input(message: 'warte')

    withCredentials([usernamePassword(credentialsId: params.credentials, usernameVariable: 'accessKey', passwordVariable: 'secretAccessKey')]) {
            
        def reservation = result.getReservation()
        def instances = reservation.getInstances()
        def instanceIds = instances.collect { instance ->
            instance.getInstanceId()
        }

        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest()
        describeInstancesRequest.setInstanceIds(instanceIds)

        def describeInstancesResult = ec2Client.describeInstances(describeInstancesRequest)
        def reservations = describeInstancesResult.getReservations()
        reservations.each { res ->
            inst = res.getInstances()
            inst.each { i ->
                echo i.getPublicDnsName()
            }
        }

    }

    body()

}
