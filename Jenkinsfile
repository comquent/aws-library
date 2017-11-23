pipeline {
  agent any
  stages {
    stage('Checkout') {
      steps {
        git(url: 'https://github.com/comquent/spring-petclinic.git', branch: 'master')
        deleteDir()
      }
    }
  }
}