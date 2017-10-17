def call(key, fileName) {
    withS3Instance.uploadFile(key, fileName)
}
