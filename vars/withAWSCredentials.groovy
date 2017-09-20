def createEC2Instance() {
    echo "Creating EC2 instance"
    return "blabla"
}



def waitOnEC2Instance(instanceId) {
    echo "Waiting until instance is up"
}



def terminateEC2Instance(instanceId) {
    echo "Terminating instance ID ${instanceId} has been triggered"
}



def call(params = null, body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config

    // Make methods in closure available
    body.waitOnEC2Instance = this.&waitOnEC2Instance
    body.createEC2Instance = this.&createEC2Instance
    body.terminateEC2Instance = this.&terminateEC2Instance

    withCredentials([
        usernamePassword(credentialsId: params.credentials, usernameVariable: 'accessKey', passwordVariable: 'secretAccessKey')
    ]) {
        body()
    }
}
