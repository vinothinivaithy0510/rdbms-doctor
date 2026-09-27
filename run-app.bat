@echo off
echo ========================================================
echo  Starting RDBMS Doctor Application...
echo  Tagline: "Write SQL. Find the Problem. Understand the Solution."
echo ========================================================

cd /d "%~dp0"

java -jar target\rdbms-doctor-1.0.0.jar

pause
