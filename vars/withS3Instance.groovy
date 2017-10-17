import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder


def getS3Client() {
    def credentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretAccessKey))
    return AmazonS3ClientBuilder.standard().withCredentials(credentials).build()
}


def STORAGE = null

def getS() {
    'zicke'
}

/**
 * Call on the object.
 */
def call(params = null, body) {
    def config = [:]
    body.resolveStrategy = Closure.OWNER_FIRST
    body.delegate = config

    this.STORAGE = this.createStorage(params.name)
    body.STORAGE = this.STORAGE

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
    if (getS3Client().doesBucketExist(name)) {
        echo "Bucket ${name} does already exist."
        name
    } else {
        return getS3Client().createBucket(name).name
    }
}


def deleteStorage(name) {
    if (getS3Client().doesBucketExist(name)) {
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


def uploadFile(key, fileName) {
    this.uploadFile(this.getS(), key, fileName)
}
