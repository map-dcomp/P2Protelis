#!/usr/bin/env groovy

pipeline {

    options { buildDiscarder(logRotator(numToKeepStr: '10')) }
    
	agent {
		label 'map-jenkins'
	}

	stages {
		stage('Init') {
			steps {
				echo "NODE_NAME = ${env.NODE_NAME}"
			}
		}
		
		stage('Sloccount') {
		    steps {
		        sh "cloc --by-file --xml --out=cloc.xml src"
			sloccountPublish pattern: 'cloc.xml' 
		    }
		}

		stage('Build and Test') {
			steps {
                          wrap([$class: 'Xvfb']) {                          
				timestamps {
                                    timeout(time: 1, unit: 'HOURS') {
                                      sh "./gradlew --continue --no-daemon --gradle-user-home " + gradleRepo() + " -Dmaven.repo.local=" + mavenRepo() + " clean build check"
                                    }
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
					to: 'FILL-IN-EMAIL-ADDRESS',
					subject: '$DEFAULT_SUBJECT', 
					body: '''${PROJECT_NAME} - Build # ${BUILD_NUMBER} - ${BUILD_STATUS}

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

def gradleRepo() {
 "${WORKSPACE}/gradle-repo"
}

def mavenRepo() {
 "${WORKSPACE}/maven-repo"
}
