import de.comquent.awslibrary.EC2Instance

def call(params = null, body) {
    def config = [:]
    body.resolveStrategy = Closure.OWNER_FIRST
    body.delegate = config

    withCredentials([
        usernamePassword(credentialsId: params.credentials, usernameVariable: 'accessKey', passwordVariable: 'secretAccessKey')
    ]) {
        def helloObject = new EC2Instance()
        body.hello = helloObject.&hello
        body()
    }
}
