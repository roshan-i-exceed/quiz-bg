
pipeline {

    agent any

    environment {

        // ============================================================
        // JAVA
        // ============================================================

        JAVA_HOME = 'C:/Program Files/Java/jdk-17.0.2'

        // ============================================================
        // SPRING BOOT BACKEND
        // ============================================================

        APP_JAR = 'target/quiz-backend.jar'
        BACKEND_PORT = '8080'
        BACKEND_URL = 'http://localhost:8080/api/quizzes'

        // ============================================================
        // TOMCAT / APPZILLON
        // ============================================================

        APPZ_HOME = 'D:/SoftwarePath/apache-tomcat-9.0.53'
        APPZ_ARTIFACTS = 'D:/build'

        TOMCAT_PORT = '8018'

        APPZILLON_URL = 'http://localhost:8018/QuizFr/'
    }


    stages {

        // ============================================================
        // BUILD BACKEND
        // ============================================================

        stage('Build Backend Jar') {

            steps {

                echo '=========================================='
                echo 'BUILDING QUIZAPP BACKEND'
                echo '=========================================='


                // ----------------------------------------------------
                // SET JAVA
                // ----------------------------------------------------

                bat '''
                    @echo off

                    set "JAVA_HOME=C:\\Program Files\\Java\\jdk-17.0.2"
                    set "PATH=%JAVA_HOME%\\bin;%PATH%"

                    echo JAVA VERSION
                    java -version

                    echo.
                    echo MAVEN VERSION
                    mvn -version
                '''


                // ----------------------------------------------------
                // CHECK PROJECT
                // ----------------------------------------------------

                echo '=========================================='
                echo 'CHECKING MAVEN PROJECT'
                echo '=========================================='

                bat '''
                    @echo off

                    echo Current workspace:
                    cd

                    echo.
                    echo Checking pom.xml...

                    if not exist "pom.xml" (
                        echo ERROR: pom.xml not found in workspace
                        echo.
                        echo Workspace contents:
                        dir
                        exit /b 1
                    )

                    echo pom.xml found successfully.
                '''


                // ----------------------------------------------------
                // STOP OLD BACKEND
                // ----------------------------------------------------

                echo '=========================================='
                echo 'KILLING OLD BACKEND PROCESS'
                echo '=========================================='

                bat '''
                    @echo off

                    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (

                        echo Killing process %%a on port 8080

                        taskkill /F /PID %%a >nul 2>&1
                    )

                    ping 127.0.0.1 -n 3 >nul
                '''


                // ----------------------------------------------------
                // MAVEN BUILD
                // ----------------------------------------------------

                echo '=========================================='
                echo 'STARTING MAVEN BUILD'
                echo '=========================================='

                bat '''
                    @echo off

                    set "JAVA_HOME=C:\\Program Files\\Java\\jdk-17.0.2"
                    set "PATH=%JAVA_HOME%\\bin;%PATH%"

                    mvn clean package -DskipTests

                    if errorlevel 1 (

                        echo.
                        echo ==========================================
                        echo MAVEN BUILD FAILED
                        echo ==========================================

                        exit /b 1
                    )

                    echo.
                    echo ==========================================
                    echo MAVEN BUILD SUCCESSFUL
                    echo ==========================================
                '''


                // ----------------------------------------------------
                // CHECK GENERATED JAR
                // ----------------------------------------------------

                echo '=========================================='
                echo 'CHECKING GENERATED JAR'
                echo '=========================================='

                bat '''
                    @echo off

                    if not exist "target\\quiz-backend.jar" (

                        echo ERROR: target\\quiz-backend.jar NOT FOUND

                        echo.
                        echo Target directory contents:

                        if exist target (
                            dir target
                        ) else (
                            echo target directory does not exist
                        )

                        exit /b 1
                    )

                    echo.
                    echo ==========================================
                    echo QUIZBACKEND JAR FOUND
                    echo ==========================================

                    dir target\\*.jar
                '''
            }
        }


        // ============================================================
        // DEPLOY BACKEND
        // ============================================================

        stage('Deploy Backend') {

            steps {

                echo '=========================================='
                echo 'DEPLOYING QUIZAPP BACKEND'
                echo '=========================================='


                bat '''
                    @echo off

                    if not exist "%WORKSPACE%\\target\\quiz-backend.jar" (

                        echo ERROR: JAR NOT FOUND
                        echo Expected:
                        echo %WORKSPACE%\\target\\quiz-backend.jar

                        exit /b 1
                    )


                    echo.
                    echo QuizBackend JAR found.


                    // ------------------------------------------------
                    // STOP OLD PROCESS
                    // ------------------------------------------------

                    echo.
                    echo ==========================================
                    echo CHECKING PORT 8080
                    echo ==========================================


                    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (

                        echo Stopping process %%a on port 8080

                        taskkill /F /PID %%a >nul 2>&1
                    )


                    echo.
                    echo Waiting for port 8080...

                    ping 127.0.0.1 -n 4 >nul


                    // ------------------------------------------------
                    // START BACKEND
                    // ------------------------------------------------

                    echo.
                    echo ==========================================
                    echo STARTING QUIZAPP BACKEND
                    echo ==========================================


                    set "JAVA_HOME=C:\\Program Files\\Java\\jdk-17.0.2"
                    set "PATH=%JAVA_HOME%\\bin;%PATH%"
                    set "JENKINS_NODE_COOKIE=dontKillMe"


                    echo Starting:

                    echo java -jar target\\quiz-backend.jar


                    start "QuizApp-Backend" /B cmd /c ^
                    "set JENKINS_NODE_COOKIE=dontKillMe && java -jar target\\quiz-backend.jar > backend.log 2>&1"


                    echo.
                    echo BACKEND START COMMAND EXECUTED


                    echo.
                    echo Waiting for application to start...

                    ping 127.0.0.1 -n 6 >nul


                    // ------------------------------------------------
                    // SHOW LOG
                    // ------------------------------------------------

                    echo.
                    echo ==========================================
                    echo BACKEND LOG
                    echo ==========================================


                    if exist backend.log (

                        powershell -Command "Get-Content backend.log -Tail 30"

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

                echo '=========================================='
                echo 'CHECKING QUIZAPP BACKEND'
                echo '=========================================='


                bat '''
                    @echo off


                    echo.
                    echo Backend URL:
                    echo %BACKEND_URL%


                    echo.
                    echo Backend Port:
                    echo %BACKEND_PORT%


                    set RETRIES=20


                    :CHECK_BACKEND

                    echo.
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
                        echo ==========================================
                        echo PORT 8080 STATUS
                        echo ==========================================

                        netstat -ano | findstr :8080


                        echo.
                        echo ==========================================
                        echo BACKEND LOG
                        echo ==========================================


                        if exist backend.log (

                            type backend.log

                        ) else (

                            echo backend.log not found

                        )


                        exit /b 1
                    )


                    echo.
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

                echo '=========================================='
                echo 'DEPLOYING APPZILLON QUIZAPP'
                echo '=========================================='


                bat '''
                    @echo off


                    // ------------------------------------------------
                    // CHECK WAR
                    // ------------------------------------------------

                    echo.
                    echo ==========================================
                    echo CHECKING QUIZAPP WAR
                    echo ==========================================


                    if not exist "%APPZ_ARTIFACTS%\\QuizApp.war" (

                        echo ERROR:
                        echo QuizApp.war not found.

                        echo Expected:
                        echo %APPZ_ARTIFACTS%\\QuizApp.war

                        exit /b 1
                    )


                    echo QuizApp.war found.


                    // ------------------------------------------------
                    // CHECK TOMCAT
                    // ------------------------------------------------

                    echo.
                    echo ==========================================
                    echo CHECKING TOMCAT
                    echo ==========================================


                    echo TOMCAT HOME:
                    echo %APPZ_HOME%


                    if not exist "%APPZ_HOME%\\bin\\catalina.bat" (

                        echo ERROR:
                        echo catalina.bat not found.

                        exit /b 1
                    )


                    echo Tomcat installation found.


                    // ------------------------------------------------
                    // STOP TOMCAT
                    // ------------------------------------------------

                    echo.
                    echo ==========================================
                    echo STOPPING TOMCAT
                    echo ==========================================


                    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :%TOMCAT_PORT% ^| findstr LISTENING') do (

                        echo Killing PID %%a

                        taskkill /F /PID %%a >nul 2>&1
                    )


                    echo.
                    echo Waiting for Tomcat to stop...

                    ping 127.0.0.1 -n 4 >nul


                    // ------------------------------------------------
                    // REMOVE OLD APPLICATION
                    // ------------------------------------------------

                    echo.
                    echo ==========================================
                    echo REMOVING OLD QUIZAPP
                    echo ==========================================


                    rmdir /S /Q "%APPZ_HOME%\\webapps\\QuizApp" >nul 2>&1

                    del /F /Q "%APPZ_HOME%\\webapps\\QuizApp.war" >nul 2>&1


                    // ------------------------------------------------
                    // COPY NEW WAR
                    // ------------------------------------------------

                    echo.
                    echo ==========================================
                    echo COPYING QUIZAPP.WAR
                    echo ==========================================


                    copy /Y "%APPZ_ARTIFACTS%\\QuizApp.war" "%APPZ_HOME%\\webapps\\QuizApp.war"


                    if errorlevel 1 (

                        echo ERROR:
                        echo Failed to copy QuizApp.war.

                        exit /b 1
                    )


                    echo QuizApp.war copied successfully.


                    // ------------------------------------------------
                    // START TOMCAT
                    // ------------------------------------------------

                    echo.
                    echo ==========================================
                    echo STARTING TOMCAT
                    echo ==========================================


                    set "JAVA_HOME=C:\\Program Files\\Java\\jdk-17.0.2"
                    set "PATH=%JAVA_HOME%\\bin;%PATH%"
                    set "CATALINA_HOME=%APPZ_HOME%"
                    set "JENKINS_NODE_COOKIE=dontKillMe"


                    echo Starting Tomcat...

                    "%APPZ_HOME%\\bin\\catalina.bat" start


                    if errorlevel 1 (

                        echo.
                        echo ERROR:
                        echo Tomcat failed to start.

                        exit /b 1
                    )


                    echo.
                    echo TOMCAT START COMMAND EXECUTED


                    echo.
                    echo Waiting 15 seconds for Tomcat...

                    ping 127.0.0.1 -n 16 >nul


                    // ------------------------------------------------
                    // CHECK TOMCAT PORT
                    // ------------------------------------------------

                    echo.
                    echo ==========================================
                    echo CHECKING TOMCAT PORT
                    echo ==========================================


                    netstat -ano | findstr :%TOMCAT_PORT% | findstr LISTENING


                    if errorlevel 1 (

                        echo WARNING:
                        echo Port %TOMCAT_PORT% is not listening yet.

                    ) else (

                        echo Port %TOMCAT_PORT% is listening.

                    )


                    // ------------------------------------------------
                    // TOMCAT LOG
                    // ------------------------------------------------

                    echo.
                    echo ==========================================
                    echo TOMCAT LOG
                    echo ==========================================


                    if exist "%APPZ_HOME%\\logs\\catalina.out" (

                        powershell -Command "Get-Content '%APPZ_HOME%\\logs\\catalina.out' -Tail 30"

                    ) else if exist "%APPZ_HOME%\\logs\\jenkins-run.log" (

                        powershell -Command "Get-Content '%APPZ_HOME%\\logs\\jenkins-run.log' -Tail 30"

                    ) else (

                        echo No Tomcat log found.

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

                echo '=========================================='
                echo 'CHECKING APPZILLON'
                echo '=========================================='


                bat '''
                    @echo off


                    echo.
                    echo Appzillon URL:
                    echo %APPZILLON_URL%


                    echo.
                    echo Tomcat Port:
                    echo %TOMCAT_PORT%


                    set RETRIES=30


                    :CHECK_APPZILLON

                    echo.
                    echo Checking Appzillon...
                    echo Attempts remaining: %RETRIES%


                    curl -s -o nul -w "%%{http_code}" "%APPZILLON_URL%" | findstr "200 302 404"


                    if not errorlevel 1 (

                        echo.
                        echo ==========================================
                        echo APPZILLON IS RUNNING
                        echo ==========================================

                        echo URL:
                        echo %APPZILLON_URL%

                        exit /b 0
                    )


                    set /a RETRIES-=1


                    if %RETRIES% LEQ 0 (

                        echo.
                        echo ==========================================
                        echo APPZILLON FAILED TO START
                        echo ==========================================


                        echo.
                        echo ==========================================
                        echo TOMCAT PORT STATUS
                        echo ==========================================

                        netstat -ano | findstr :%TOMCAT_PORT%


                        echo.
                        echo ==========================================
                        echo TOMCAT LOG
                        echo ==========================================


                        if exist "%APPZ_HOME%\\logs\\catalina.out" (

                            powershell -Command "Get-Content '%APPZ_HOME%\\logs\\catalina.out' -Tail 30"

                        ) else if exist "%APPZ_HOME%\\logs\\jenkins-run.log" (

                            powershell -Command "Get-Content '%APPZ_HOME%\\logs\\jenkins-run.log' -Tail 30"

                        ) else (

                            echo No Tomcat log found.

                        )


                        exit /b 1
                    )


                    echo.
                    echo Waiting 5 seconds...

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
            echo 'http://localhost:8018/QuizFr/'

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

