@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo   Trexo Test Automation Windows Execution Runner
echo ===================================================

:: Set defaults
set SUITE_XML_FILE=testng.xml
set BROWSER=chrome
set TEST_ENV=qa
set HEADLESS=true

:: Process command line arguments (key=value format)
for %%a in (%*) do (
    set "arg=%%a"
    for /f "tokens=1,2 delims==" %%i in ("!arg!") do (
        set "key=%%i"
        set "val=%%j"
        if /i "!key!"=="suite" set "SUITE_XML_FILE=!val!"
        if /i "!key!"=="browser" set "BROWSER=!val!"
        if /i "!key!"=="env" set "TEST_ENV=!val!"
        if /i "!key!"=="headless" set "HEADLESS=!val!"
    )
)

echo Configuration:
echo   - TestNG Suite XML: %SUITE_XML_FILE%
echo   - Browser:           %BROWSER%
echo   - Environment:       %TEST_ENV%
echo   - Headless:          %HEADLESS%
echo.

:: Check for global maven
where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    echo Global 'mvn' command found in PATH.
    set MAVEN_CMD=mvn
) else (
    echo Global 'mvn' not found. Checking for bundled Maven distribution...
    set BUNDLED_MVN="%~dp0apache-maven-3.9.12-bin\apache-maven-3.9.12\bin\mvn.cmd"
    if exist !BUNDLED_MVN! (
        echo Bundled Maven found at: !BUNDLED_MVN!
        set MAVEN_CMD=!BUNDLED_MVN!
    ) else (
        echo ERROR: Maven was not found globally or in the bundled directory.
        echo Please install Maven or ensure it is in your PATH.
        exit /b 1
    )
)

echo Running Maven command:
echo %MAVEN_CMD% clean test -DsuiteXmlFile=%SUITE_XML_FILE% -Dbrowser=%BROWSER% -Denv=%TEST_ENV% -Dheadless=%HEADLESS%
echo.

call %MAVEN_CMD% clean test -DsuiteXmlFile=%SUITE_XML_FILE% -Dbrowser=%BROWSER% -Denv=%TEST_ENV% -Dheadless=%HEADLESS%

if %ERRORLEVEL% equ 0 (
    echo.
    echo Execution successful!
    exit /b 0
) else (
    echo.
    echo Execution failed with exit code %ERRORLEVEL%!
    exit /b %ERRORLEVEL%
)
