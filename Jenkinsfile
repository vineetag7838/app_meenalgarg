pipeline{
    
    agent any
    
    environment{
        scannerHome = tool 'SonarQubeScanner'
        dockerUserName = 'meenalgarg2610'
        gitURL = 'https://github.com/vineetag7838/app_meenalgarg.git'
        sonarProjectName = 'sonar_meenalgarg'
        sonarURL = 'http://localhost:9000'
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
                git poll:true, url: gitURL

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
					bat "${scannerHome}/bin/sonar-scanner \
					-Dsonar.projectKey=${sonarProjectName} \
                    -Dsonar.projectName=${sonarProjectName} \
					-Dsonar.host.url=${sonarURL} \
					-Dsonar.java.binaries=target/classes \
                    -Dsonar.tests=src/test/java \
                    -Dsonar.sources=src/main/java \
                    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml"
                }
                echo 'checking if sonar quality gate passed'
                timeout(time: 10, unit: 'MINUTES') {
                waitForQualityGate abortPipeline: true
                }

            }
        }
		
        stage('Docker image'){
            steps{
                echo 'create docker image step'
                bat "docker build -t i-${dockerUserName}:${BUILD_NUMBER} --no-cache -f Dockerfile ."
            }
        }
		
        stage('Containers'){
            parallel{
                stage('Publish image to Docker hub'){
                    steps{
                        echo 'push image to docker hub step'
                        bat "docker tag i-${dockerUserName}:${BUILD_NUMBER} ${dockerUserName}/i-${dockerUserName}-${BRANCH_NAME}:${BUILD_NUMBER}"
				        bat "docker tag i-${dockerUserName}:${BUILD_NUMBER} ${dockerUserName}/i-${dockerUserName}-${BRANCH_NAME}:latest"                 
                        
                        withDockerRegistry(credentialsId: 'DockerHub', url: ''){
                        bat "docker push ${dockerUserName}/i-${dockerUserName}-${BRANCH_NAME}:${BUILD_NUMBER}"
				        //bat "docker push ${dockerUserName}/i-${dockerUserName}-${BRANCH_NAME}:latest"
                        }
                    }
                }
                stage('Pre-container check'){
                    steps{
				        echo 'Remove already running docker container'
				        script{
                            try{
                                echo 'stopping already running container'
                                bat "docker stop c-${dockerUserName}-${BRANCH_NAME}"

                                echo 'removing the old container'
                                bat "docker container rm c-${dockerUserName}-${BRANCH_NAME}"
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
                    def dockerPort
                    if(BRANCH_NAME == 'master'){
                        dockerPort = 7200
                    }
                    if(BRANCH_NAME == 'develop'){
                        dockerPort = 7300
                    }
                    bat "docker run --name c-${dockerUserName}-${BRANCH_NAME} -d -p ${dockerPort}:8100 ${dockerUserName}/i-${dockerUserName}-${BRANCH_NAME}:latest"
                }
			}
		}
		
		stage('Kubernetes Deployment'){
			steps{
                echo 'Kubernetes deployment step'
                script{
                    def kubernetesPort
                    def firewallRuleName
                    if(BRANCH_NAME == 'master'){
                        kubernetesPort = 30157
                        firewallRuleName = 'app-meenalgarg-master-node-port'
                    }
                    if(BRANCH_NAME == 'develop'){
                        kubernetesPort = 30158
                        firewallRuleName = 'app-meenalgarg-develop-node-port'
                    }
                    powershell "(Get-Content ${WORKSPACE}\\deployment-master.yaml).Replace('{{BRANCHNAME}}', '${BRANCH_NAME}').Replace('{{BUILDNUMBER}}', '${BUILD_NUMBER}').Replace('{{NODEPORT}}', '${kubernetesPort}') | Out-File ${WORKSPACE}\\deployment-master.yaml"
                    bat "kubectl apply -f deployment.yaml"
                    try{
                        bat "gcloud compute firewall-rules create ${firewallRuleName} --allow tcp:${kubernetesPort} --project sodium-burner-319611"
                    }catch(Exception e){
                        // nothing to be done here, just catching exception in case firewall rule already exists
                    }
                }
			}
		}        
    }
}
