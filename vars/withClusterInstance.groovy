
@Grab('com.amazonaws:aws-java-sdk')
import com.amazonaws.services.s3.AmazonS3Client

def call(params = null, body) {
def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    
    echo "Working on ${params}"
    
    body()
    
    
}

