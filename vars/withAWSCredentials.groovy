import de.comquent.awslibrary.EC2Instance

def call(params = null, body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config

    withCredentials([
        usernamePassword(credentialsId: params.credentials, usernameVariable: 'accessKey', passwordVariable: 'secretAccessKey')
    ]) {
        def helloObject = new EC2Instance()
        body.hello = 'xxx' // helloObject.&hello
        body()
    }
}
