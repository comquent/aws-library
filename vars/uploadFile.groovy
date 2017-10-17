def call(key, fileName) {
    withS3Storage.uploadFile(STORAGE, key, fileName)
}
