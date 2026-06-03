pipeline {
  agent any

  environment {
    DOCKER_IMAGE = "Bouba6/daef-portal-idp"
    GIT_CONFIG_REPO = "git@github.com:Bouba6/portail-k8s-config.git"
  }

  stages {
    stage('Build & Push Docker Image') {
      steps {
        script {
          def imageTag = "${env.BUILD_NUMBER}"
          withCredentials([usernamePassword(
            credentialsId: 'dockerhub-creds',
            usernameVariable: 'DOCKER_USER',
            passwordVariable: 'DOCKER_PASS'
          )]) {
            sh """
              docker build -t ${DOCKER_IMAGE}:${imageTag} ./daef-portal-idp
              echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin
              docker push ${DOCKER_IMAGE}:${imageTag}
            """
          }
          env.IMAGE_TAG = imageTag
        }
      }
    }

    stage('Update K8s Config Repo') {
      steps {
        withCredentials([sshUserPrivateKey(credentialsId: 'github-ssh', keyFileVariable: 'SSH_KEY')]) {
          sh """
            git clone ${GIT_CONFIG_REPO} k8s-config
            cd k8s-config
            sed -i 's|image: .*|image: ${DOCKER_IMAGE}:${env.IMAGE_TAG}|' deployment.yaml
            git config user.email "jenkins@ci.local"
            git config user.name "Jenkins"
            git add deployment.yaml
            git commit -m "ci: update image tag to ${env.IMAGE_TAG}"
            GIT_SSH_COMMAND="ssh -i $SSH_KEY" git push origin main
          """
        }
      }
    }
  }
}