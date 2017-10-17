def call(key, fileName) {
    withS3Instance.uploadFile(INSTANCE, key, fileName)
}
