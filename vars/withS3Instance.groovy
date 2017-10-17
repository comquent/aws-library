def getS3Client() {
		def credentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretAccessKey))
		return AmazonS3ClientBuilder.standard().withCredentials(credentials).build()
}


/**
 * Call on the object.
 */
def call(params = null, body) {
    def config = [:]
    body.resolveStrategy = Closure.OWNER_FIRST
    body.delegate = config

    def instanceId = this.create()
    body.INSTANCE_ID = instanceId

    // Call closure
    body()
}


def getStorages() {
    def names = []
	  buckets = client.listBuckets()
	  buckets.each{
  		names << it
	  }
    return names
}
