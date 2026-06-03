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
        // 1. On utilise obligatoirement keyFileVariable pour une clé privée SSH
        withCredentials([sshUserPrivateKey(credentialsId: 'github-ssh', keyFileVariable: 'SSH_KEY_PATH')]) {
          sh """
            # Nettoyage du dossier temporaire
            rm -rf k8s-config

            # 2. Configuration de l'environnement SSH (On utilise env. pour être 100% explicite)
            export GIT_SSH_COMMAND="ssh -i \${SSH_KEY_PATH} -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no"

            # 3. Utilisation directe des variables d'environnement Jenkins (sans antislash pour Groovy)
            git clone ${env.GIT_CONFIG_REPO} k8s-config
            cd k8s-config
            
            # Modification du fichier deployment.yaml (version Mac)
            sed -i.bak "s|image: .*|image: boobabathily/daef-portal-idp:${env.IMAGE_TAG}|" deployment.yaml
            rm -f deployment.yaml.bak
            
            # Configuration Git de l'auteur du commit
            git config user.name "Jenkins-CI"
            git config user.email "jenkins@gogainde.com"
            
            # Commit et Envoi
            git add deployment.yaml
            git commit -m "chore: update image tag to ${env.IMAGE_TAG} [skip ci]"
            git push origin main
          """
        }
      }
    }
  }
}