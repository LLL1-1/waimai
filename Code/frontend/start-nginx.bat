@echo off
cd /d %~dp0nginx-1.20.2
start "" nginx.exe -c ../nginx.conf
echo Nginx started: http://localhost:8082
