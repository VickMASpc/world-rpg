@echo off
setlocal
cd /d "%~dp0"

echo [World RPG] Building...

if exist gradlew.bat (
    call gradlew.bat build --stacktrace
    exit /b %ERRORLEVEL%
)

where gradle >nul 2>nul
if errorlevel 1 (
    echo.
    echo ERROR: No Gradle wrapper is committed yet and "gradle" is not available on PATH.
    echo Install Gradle once, run:
    echo   gradle wrapper --gradle-version 8.10.2
    echo then commit gradlew, gradlew.bat, gradle/wrapper/gradle-wrapper.jar and gradle-wrapper.properties.
    exit /b 1
)

call gradle build --stacktrace
exit /b %ERRORLEVEL%
