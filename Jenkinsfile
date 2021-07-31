pipeline{
    
    agent any
    
    environment{
        scannerHome = tool 'SonarQubeScanner'
        username = 'meenalgarg2610'
        docker_registry = 'meenalgarg2610/jenkins-multibranch-pipeline'
    }
    tools{
        maven 'Maven3'
    }
    // trigger for polling any change in github
    triggers {
      pollSCM('H/2 * * * *') 
    }         
    
    options{
        timestamps()
        timeout(time: 15, unit: 'MINUTES')
        skipDefaultCheckout()
        buildDiscarder(logRotator(
            numToKeepStr: '3',
            daysToKeepStr: '30'
            ))
        parallelsAlwaysFailFast()
    }
    
    stages{

        stage('Build'){
            steps{
                echo 'checkout code'
                echo 'pulling branch: - '+ BRANCH_NAME
                git poll:true, url: 'https://github.com/mgarg-03-05/app_meenalgarg.git'

                echo 'maven build'
                bat 'mvn clean package'
            }
        }
        
        stage('Unit Testing'){
            when {
                branch 'master'
            }
            steps{
                echo 'unit testing step'
                bat 'mvn test'
            }
        }
		
        stage('SonarQube analysis'){
             when {
                branch 'develop'
            }
            steps{
                echo 'sonarQube code analysis step'
                
                //Test_Sonar - name of configuration in jenkins
                withSonarQubeEnv('Test_Sonar') {
					bat "mvn clean verify sonar:sonar \
					-Dsonar.projectKey=sonar_meenalgarg2610 \
					-Dsonar.host.url=http://localhost:9000 \
					-Dsonar.java.binaries=target/classes "
                }
                sleep 10
                echo 'checking if sonar quality gate passed'
                // setup a web hook in sonarqube before executing this step
                timeout(time: 10, unit: 'MINUTES') {
                waitForQualityGate abortPipeline: true
                }

            }
        }
		
        stage('Docker image'){
            steps{
                echo 'create docker image step'
                bat "docker build -t i-${username}:${BUILD_NUMBER} --no-cache -f Dockerfile ."
            }
        }
		
        stage('Containers'){
            parallel{
                stage('Publish image to Docker hub'){
                    steps{
                        echo 'push image to docker hub step'
                        bat "docker tag i-${username}:${BUILD_NUMBER} ${username}/i-${username}-${env.BRANCH_NAME}:${BUILD_NUMBER}"
				        bat "docker tag i-${username}:${BUILD_NUMBER} ${username}/i-${username}-${env.BRANCH_NAME}:latest"                
                        
                        withDockerRegistry(credentialsId: 'DockerHub', url: ''){
                        bat "docker push ${username}/i-${username}-${env.BRANCH_NAME}:${BUILD_NUMBER}"
				        bat "docker push ${username}/i-${username}-${env.BRANCH_NAME}:latest"
                        }
                    }
                }
                stage('Pre-container check'){
                    steps{
				        echo 'Remove already running docker container'
				        script{
                            try{
                                echo 'stopping already running container'
                                bat "docker stop c-${username}-${env.BRANCH_NAME}"

                                echo 'removing the old container'
                                bat "docker container rm c-${username}-${env.BRANCH_NAME}"
                            }catch(Exception e){
                                // Nothing to be done here, added only to prevent the failure of pipeline 
                                //because when pipeline will run for the first time, 
                                //there won't be any container to remove
                            }
                        }
			        }
                }
            }
        }
		
		stage('Docker deployment'){
			steps{
				echo 'docker deployment step'
                script{
                    if(env.BRANCH_NAME == 'master'){
                        bat "docker run --name c-${username}-master -d -p 7200:8100 ${username}/i-${username}-master:latest"
                    }
                    if(env.BRANCH_NAME == 'develop'){
                        bat "docker run --name c-${username}-develop -d -p 7300:8100 ${username}/i-${username}-develop:latest"
                    }
                }
			}
		}
		
        //stage('Local Kubernetes Deployment'){
			//steps{
				//echo 'Deploying on local kubernetes'
				//bat 'kubectl apply -f deployment.yaml'
			//}
		//}
		
		// stage('Kubernetes Deployment'){
		// 	steps{
		// 		step([$class: 'KubernetesEngineBuilder', projectId: 'sodium-burner-319611', clusterName: 'demo-cluster', location: 'us-central1', manifestPattern: 'deployment-master.yaml', credentialsId: 'NAGP_jenkinsPipeline', verifyDeployment: true])

        //         step([$class: 'KubernetesEngineBuilder', projectId: 'sodium-burner-319611', clusterName: 'demo-cluster', location: 'us-central1', manifestPattern: 'deployment-develop.yaml', credentialsId: 'NAGP_jenkinsPipeline', verifyDeployment: true])
		// 	}
		// }        
    }
}