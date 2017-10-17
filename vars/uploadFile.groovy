def call(key, fileName) {
    withS3Instance.uploadFile(delegate.STORAGE, key, fileName)
}
