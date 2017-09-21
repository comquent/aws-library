import de.comquent.awslibrary.EC2Instance

def call(params = null, body) {
    def config = [:]
    body.resolveStrategy = Closure.OWNER_FIRST
    body.delegate = config
    body.hello = this.&hello

    withCredentials([
        usernamePassword(credentialsId: params.credentials, usernameVariable: 'accessKey', passwordVariable: 'secretAccessKey')
    ]) {
        body()
    }
}

def hello = {
    println "HELLO"
}
