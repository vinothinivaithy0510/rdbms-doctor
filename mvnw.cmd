@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script for RDBMS Doctor
@REM ----------------------------------------------------------------------------

@if "%DEBUG%" == "" @echo off
@setlocal

set MAVEN_CMD=C:\Users\vinothini\maven\apache-maven-3.9.9\bin\mvn.cmd
if exist "%MAVEN_CMD%" (
    "%MAVEN_CMD%" %*
) else (
    mvn %*
)
