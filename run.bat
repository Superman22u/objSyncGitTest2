@echo off
echo ========================================
echo   Payment Tracker (Flat File Edition)
echo ========================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo Maven is not installed or not in PATH.
    echo Please install Maven from https://maven.apache.org/
    pause
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo Java is not installed or not in PATH.
    echo Please install Java JDK 11 or higher.
    pause
    exit /b 1
)

echo Building and running Payment Tracker...
echo.

REM Run with Maven
mvn clean compile exec:java

if %errorlevel% neq 0 (
    echo.
    echo Build/Run failed!
    echo.
    echo Troubleshooting:
    echo 1. Check Java installation: java -version
    echo 2. Check Maven installation: mvn -version
    echo 3. Make sure you have internet connection for dependencies
    echo 4. Check pom.xml configuration
    pause
)