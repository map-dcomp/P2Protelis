#!/usr/bin/env groovy

pipeline {

	agent {
		label 'map-jenkins'
	}

	stages {
		stage('Init') {
			steps {
				echo "NODE_NAME = ${env.NODE_NAME}"
			}
		}
		
		stage('Build and Test') {
			steps {
                          wrap([$class: 'Xvfb']) {                          
				timestamps {
                                    sh "./continuous_integration/standard_build"
				}
                          }
			}
		}
                
	} // stages
		
	post {
		always {
                  //archiveArtifacts artifacts: ''
												
                    openTasks defaultEncoding: '', excludePattern: '', healthy: '', high: 'FIXME,HACK', low: '', normal: 'TODO', pattern: '**/*.java,**/*.sh,**/*.py', unHealthy: ''
			warnings categoriesPattern: '', consoleParsers: [[parserName: 'Java Compiler (javac)']], defaultEncoding: '', excludePattern: '', healthy: '', includePattern: '', messagesPattern: '', unHealthy: ''

                    findbugs pattern: '**/build/reports/findbugs/*.xml', unstableTotalAll: '0'

                    checkstyle pattern: '**/build/reports/checkstyle/*.xml', unstableTotalAll: '0'
                    
                    junit "**/build/test-results/**/*.xml"
                    
			emailext recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'CulpritsRecipientProvider']], 
					to: 'jon.schewe@raytheon.com',
					subject: '$DEFAULT_SUBJECT', 
					body: '''${PROJECT_NAME} - Build # ${BUILD_NUMBER} - ${BUILD_STATUS}

Branch: ${GIT_BRANCH}

Changes:
${CHANGES}

Failed Tests:
${FAILED_TESTS, onlyRegressions=false}

Check console output at ${BUILD_URL} to view the full results.

Tail of Log:
${BUILD_LOG, maxLines=50}

'''

		} // always
	} // post

} // pipeline
