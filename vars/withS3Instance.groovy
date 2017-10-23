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
def downloadFile(storageName, fileName) {
    def input = getS3Client().getObject(storageName, fileName).getObjectContent()
	
    byte[] buffer = new byte[8 * 1024]

    try {
        OutputStream output = new ByteArrayOutputStream()
        int bytesRead
        while ((bytesRead = input.read(buffer)) != -1) {
		println "${bytesRead}"
        //    output.write(buffer, 0, bytesRead);
        }
    } finally {
        input.close();
    }
}

@NonCPS
def XXXdownloadFile(storageName, fileName) {
    def input = getS3Client().getObject(storageName, fileName).getObjectContent()
	
    byte[] buffer = new byte[8 * 1024];

    (new File(fileName)).mkdirs()
	
    try {
        OutputStream output = new FileOutputStream(fileName);
        try {
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } finally {
            output.close();
        }
    } finally {
        input.close();
    }
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
