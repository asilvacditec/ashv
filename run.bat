@echo off
setlocal
cd /d "%~dp0"
set "JAVA=java"
if defined JAVA_HOME set "JAVA=%JAVA_HOME%\bin\java.exe"
if not exist "target\ash-viewer.jar" (
  echo Build first: mvn clean verify
  exit /b 1
)
"%JAVA%" -Xmx768m -jar "target\ash-viewer.jar" %*
exit /b %ERRORLEVEL%
