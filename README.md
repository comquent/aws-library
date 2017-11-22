# aws-library

## Structure

### Simple Usage

    withAWSCredentials([credentials: 'aws-credentials']) {
        withEC2Instance([...sizing...etc...]) {
            echo "Deploying to server ${PUBLIC_DNS_NAME}"
        }
    }
    
### Use Instances Parallel

    withAWSCredentials([credentials: 'aws-credentials']) {
        withEC2Instance([waitOn: false, .....]) {
            def petclinic_instance = INSTANCE_ID
            withEC2Instance {
                def petclinic_server = withEC2Instance.waitOn(petclinic_instance)
                def petclinic_db_server = PUBLIC_DNS_NAME
                echo "Deploying Petclinic™ to ${petclinic_server}, DB will run on ${petclinic_db_server}"
            }
        }
    }

### Manual Control
  
    withAWSCredentials([credentials: 'aws-credentials']) {
        def instanceId = withEC2Instance.create()
        def server = withEC2Instance.waitOn(instanceId)
        echo "Doing something with ${server}"
        withEC2Instance.terminate(instanceId)
    }

### Extended example

    withAWSCredentials([credentials: 'aws-credentials']) {
        def petclinic_instance_id
        withEC2Instance([waitOn: false, terminate: false]) {
            petclinic_instance_id = INSTANCE_ID
            withEC2Instance() {
                def petclinic_server = withEC2Instance.waitOn(petclinic_instance_id)
                def petclinic_db_server = PUBLIC_DNS_NAME
                node {
                    echo "Deploying Petclinic™ to ${petclinic_server}, DB will run on ${petclinic_db_server}"
                }
            }
        }
        withEC2Instance.terminate(petclinic_instance_id)
    }
