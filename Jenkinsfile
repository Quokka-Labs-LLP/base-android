pipeline {
    agent {
        // # JENKINS AGENT (BUILD SERVER)
        label 'Jenkins-Agent-1_QL_DEV-Server'
    }

    options {
        // # Setting timeout option to abort the build automatically if the build get's stuck
        timeout(time: 45, unit: 'MINUTES')
    }

    environment {
        SCANNER_HOME = tool 'SonarQubeScanner-V4.8.0.2856'

        WEBHOOK_URL = "https://quokkalabsllp.webhook.office.com/webhookb2/0790c749-18af-46ec-a453-e568cda0ba52@4821c368-51a8-493c-92ea-826dd01a89cc/JenkinsCI/00758520ae9745d3bb7f39673a7fc16b/05fbe5b2-8efd-4b4d-bf06-59990f3b2907"
        COLOUR_SUCCCESS = "#7B83EB" //Medium Slate Blue
        COLOUR_FAILURE = "#FFB316" //Spanish Yellow

        WDIR = "/jenkins-pipelines/${JOB_NAME}"
        BRANCHH = "development"
        TARGET_BRANCHH = "${CHANGE_TARGET}"

        REPO_OWNER = "automation-quokkalabs"
        REPO_NAME = "base-android"
        GITHUB_TOKEN_ID = "github_automation-quokkalabs"
        PROJECT_NAME = "base-android"
        WS_DIR = "${WDIR}/${PROJECT_NAME}"
    }

    stages {
        stage('SCM') {
        // # Pulling code from SCM
            when {
                expression {
                    targetbrachconfirm()
                }
            }
            steps{
                sh "mkdir -p ${WS_DIR}"
                dir ("${WS_DIR}") {
                    checkout scm
                }
            }
        }

        stage("SonarQube Analysis") {
        // # Performing Code Quality Check in jenkins agent's workspace
            when {
                expression {
                    targetbrachconfirm()
                }
            }
            steps {
                dir ("${WS_DIR}") {
                    withCredentials([string(credentialsId: 'ql-sonarqube-server-token', variable: 'SonarToken'), string(credentialsId: 'ql-sonarqube-global-webhook', variable: 'SonarWebHook')]) {
                        withSonarQubeEnv(installationName: 'QL-SONARQUBE-SERVER', credentialsId: 'ql-sonarqube-server-token') {
                            sh ''' $SCANNER_HOME/bin/sonar-scanner \
                                -Dsonar.projectKey=$PROJECT_NAME \
                                -Dsonar.java.binaries=build/classes/java/ \
                                -Dsonar.sources=.
                            '''
                            //-Dsonar.java.binaries=. \
                            //-Dsonar.exclusions=$EXCLUSION \
                        }
                    }
                }
            }
        }

        stage("SonarQube QualityGate") {
        // # Waiting for Code Quality Check Response from the Quality Gate Server
            when {
                expression {
                    targetbrachconfirm()
                }
            }

            steps {
                dir ("${WS_DIR}") {
                    withCredentials([string(credentialsId: 'ql-sonarqube-server-token', variable: 'SonarToken'), string(credentialsId: 'ql-sonarqube-global-webhook', variable: 'SonarWebHook')]) {
                        waitForQualityGate (abortPipeline: true, credentialsId: 'ql-sonarqube-server-token', webhookSecretId: 'ql-sonarqube-global-webhook')
                    }
                }
            }

/*            post {
                always {
                    // # Clean Workspace inside Jenkins Agent () if build fails.
                    cleanWs deleteDirs: true, disableDeferredWipeout: false, notFailBuild: true
                    script {
                        sh "rm -rf ${WDIR}"
                    }
                }
            }
*/        }

        stage("TEST") {
        // # Waiting for Code Quality Check Response from the Quality Gate Server
            when {
                expression {
                    targetbrachconfirm()
                }
            }

            steps {
                // Simulate Pipeline failure
                // # failure
                //sh 'ech "failure"'

                // # Success
                sh 'echo "success"'
            }
        }
    }

    post {
        // # Executes below commands in JENKINS AGENT (DEPLOYMENT SERVER) if build success
        success {
            // # Clean Workspace inside Jenkins Agent () if build fails.
            cleanWs deleteDirs: true, disableDeferredWipeout: false, notFailBuild: true

            script {
                sh "rm -rf ${WDIR}"
            }

            script {
                if ("${TARGET_BRANCHH}" == "${BRANCHH}") {
                    // # Sends out the notification to MSTeams channel about the build
                    office365ConnectorSend webhookUrl: "${WEBHOOK_URL}",
                        color : "${COLOUR_SUCCCESS}",
                        status: "${currentBuild.currentResult}",
                        message: "Sonar Code Scanning for the project - ${env.JOB_NAME} is successfully"
                    }
                else {
                    sh 'echo "Teams Notification cannot not be sent as PR target branch mismatch"'
                }
            }

            // # Sends out the notification through e-mails about the build
            //emailext to: 'ananda.yashaswi@quokkalabs.com, prem.shankar@quokkalabs.com, shashank.tripathi@quokkalabs.com, nisha.rana@quokkalabs.com',
                     // to: 'techteam@quokkalabs.com',
                     // recipientProviders: [buildUser(), contributor(), developers(), requestor(), upstreamDevelopers()],
                     //subject: "Jenkins build for ${env.JOB_NAME} is ${currentBuild.currentResult}",
                     //body:  "Jenkins build for the Job - ${env.JOB_NAME} is ${currentBuild.currentResult}. \n <br> More Info can be found here - ${env.BUILD_URL}",
                     //attachLog: false
        }

        // # Executes below commands in JENKINS AGENT (DEPLOYMENT SERVER) if build success
        failure {
            // # Clean Workspace inside Jenkins Agent () if build fails.
            cleanWs deleteDirs: true, disableDeferredWipeout: false, notFailBuild: true

            script {
                sh "rm -rf ${WDIR}"
            }

            script {
                if ("${TARGET_BRANCHH}" == "${BRANCHH}") {
                    // # Sends out the notification to MSTeams channel about the build
                    office365ConnectorSend webhookUrl: "${WEBHOOK_URL}",
                        color : "${COLOUR_FAILURE}",
                        status: "${currentBuild.currentResult}",
                        message: "Sonar Code Scanning for the project - ${env.JOB_NAME} is failure. For additional information, check here to check the log - (<${env.BUILD_URL}|Open>)"
                    }
                else {
                    sh 'echo "Teams Notification cannot not be sent as PR target branch mismatch"'
                }
            }

            // # Sends out the notification through e-mails about the build
            //emailext to: 'ananda.yashaswi@quokkalabs.com, prem.shankar@quokkalabs.com, shashank.tripathi@quokkalabs.com, nisha.rana@quokkalabs.com',
                     // to: 'ananda.yashaswi@quokkalabs.com, automation@quokkalabs.com',
                     // recipientProviders: [buildUser(), contributor(), culprits(), previous(), developers(), requestor(), brokenBuildSuspects(), brokenTestsSuspects(), upstreamDevelopers()],
                     //subject: "Jenkins build for ${env.JOB_NAME} is ${currentBuild.currentResult}",
                     //body: "Jenkins build for the Job - ${env.JOB_NAME} is ${currentBuild.currentResult}. \n <br> More Info can be found here - ${env.BUILD_URL}",
                     //attachLog: true
       }
    }
}

def targetbrachconfirm() {
    //def sourceBranch = env.CHANGE_SOURCE // Source branch of the pull request
    def targetBranch = env.CHANGE_TARGET // Target branch of the pull request

    // Check if the pull request is coming from 'source-branch' to 'target-branch'
    //return sourceBranch == 'source-branch' && targetBranch == 'target-branch'
    if (targetBranch == "development") {
        return targetBranch == "development"
    }

    else if (targetBranch == "staging") {
        return targetBranch == "staging"
    }

    else {
        //return "Other Branches";
    }

}
