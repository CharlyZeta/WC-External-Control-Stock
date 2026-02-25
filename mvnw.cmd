@REM Licensed to the Apache Software Foundation (ASF)
@REM Maven Wrapper startup batch script for Windows
@REM Usage: mvnw.cmd [goal...]

@echo off
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
set "PROPERTIES_FILE=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties"

@REM Read distribution and wrapper URLs from properties file
set "MVNW_DIST_URL="
set "MVNW_WRAPPER_URL="
if exist "%PROPERTIES_FILE%" (
    for /f "usebackq eol=# tokens=1,* delims==" %%b in ("%PROPERTIES_FILE%") do (
        if "%%b"=="distributionUrl" set "MVNW_DIST_URL=%%c"
        if "%%b"=="wrapperUrl" set "MVNW_WRAPPER_URL=%%c"
    )
)

@REM Default URLs if not read from properties
if "%MVNW_DIST_URL%"=="" set "MVNW_DIST_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip"

@REM Find Java 21 (required for this project)
@REM Override JAVA_HOME if it doesn't point to Java 21
set "JAVACMD="

@REM Check common Adoptium Java 21 locations
for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk-21*") do (
    if exist "%%d\bin\java.exe" (
        set "JAVA_HOME=%%d"
        set "JAVACMD=%%d\bin\java.exe"
    )
)

@REM Fallback to JAVA_HOME if set
if "%JAVACMD%"=="" (
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\java.exe" (
            set "JAVACMD=%JAVA_HOME%\bin\java.exe"
        )
    )
)

@REM Fallback to PATH
if "%JAVACMD%"=="" (
    for %%i in (java.exe) do set "JAVACMD=%%~$PATH:i"
)

if "%JAVACMD%"=="" (
    echo ERROR: Java not found. Install Java 21 or set JAVA_HOME.
    exit /b 1
)

@REM Check java version
echo Using Java: %JAVACMD%

@REM Try using wrapper jar first
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
if not "%MVNW_WRAPPER_URL%"=="" (
    if not exist "%WRAPPER_JAR%" (
        echo Downloading Maven Wrapper JAR...
        powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%MVNW_WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'}" >nul 2>&1
    )
)

if exist "%WRAPPER_JAR%" (
    "%JAVACMD%" %MAVEN_OPTS% -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
    if not ERRORLEVEL 1 goto end
    echo Maven Wrapper JAR failed, falling back to direct download...
)

@REM Direct Maven download fallback
set "MAVEN_HOME=%MAVEN_PROJECTBASEDIR%.mvn\maven"
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo Downloading Maven 3.9.9 directly...
    set "MAVEN_ZIP=%MAVEN_PROJECTBASEDIR%.mvn\maven.zip"
    powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%MVNW_DIST_URL%' -OutFile '%MAVEN_PROJECTBASEDIR%.mvn\maven.zip'}"
    if ERRORLEVEL 1 (
        echo ERROR: Failed to download Maven. Check your internet connection.
        exit /b 1
    )
    powershell -Command "& {Expand-Archive -Path '%MAVEN_PROJECTBASEDIR%.mvn\maven.zip' -DestinationPath '%MAVEN_PROJECTBASEDIR%.mvn' -Force}"
    if ERRORLEVEL 1 (
        echo ERROR: Failed to extract Maven archive.
        exit /b 1
    )
    @REM Rename the extracted directory
    for /d %%d in ("%MAVEN_PROJECTBASEDIR%.mvn\apache-maven-*") do (
        if exist "%MAVEN_HOME%" rmdir /s /q "%MAVEN_HOME%"
        rename "%%d" maven
    )
    del "%MAVEN_PROJECTBASEDIR%.mvn\maven.zip" 2>nul
)

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo ERROR: Maven installation not found at %MAVEN_HOME%
    exit /b 1
)

echo Using Maven at: %MAVEN_HOME%
"%MAVEN_HOME%\bin\mvn.cmd" %*

:end
endlocal
