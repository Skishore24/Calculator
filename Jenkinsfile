pipeline {
    agent any

    tools {
        jdk 'Java-17'
        maven 'Maven-3.9'
    }

    options {
        timeout(time: 15, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code...'
            }
        }

        stage('Compile') {
            steps {
                echo 'Compiling Calculator Spring Boot project...'
                bat '.\\mvnw.cmd compile'
            }
        }

        stage('Unit Tests') {
            steps {
                echo 'Running JUnit 5 Test Suite...'
                bat '.\\mvnw.cmd test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging executable JAR...'
                bat '.\\mvnw.cmd package -DskipTests'
            }
        }

        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }

    post {
        success {
            echo 'Calculator build and tests completed successfully!'
        }
        failure {
            echo 'Build failed. Please inspect the console log.'
        }
    }
}
