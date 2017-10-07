/**
 * Call on the object.
 */
def call(params = null, body) {
    def config = [:]
    body.resolveStrategy = Closure.OWNER_FIRST
    body.delegate = config

    def instanceId = createEC2Instance()
    body.INSTANCE_ID = instanceId
    
    body.PUBLIC_DNS_NAME = waitOnEC2Instance(instanceId)

    // Call closure
    body()

    terminateEC2Instance(instanceId)
}

def t(wert) {
    echo "Terminating ${wert}"
}
