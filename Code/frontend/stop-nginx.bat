@echo off
cd /d %~dp0nginx-1.20.2
nginx.exe -c ../nginx.conf -s stop
echo Nginx stopped.
