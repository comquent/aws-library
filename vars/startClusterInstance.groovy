
def call(body) {
    // Any valid steps can be called from this code, just like in other
    // Scripted Pipeline
    echo "Hello."
    body()
}

