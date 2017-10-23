import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder

import com.amazonaws.services.s3.model.ObjectMetadata

import hudson.FilePath


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
def XXXuploadFile(storageName, key, fileName) {
    def content = readFile(fileName)
    getS3Client().putObject(storageName, key, content)
}


def uploadFile(storageName, key, fileName) {
    try {
        // if(build.workspace.isRemote())
        // def fp = new FilePath(Jenkins.getInstance().getComputer(NODE_NAME).getChannel(), "${WORKSPACE}/${fileName}")
        // else
        def fp = new FilePath(new File("${WORKSPACE}/${fileName}"))
        def inputStream = fp.read()
        getS3Client().putObject(storageName, key, inputStream, new ObjectMetadata())
    } finally {
        input.close()
    }
   
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
    try {
        // if(build.workspace.isRemote())
        // def fp = new FilePath(Jenkins.getInstance().getComputer(NODE_NAME).getChannel(), "${WORKSPACE}/${fileName}")
        // else
        def fp = new FilePath(new File("${WORKSPACE}/${fileName}"))
        fp.copyFrom(input)
    } finally {
        input.close()
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
