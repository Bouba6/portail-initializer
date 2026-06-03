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
              # Connexion à DockerHub
              echo "${DOCKER_PASS}" | docker login -u "${DOCKER_USER}" --password-stdin
              
              # Build et Push multi-plateforme (Compatible OpenShift AMD64 et ton Mac ARM64)
              docker buildx build --platform linux/amd64,linux/arm64 -t ${DOCKER_IMAGE}:${imageTag} --push ./daef-portal-idp
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

    stage('Deploy to OpenShift') {
      steps {
        // Jenkins récupère ton token de l'Action 1 de manière sécurisée
        withCredentials([string(credentialsId: 'openshift-token', variable: 'OCP_TOKEN')]) {
          sh """
            # 1. Connexion au cluster OpenShift de Heritage Africa
            oc login --token=\${OCP_TOKEN} --server=https://api.origins.heritage.africa:6443 --insecure-skip-tls-verify
            
            # 2. Sélection de ton projet actif
            oc project portail-mifass
            
            # 3. Entrée dans le dossier cloné
            cd k8s-config
            
            # 4. Déploiement des configurations
            oc apply -f deployment.yaml
            oc apply -f service-route.yaml
            
            # 5. Suivi du démarrage en direct dans la console Jenkins
            oc rollout status deployment/daef-portal-idp --timeout=2m
          """
        }
      }
    }
  }
}