def call() {
    echo "Creating EC2 instance"
    def id = new Random().nextInt(1000)
    return "${id}"
}
