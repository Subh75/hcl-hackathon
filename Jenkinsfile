pipeline {
    agent any

    tools {
        maven 'Maven 3.9.6'
        nodejs 'NodeJS 18'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Microservices') {
            parallel {
                stage('Auth Service') {
                    steps {
                        dir('auth-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Payee Service') {
                    steps {
                        dir('payee-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Scoring Service') {
                    steps {
                        dir('bank-scoring-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('API Gateway') {
                    steps {
                        dir('api-gateway') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }

        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: '**/target/*.jar, frontend/dist/**', allowEmptyArchive: true
            }
        }
    }

    post {
        success {
            echo 'Build successful! Artifacts are ready for deployment.'
        }
        failure {
            echo 'Build failed. Please check the console output.'
        }
    }
}
