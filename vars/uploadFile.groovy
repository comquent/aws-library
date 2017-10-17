def call(key, fileName) {
    withS3Instance.uploadFile(withS3Instance.getS(), key, fileName)
}
