
@Grab('com.amazonaws:aws-java-sdk')

def call(params = null, body) {
def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    
    echo "Working on ${params}"
    
    body()
    
    
}

