def createEC2Instance() {
    echo "Creating EC2 instance"
    return "blabla"
}



def waitOnEC2Instance(instanceId) {
    echo "Waiting until instance is up"
}


def call(params = null, body) {
    def config = [:]
    body.resolveStrategy = Closure.TO_SELF
    body.delegate = config

    // Make methods in closure available
    body.waitOnEC2Instance = this.&waitOnEC2Instance
    body.createEC2Instance = this.&createEC2Instance

    withCredentials([
        usernamePassword(credentialsId: params.credentials, usernameVariable: 'accessKey', passwordVariable: 'secretAccessKey')
    ]) {
        body()
    }
}
