pipeline {
    agent any

    tools {
        jdk 'JDK21'
        maven 'Maven3'
    }

    options {
        skipDefaultCheckout(false)
    }

    stages {
        stage('Check Backend Changes') {
            steps {
                script {
                    def backendChanged = false

                    for (changeLogSet in currentBuild.changeSets) {
                        for (entry in changeLogSet.items) {
                            for (file in entry.affectedFiles) {
                                if (file.path.startsWith('lms-backend/')) {
                                    backendChanged = true
                                }
                            }
                        }
                    }

                    // Useful for manual "Build Now", first build, or when changelog is empty.
                    if (currentBuild.changeSets.size() == 0) {
                        echo 'No changelog found. This may be a manual build or first build.'
                        backendChanged = true
                    }

                    env.BACKEND_CHANGED = backendChanged.toString()
                    echo "Backend changed: ${env.BACKEND_CHANGED}"
                }
            }
        }

        stage('Compile Backend') {
            when {
                expression { return env.BACKEND_CHANGED == 'true' }
            }
            steps {
                dir('lms-backend') {
                    bat 'mvn -version'
                    bat 'mvn clean compile'
                }
            }
        }

        stage('Prioritise Backend Tests') {
            when {
                expression { return env.BACKEND_CHANGED == 'true' }
            }
            steps {
                dir('lms-backend') {
                    bat 'py --version'
                    bat 'py tools\\prioritize_tests.py'
                    bat 'type test-results\\selected-tests.txt'
                    archiveArtifacts artifacts: 'test-results/prioritization-report.json,test-results/selected-tests.txt', fingerprint: true
                }
            }
        }

        stage('Run Prioritised Backend Tests') {
            when {
                expression { return env.BACKEND_CHANGED == 'true' }
            }
            steps {
                dir('lms-backend') {
                    script {
                        def selectedTests = readFile('test-results/selected-tests.txt').trim()
                        echo "Running backend tests in prioritised order: ${selectedTests}"
                        bat "mvn test -Dtest=${selectedTests}"
                    }
                }
            }
            post {
                always {
                    dir('lms-backend') {
                        junit 'target/surefire-reports/*.xml'
                        archiveArtifacts artifacts: 'target/surefire-reports/*.xml', allowEmptyArchive: true
                    }
                }
            }
        }

        stage('Package Backend') {
            when {
                expression { return env.BACKEND_CHANGED == 'true' }
            }
            steps {
                dir('lms-backend') {
                    bat 'mvn package -DskipTests'
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Skip Backend Pipeline') {
            when {
                expression { return env.BACKEND_CHANGED != 'true' }
            }
            steps {
                echo 'Only frontend or non-backend files changed. Backend CI pipeline skipped.'
            }
        }
    }

    post {
        always {
            echo 'Pipeline completed.'
        }
    }
}
