import de.comquent.awslibrary.EC2Instance

def call(params = null, body) {
def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config

    def instance = new EC2Instance()
    
    echo "Working on ${params}"
    
    body()
    
}
