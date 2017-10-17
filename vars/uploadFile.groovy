def call(key, fileName) {
    withS3Instance.uploadFile(STORAGE, key, fileName)
}
