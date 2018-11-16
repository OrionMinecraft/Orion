pipeline {
    agent any

    stages {
        stage('Checkout code') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh './gradlew --no-daemon build'
                archiveArtifacts 'OrionLauncher/build/libs/*-all.jar'
            }
        }
    }
}
