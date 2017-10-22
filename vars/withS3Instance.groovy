import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder


@NonCPS
def getS3Client() {
    def credentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretAccessKey))
    AmazonS3ClientBuilder.standard().withCredentials(credentials).build()
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


def listFiles(storageName) {
    getS3Client().listObjects(storageName).getObjectSummaries().collect {
        it.key
    }
}


/**
  * @Todo Zum Schreiben den Kontext verwenden. So kommt im Moment nix auf Platte an.
  */
@NonCPS
def downloadFile(storageName, fileName) {
    def stream = getS3Client().getObject(storageName, fileName).getObjectContent()
    def fos = new FileOutputStream(new File(fileName))
    byte[] read_buf = new byte[1024]
    def read_len = 0
    while ((read_len = stream.read(read_buf)) > 0) {
        fos.write(read_buf, 0, read_len)
    }
    stream.close();
    fos.close();	
}


def deleteFile(storageName, fileName) {
	getS3Client().deleteObject(storageName, fileName)
}


def emptyStorage(storageName) {
    def s3Client = getS3Client()
    s3Client.listObjects(storageName).getObjectSummaries().each {
        println "Delete file name '${it.key}'"
        s3Client.deleteObject(storageName, it.key)
    }
}
