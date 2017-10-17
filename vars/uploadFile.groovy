def call(key, fileName) {
    withS3Instance.uploadFile('zicke', key, fileName)
}
