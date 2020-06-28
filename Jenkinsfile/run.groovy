pipeline {
    agent{node('master')}
    stages {
        stage('Clean workspace and download git repository') {
            steps {
                script {
                    cleanWs()
                    withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        try {
                            sh "echo '${password}' | sudo -S docker stop mz"
                            sh "echo '${password}' | sudo -S docker container rm mz"
                        } catch (Exception e) {
                            print 'container not exist, skip clean'
                        }
                    }
                }
                script {
                    echo 'Download project'
                    checkout([$class                           : 'GitSCM',
                              branches                         : [[name: '*/master']],
                              doGenerateSubmoduleConfigurations: false,
                              extensions                       : [[$class           : 'RelativeTargetDirectory',
                                                                   relativeTargetDir: 'auto']],
                              submoduleCfg                     : [],
                              userRemoteConfigs                : [[credentialsId: '	MariaZaicevaGit', url: 'https://github.com/zaiceva-m/devops.git']]])
                }
            }
        }
        stage ('Create docker image'){
            steps{
                script{
                     withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {

                        sh "echo '${password}' | sudo -S docker build ${WORKSPACE}/auto -t mz_nginx"
                        sh "echo '${password}' | sudo -S docker run -d -p 8118:80 --name mz -v /home/adminci/study_ansible/Zaiceva:/result mz_nginx"
                    }
                }
            }
        }
        stage ('Write result in the file'){
            steps{
                script{
                    withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        
                        sh "echo '${password}' | sudo -S docker exec -t mz bash -c 'df -h > /result/result.txt'"
                        sh "echo '${password}' | sudo -S docker exec -t mz bash -c 'top -n 1 -b >> /result/result.txt'"
                    }
                }
            }
        }
        
    }

    
}
