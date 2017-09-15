# aws-library

## Structure

### Simple Usage

    withCloudCredentials([credentials: 'aws-credentials']) {
        withLinuxInstance([...sizing...etc...]) {
            echo "Deploying to server ${PUBLIC_DNS_NAME}"
        }
    }
    
### Use Instances Parallel

    stage('Integration Test') {
        withCloudCredentials([credentials: 'aws-credentials']) {
            withLinuxInstance([waitOn: false, .....]) {
                def petclinic_instance = INSTANCE_ID
                withDBInstance {
                    def petclinic_server = waitOnLinuxInstance(petclinic_instance)
                    def petclinic_db_server = PUBLIC_DNS_NAME
                    echo "Deploying Petclinic™ to ${petclinic_server}, DB will run on ${petclinic_db_server}"
                }
            }
        }
    }

### Manual Control
  
    stage('Integration Test') {
        withCloudCredentials([credentials: 'aws-credentials']) {
            def instanceId = createLinuxInstance()
            def server = waitOnLinuxInstance(instanceId)
            echo "Doing something with ${server}"
            terminateLinuxInstance(instanceId)
        }
    }
