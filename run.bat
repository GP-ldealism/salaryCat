@echo off
chcp 65001 >nul 2>&1

mkdir bin 2>nul

javac -d bin -encoding UTF-8 src/*.java
if %errorlevel% neq 0 (
    echo Compilation failed.
    pause
    exit /b 1
)

java -cp bin SalaryCatApp
