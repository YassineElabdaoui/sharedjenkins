def call(Map params) {
    pipeline {
        agent any
        tools {
            maven "maven_3_9_5"
        }

        stages {
            stage('Checkout') {
                steps {
                    git url: params.repoUrl, branch: params.branch ?: 'main'
                }
            }
            stage('Build Maven') {
                steps {
                    dir(params.serviceDir) {
                        bat 'mvn clean install'
                    }
                }
            }
            stage('Build Docker image') {
                steps {
                    script {
                        dir(params.serviceDir) {
                            bat "docker build -t ${params.dockerImage} ."
                        }
                    }
                }
            }
            stage('Push image to hub') {
                steps {
                    script {
                        withCredentials([string(credentialsId: params.dockerCredentialsId, variable: 'dockerhubpwd')]) {
                            bat "docker login -u ${params.dockerUsername} -p ${dockerhubpwd}"
                        }
                        bat "docker push ${params.dockerImage}:latest"
                    }
                }
            }
        }
    }
}

