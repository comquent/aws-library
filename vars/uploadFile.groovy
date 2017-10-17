def call(key, fileName) {
    body.resolveStrategy = Closure.OWNER_FIRST

    withS3Instance.uploadFile(delegate.STORAGE, key, fileName)
}
