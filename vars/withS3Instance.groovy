import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder


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

    body.STORAGE = this.createStorage()

    // Call closure
    body()
}


def getStorages() {
    def names = []
    buckets = getS3Client().listBuckets()
    buckets.collect {
        it.name
    }
}


def createStorage(name) {
    return getS3Client().createBucket(name)
}


def deleteStorage(name) {
	if (client.doesBucketExist(name)) {
        echo "Delete bucket ${name}."
		getS3Client().deleteBucket(name)
    } else {
        echo "Bucket ${name} does not exist."
    }
}

/**
  * @todo
  * Filehandle aus dem Context via Jenkins Api holen.
  */
def uploadFile(storageName, key, fileName) {
	def content = readFile(fileName)
	getS3Client().putObject(storageName, key, content)
}