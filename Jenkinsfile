/* Requires the Docker Pipeline plugin */
pipeline {
     agent {
        docker {
//             image 'alpine/git:2.43.0'
//             image 'openjdk:17-ea-oraclelinux8'
            image 'gradle:8-jdk17-focal'
        }
    }
//     agent any

    tools {
        // Install the Gradle version configured as "gradle-8.0.2" and add it to the path.
        gradle "gradle-8.0.2"
    }

    // https://e.printstacktrace.blog/jenkins-pipeline-environment-variables-the-definitive-guide/
    // https://www.cnblogs.com/bymo/p/7601519.html
    // https://www.unix.com/shell-programming-and-scripting/98834-search-replace-using-awk-variables.html#2
    environment {
        // some variables have been put to Jenkins -> System -> Environment variables
//         NEXUS_CREDENTIALS_ID = 'nexus3-deployer-maven'
        // NEXUS_CREDENTIALS_ID = 'nexus3-admin'

        // NEXUS_HOSTNAME = 'localhost:38081'
//         NEXUS_HOSTNAME = 'nexus3:8081'

//         GITHUB_CREDENTIALS_ID = 'github-siukatech'
        GITHUB_PROJECT_NAME = 'poc-react-backend-parent'
        GITHUB_PROJECT_URL = "https://github.com/siukatech/poc-react-backend-parent.git"
//         GITHUB_PROJECT_URL = "${GIT_URL}"

//         GIT_USERNAME = 'siukatech'
//         GIT_REPO_NAME = 'poc-react-backend-parent'

    }

    stages {
        stage('set-up') {
            steps {
                sh '''
                    printenv
                '''

                echo "WORKSPACE: ${WORKSPACE}"
                echo "BUILD_NUMBER: ${BUILD_NUMBER}"

                // Get some code from a GitHub repository
                git(
                    branch: 'main',
                    changelog: false,
                    credentialsId: GITHUB_CREDENTIALS_ID,
                    poll: false,
                    url: GITHUB_PROJECT_URL
//                     url: GIT_URL
                )

                script {
                    def versionInfo = sh(script: "./gradlew properties | grep 'version' | awk '{print \$2}'",
                        returnStdout: true)?.trim()
                    env.versionInfo = versionInfo
                    // echo "versionInfo: ${versionInfo}"

                    def artifactId = sh(script: "./gradlew properties | grep 'name' | awk '{print \$2}'",
                        returnStdout: true)?.trim()
                    env.artifactId = artifactId

                    env.groupId = sh(script: "./gradlew properties | grep 'group' | awk '{print \$2}'",
                        returnStdout: true)?.trim()
                    // env.groupId = groupId

                    env.tagSuffix = sh(script: '''
                        date +"%Y%m%d-%H%M%S-%Z"
                    ''', returnStdout: true)?.trim()

//                     env.projectName = sh(
// //                         script: "./gradlew properties | grep 'projectDir' | awk '{n=split(\$2,a,\'/\'); print a[n]}'",
//                         script: '''
// //                             gradle properties | grep 'projectDir' | awk '{n=split($2,a,"/"); print a[n]}'
//                             echo "${GITHUB_PROJECT_URL}" | awk '{n=split($2,a,"/"); print a[n]}'
//                         ''',
//                         returnStdout: true)?.trim()

                    withCredentials([
                        usernamePassword(
                            credentialsId: GITHUB_CREDENTIALS_ID,
                            passwordVariable: 'password',
                            usernameVariable: 'username'
                        )
                    ]) {
                        env.gitUsername = username
                        env.gitPassword = password
                    }

                    withCredentials([
                        usernamePassword(
                            credentialsId: NEXUS_CREDENTIALS_ID,
                            passwordVariable: 'password',
                            usernameVariable: 'username'
                        )
                    ]) {
                        env.nexusUsername = username
                        env.nexusPassword = password
                    }

//                     env.projectUrl = "${GITHUB_HOSTNAME}/${gitUsername}/${projectName}.git"

                }

                echo "currentStage: ${STAGE_NAME}"
                echo "groupId: ${groupId}"
                echo "artifactId: ${artifactId}"
                echo "versionInfo: ${versionInfo}"
                echo "tagSuffix: ${tagSuffix}"
                echo "gitUsername: ${gitUsername}"
//                 echo "projectName: ${projectName}"
//                 echo "projectUrl: ${projectUrl}"
//                 echo "nexusUsername: ${nexusUsername}"
//                 echo "nexusPassword: ${nexusPassword}"
            }
        }

        stage('build') {
            steps {
//                 echo "currentStage: ${STAGE_NAME}"
//                 echo "groupId: ${groupId}"
//                 echo "artifactId: ${artifactId}"
//                 echo "versionInfo: ${versionInfo}"

                // Run gradle on a Unix agent.
                sh "./gradlew build"
                // sh "./gradlew clean build"

//                 sh "ls -la build/**/*.jar"

                sh "cp \$(ls build/**/*.jar) build/"

                sh "ls -la build/*.jar"
            }

//             post {
//                 // If Maven was able to run the tests, even if some of the test
//                 // failed, record the test results and archive the jar file.
//                 success {
//                     junit '**/target/surefire-reports/TEST-*.xml'
//                     archiveArtifacts 'target/*.jar'
//                 }
//             }
        }

//         stage('archive') {
//             steps {
//                 sh "ls -la build/**/*.jar"
//                 sh "ls -la ${WORKSPACE}/build/**/*.jar"
//
//                 archiveArtifacts artifacts: 'build/**/*.jar', onlyIfSuccessful: true
//             }
//         }

        stage('publish') {
            steps {
                sh "ls -la ${WORKSPACE}/build/*.jar"
                // sh "ls -la ${WORKSPACE}/build/**/*.jar"

//                 echo "currentStage: ${STAGE_NAME}"
//                 echo "groupId: ${groupId}"
//                 echo "artifactId: ${artifactId}"
//                 echo "versionInfo: ${versionInfo}"

                // script {
                //     // Find built artifact under target folder
                //     def filesByGlob = findFiles(glob: "build/**/*.jar");
                //     // Print some info from the artifact found
                //     echo "${filesByGlob[0].name} ${filesByGlob[0].path} ${filesByGlob[0].directory} ${filesByGlob[0].length} ${filesByGlob[0].lastModified}"
                //     // Extract the path from the File found
                //     env.artifactPath = filesByGlob[0].path;
                //     // Assign to a boolean response verifying If the artifact name exists
                //     env.artifactExists = fileExists artifactPath;
                // }
                // echo "artifactPath: ${artifactPath}"
                // echo "artifactExists: ${artifactExists}"

                nexusArtifactUploader (
                    artifacts: [
                        [artifactId: artifactId,
                        classifier: '',
                        file: 'build/' + artifactId + '-' + versionInfo + '.jar',
                        // file: artifactPath,
                        type: 'jar']
                    ],
                    credentialsId: NEXUS_CREDENTIALS_ID,
                    groupId: groupId,
                    nexusUrl: NEXUS_HOSTNAME,
                    nexusVersion: 'nexus3',
                    protocol: 'http',
                    repository: 'maven-snapshots',
                    version: versionInfo
                )
            }
        }

        stage('tag') {
            steps {
                script {
                    env.tagName = 'release-' + "${versionInfo}" + '-' + "${tagSuffix}" + '-' + "${BUILD_NUMBER}"
                    echo "tagName: ${tagName}"

                    withCredentials([
                        usernamePassword(
                            credentialsId: GITHUB_CREDENTIALS_ID,
                            passwordVariable: 'password',
                            usernameVariable: 'username'
                        )
                    ]) {

                        // https://stackoverflow.com/a/52492319
//                     echo "USERNAME:PASSWORD@aaa" | awk -v srch="USERNAME" -v repl="$COMMAND_MODE" -v pw="${TERM}" '{sub(srch,repl,$0); sub(/PASSWORD/,pw,$0); print $0}'
//                         remoteUrl = sh(
//                             script: "echo \"${GITHUB_REMOTE_URL}/${gitUsername}/${GITHUB_PROJECT_NAME}.git\" | awk -v un=\"${gitUsername}\" -v pw=\"${gitPassword}\" '{sub(/USERNAME/,un,\$0); sub(/PASSWORD/,pw,\$0);print \$0}'",
//                             returnStdout: true)?.trim()

//                             git config --global user.email "user.email is not required"
//                             git push https://${username}:${password}@github.com/${username}/${GITHUB_PROJECT_NAME}.git --tags
//                             git push origin ${tagName}
                        sh('''
                            git config --global user.email "${GITHUB_CREDENTIALS_EMAIL}"
                            git config --global user.name "${username}"
                            git tag -a ${tagName} -m "Release version ${tagName}"
                            git push https://${username}:${password}@github.com/${username}/${GITHUB_PROJECT_NAME}.git --tags
                        ''')

                    }

                }
            }
        }

    }

    // https://www.jenkins.io/doc/pipeline/tour/post/
    post {
        always {
            echo 'One way or another, I have finished'
            deleteDir() /* clean up our workspace */
        }
        success {
            echo 'I succeeded!'
        }
    }

}
