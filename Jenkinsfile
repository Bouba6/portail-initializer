pipeline {
  agent any

  environment {
    DOCKER_IMAGE = "boobabathily/daef-portal-idp"
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
              docker login -u "${DOCKER_USER}" -p "${DOCKER_PASS}"
              docker push ${DOCKER_IMAGE}:${imageTag}
            """
          }
          env.IMAGE_TAG = imageTag
        }
      }
    }

    stage('Update K8s Config Repo') {
        steps {
            withCredentials([sshUserPrivateKey(credentialsId: 'github-ssh', keyVariable: 'SSH_KEY')]) {
            sh """
                # Nettoyage préventif du dossier s'il existe déjà
                rm -rf k8s-config

                git clone ${GIT_CONFIG_REPO} k8s-config
                cd k8s-config
                
                # Ta commande sed compatible Mac
                sed -i.bak 's|image: .*|image: boobabathily/daef-portal-idp:${IMAGE_TAG}|' deployment.yaml
                rm -f deployment.yaml.bak
                
                git config user.name "Jenkins-CI"
                git config user.email "jenkins@gogainde.com"
                git add deployment.yaml
                git commit -m "chore: update image tag to ${IMAGE_TAG} [skip ci]"
                
                # Tes commandes de push SSH...
            """
            }
        }
        }
  }
}