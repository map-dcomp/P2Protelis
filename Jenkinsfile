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
                                      sh "./gradlew -Dtest.ignoreFailures=true --continue --no-daemon --gradle-user-home " + gradleRepo() + " -Dmaven.repo.local=" + mavenRepo() + " clean build check"
                                    }
				}
			    }
			}
		}

		stage('Gather tool results') {
		        // any post build steps that can fail need to be here to ensure that the email is sent out in the end
			steps {
                          recordIssues \
                              qualityGates: [[threshold: 1, type: 'TOTAL', unstable: true]], \
                              tools: [spotBugs(pattern: '**/build/reports/spotbugs/*.xml'), \
			              checkStyle(pattern: '**/build/reports/checkstyle/*.xml')]
                    
			  junit testResults: "**/build/test-results/**/*.xml", keepLongStdio: false
			  
                          recordIssues tool: taskScanner(excludePattern: 'gradle-repo/**,maven-repo/**', includePattern: '**/*.java,**/*.sh,**/*.py', highTags: 'FIXME,HACK', normalTags: 'TODO')
  		 
                          recordIssues tool: java()
			}
		}

		stage('Archive artifacts') {
		        steps {
			    archiveArtifacts artifacts: '**/*.log,**/build/reports/**,**/build/test-results/**'
			}
		}

	} // stages
		
	post {
		always {
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
