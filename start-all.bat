@echo off
set JAVA_HOME=C:\jdk17\jdk-17.0.12
set PATH=%JAVA_HOME%\bin;C:\maven\apache-maven-3.9.6\bin;%PATH%

echo Starting Auth Service...
start "Auth Service" cmd /k "cd auth-service && mvn spring-boot:run"

echo Starting Payee Service...
start "Payee Service" cmd /k "cd payee-service && mvn spring-boot:run"

echo Starting Bank Scoring Service...
start "Bank Scoring Service" cmd /k "cd bank-scoring-service && mvn spring-boot:run"

echo Starting API Gateway...
start "API Gateway" cmd /k "cd api-gateway && mvn spring-boot:run"

set PATH=C:\nodejs\node-v20.18.0-win-x64;%PATH%
echo Starting Frontend...
start "Frontend" cmd /k "cd frontend && npx ng serve --open"

echo All services are starting up in separate windows!
