pipeline {
 
    agent any
 
    environment {
 
        // ============================================================


        // JAVA


        // ============================================================
 
        JAVA_HOME = 'C:/Program Files/Java/jdk-17.0.2'
 
        // ============================================================


        // SPRING BOOT


        // ============================================================
 
        APP_JAR = 'target/quiz-backend.jar'
 
        BACKEND_PORT = '8080'
 
        BACKEND_URL =
            'http://localhost:8080/api/quizzes'
 
        // ============================================================


        // TOMCAT / APPZILLON


        // ============================================================
 
        APPZ_HOME =


            'D:/SoftwarePath/apache-tomcat-9.0.53'
 
        APPZ_ARTIFACTS =


            'D:/build'
 
        TOMCAT_PORT = '8018'
 
        APPZILLON_URL =


            'http://localhost:8018/QuizFr/'


    }
 
 
    stages {
 
        // ============================================================


        // CHECKOUT


        // ============================================================
 
        stage('Checkout') {
 
            steps {
 
                echo '=========================================='


                echo 'CHECKING OUT QUIZAPP'


                echo '=========================================='
 
                git branch: 'main',


                    url: 'https://github.com/roshan-i-exceed/quiz-bg.git'
 
                echo 'QUIZAPP CHECKOUT SUCCESSFUL'


            }


        }
 
        // ============================================================


        // BUILD BACKEND


        // ============================================================
 
        stage('Build Backend Jar') {
 
            steps {
 
                echo '=========================================='


                echo 'KILLING OLD PROCESSES'


                echo '=========================================='
 
                bat '''


                    @echo off


                    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (


                        echo Killing process %%a on port 8080


                        taskkill /F /PID %%a >nul 2>&1


                    )


                    ping 127.0.0.1 -n 3 >nul


                '''
 
                echo '=========================================='


                echo 'STARTING MAVEN BUILD'


                echo '=========================================='
 
                bat 'mvn -f quiz-backend\\pom.xml clean package -DskipTests'
 
                echo '=========================================='


                echo 'CHECKING JAR'


                echo '=========================================='
 
                bat 'dir quiz-backend\\target\\*.jar'


            }


        }
 
        // ============================================================


        // DEPLOY BACKEND


        // ============================================================
 
        stage('Deploy Backend') {
 
            steps {
 
                bat 'if not exist "quiz-backend\\target\\quiz-backend.jar" (echo ERROR: JAR NOT FOUND && exit /b 1)'
 
                echo 'QuizBackend JAR found'
 
                bat '''


                    @echo off
 
                    REM CHECK PORT 8080
 
                    echo.


                    echo CHECKING PORT 8080


                    echo ==========================================
 
                    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (


                        echo Stopping process %%a on port 8080


                        taskkill /F /PID %%a >nul 2>&1


                    )
 
                    echo WAITING FOR PORT 8080


                    ping 127.0.0.1 -n 4 >nul
 
                    REM START BACKEND
 
                    echo.


                    echo STARTING QUIZAPP


                    echo ==========================================
 
                    set "JAVA_HOME=C:\\Program Files\\Java\\jdk-17.0.2"


                    set "PATH=%JAVA_HOME%\\bin;%PATH%"


                    set "JENKINS_NODE_COOKIE=dontKillMe"
 
                    start "QuizApp-Backend" /B cmd /c ^


                    "set JENKINS_NODE_COOKIE=dontKillMe && java -jar quiz-backend\\target\\quiz-backend.jar > backend.log 2>&1"
 
                    echo QUIZBACKEND START COMMAND EXECUTED


                    echo WAITING FOR APPLICATION TO START
 
                    ping 127.0.0.1 -n 6 >nul
 
                    echo.


                    echo BACKEND LOG:


                    if exist backend.log (


                        powershell -Command "Get-Content backend.log -Tail 20"


                    ) else (


                        echo backend.log not found


                    )


                '''


            }


        }
 
        // ============================================================


        // BACKEND HEALTH CHECK


        // ============================================================
 
        stage('Backend Health Check') {
 
            steps {
 
                bat '''


                    @echo off
 
                    echo ==========================================


                    echo CHECKING QUIZAPP BACKEND


                    echo ==========================================
 
                    echo.


                    echo Backend URL:


                    echo %BACKEND_URL%
 
                    echo.
 
                    set RETRIES=20
 
                    :CHECK_BACKEND
 
                    echo Checking backend...


                    echo Remaining attempts: %RETRIES%
 
                    curl -s -o nul -w "%%{http_code}" "%BACKEND_URL%" | findstr "200 201"
 
                    if not errorlevel 1 (
 
                        echo.


                        echo ==========================================


                        echo BACKEND IS RUNNING


                        echo ==========================================
 
                        echo Backend URL:


                        echo %BACKEND_URL%
 
                        exit /b 0


                    )
 
                    echo.


                    echo Backend not ready.
 
                    set /a RETRIES-=1
 
                    if %RETRIES% LEQ 0 (
 
                        echo.


                        echo ==========================================


                        echo BACKEND FAILED TO START


                        echo ==========================================
 
                        echo.


                        echo BACKEND LOG


                        echo ==========================================
 
                        if exist backend.log (
 
                            type backend.log
 
                        ) else (
 
                            echo backend.log not found
 
                        )
 
                        exit /b 1


                    )
 
                    echo Waiting 3 seconds before retry...
 
                    ping 127.0.0.1 -n 4 >nul
 
                    goto CHECK_BACKEND


                '''


            }


        }
 
        // ============================================================


        // DEPLOY APPZILLON


        // ============================================================
 
        stage('Deploy Appzillon') {
 
            steps {
 
                bat '''


                    @echo off
 
                    echo ==========================================


                    echo DEPLOYING APPZILLON QUIZAPP


                    echo ==========================================
 
                    REM CHECK WAR


                    echo CHECKING QUIZAPP WAR


                    if not exist "%APPZ_ARTIFACTS%\\QuizApp.war" (


                        echo ERROR: QuizApp.war not found at %APPZ_ARTIFACTS%\\QuizApp.war


                        exit /b 1


                    )


                    echo QuizApp.war found.
 
                    REM CHECK TOMCAT


                    echo.


                    echo TOMCAT HOME: %APPZ_HOME%


                    if not exist "%APPZ_HOME%\\bin\\catalina.bat" (


                        echo ERROR: catalina.bat not found


                        exit /b 1


                    )
 
                    REM STOP TOMCAT on port 8111


                    echo.


                    echo STOPPING TOMCAT


                    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8111 ^| findstr LISTENING') do (


                        echo Killing PID %%a


                        taskkill /F /PID %%a >nul 2>&1


                    )


                    ping 127.0.0.1 -n 4 >nul
 
                    REM REMOVE OLD APP


                    echo.


                    echo REMOVING OLD QUIZAPP


                    rmdir /S /Q "%APPZ_HOME%\\webapps\\QuizApp" >nul 2>&1


                    del /F /Q "%APPZ_HOME%\\webapps\\QuizApp.war" >nul 2>&1
 
                    REM COPY WAR


                    echo.


                    echo COPYING QUIZAPP.WAR


                    copy /Y "%APPZ_ARTIFACTS%\\QuizApp.war" "%APPZ_HOME%\\webapps\\QuizApp.war"


                    if errorlevel 1 (


                        echo ERROR COPYING QuizApp.war


                        exit /b 1


                    )


                    echo QuizApp.war copied.
 
                    REM START TOMCAT


                    echo.


                    echo STARTING TOMCAT


                    set "JAVA_HOME=C:\\Program Files\\Java\\jdk-17.0.2"


                    set "PATH=%JAVA_HOME%\\bin;%PATH%"


                    set "CATALINA_HOME=%APPZ_HOME%"


                    set "JENKINS_NODE_COOKIE=dontKillMe"
 
                    echo Running: "%APPZ_HOME%\\bin\\catalina.bat" start


                    "%APPZ_HOME%\\bin\\catalina.bat" start
 
                    echo TOMCAT START COMMAND EXECUTED


                    echo WAITING 15 SECONDS FOR TOMCAT TO BOOT


                    ping 127.0.0.1 -n 16 >nul
 
                    echo.


                    echo CHECKING PORT 8111


                    netstat -ano | findstr :8111 | findstr LISTENING


                    if errorlevel 1 (


                        echo WARNING: Port 8111 not listening yet


                    ) else (


                        echo Port 8111 is listening


                    )
 
                    echo.


                    echo TOMCAT LOG (last 30 lines):


                    if exist "%APPZ_HOME%\\logs\\catalina.out" (


                        powershell -Command "Get-Content '%APPZ_HOME%\\logs\\catalina.out' -Tail 30"


                    ) else if exist "%APPZ_HOME%\\logs\\jenkins-run.log" (


                        powershell -Command "Get-Content '%APPZ_HOME%\\logs\\jenkins-run.log' -Tail 30"


                    ) else (


                        echo No Tomcat log found


                        dir "%APPZ_HOME%\\logs\\" 2>nul


                    )


                '''


            }


        }
 
        // ============================================================


        // APPZILLON HEALTH CHECK


        // ============================================================
 
        stage('Appzillon Health Check') {
 
            steps {
 
                bat '''


                    @echo off
 
                    echo ==========================================


                    echo CHECKING APPZILLON


                    echo ==========================================


                    echo URL: %APPZILLON_URL%
 
                    set RETRIES=30
 
                    :CHECK_APPZILLON
 
                    echo.


                    echo Checking... attempts left: %RETRIES%
 
                    curl -s -o nul -w "%%{http_code}" "%APPZILLON_URL%" | findstr "200 302 404"
 
                    if not errorlevel 1 (


                        echo.


                        echo ==========================================


                        echo APPZILLON IS RUNNING


                        echo ==========================================


                        echo URL: %APPZILLON_URL%


                        exit /b 0


                    )
 
                    set /a RETRIES-=1
 
                    if %RETRIES% LEQ 0 (


                        echo.


                        echo ==========================================


                        echo APPZILLON FAILED TO START


                        echo ==========================================
 
                        echo PORT 8111 STATUS:


                        netstat -ano | findstr :8111
 
                        echo.


                        echo TOMCAT LOG:


                        if exist "%APPZ_HOME%\\logs\\catalina.out" (


                            powershell -Command "Get-Content '%APPZ_HOME%\\logs\\catalina.out' -Tail 30"


                        ) else if exist "%APPZ_HOME%\\logs\\jenkins-run.log" (


                            powershell -Command "Get-Content '%APPZ_HOME%\\logs\\jenkins-run.log' -Tail 30"


                        ) else (


                            echo No log found


                        )
 
                        exit /b 1


                    )
 
                    ping 127.0.0.1 -n 6 >nul


                    goto CHECK_APPZILLON


                '''


            }


        }


    }
 
    // ============================================================


    // POST ACTIONS


    // ============================================================
 
    post {
 
        success {
 
            echo '=========================================='


            echo 'QUIZAPP DEPLOYMENT SUCCESSFUL'


            echo '=========================================='
 
            echo 'Backend:'


            echo 'http://localhost:8080'
 
            echo 'Appzillon:'


            echo 'http://localhost:8111/QuizApp/'
 
            echo '=========================================='


        }
 
        failure {
 
            echo '=========================================='


            echo 'QUIZAPP DEPLOYMENT FAILED'


            echo '=========================================='
 
            echo 'Check the stage that failed.'
 
            echo '=========================================='


        }


    }


}
