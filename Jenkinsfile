pipeline {
    agent any

    // Check GitHub for new commits about every 2 minutes and build when something changed
    triggers {
        pollSCM('H/2 * * * *')
    }

    options {
        timestamps()
        // Never run two deployments at the same time
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        IMAGE_NAME   = 'lakshyasachan/calculator'
        // Read by docker-compose.yml, so Compose deploys exactly the image built in this run
        IMAGE_TAG    = "${env.BUILD_NUMBER}"
        NOTIFY_EMAIL = 'lakshya.sachan.a@gmail.com'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                // A failing test makes mvn exit non-zero, which fails the build
                sh 'mvn -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t $IMAGE_NAME:$IMAGE_TAG -t $IMAGE_NAME:latest .'
            }
        }

        stage('Push') {
            steps {
                // Docker Hub login comes from the Jenkins Credentials store, never from the repo
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                  usernameVariable: 'DOCKERHUB_USER',
                                                  passwordVariable: 'DOCKERHUB_TOKEN')]) {
                    sh '''
                        echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USER" --password-stdin
                        docker push $IMAGE_NAME:$IMAGE_TAG
                        docker push $IMAGE_NAME:latest
                    '''
                }
            }
            post {
                always {
                    sh 'docker logout'
                }
            }
        }

        stage('Deploy') {
            steps {
                // Only containers whose image or config changed are recreated, so a re-run
                // leaves a running deployment alone
                sh 'docker compose up -d --no-build'

                // Wait for the API container to report healthy, then check the path through Nginx
                sh '''
                    STATUS=starting
                    for i in $(seq 1 30); do
                        STATUS=$(docker inspect -f '{{.State.Health.Status}}' $(docker compose ps -q calculator))
                        echo "calculator health: $STATUS"
                        [ "$STATUS" = "healthy" ] && break
                        sleep 5
                    done
                    if [ "$STATUS" != "healthy" ]; then
                        docker compose logs --tail=50 calculator
                        exit 1
                    fi
                    docker compose exec -T nginx wget -qO- http://127.0.0.1/actuator/health
                    docker compose exec -T nginx wget -qO- "http://127.0.0.1/api/v1/add?a=2&b=3"
                '''
            }
        }
    }

    post {
        always {
            emailext(
                to: "${NOTIFY_EMAIL}",
                subject: "${currentBuild.currentResult}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """Build ${currentBuild.currentResult}: ${env.JOB_NAME} #${env.BUILD_NUMBER}

Image: ${IMAGE_NAME}:${IMAGE_TAG}
Details: ${env.BUILD_URL}
"""
            )
        }
    }
}
