@echo off
rem Runs the whole WatchesDen backend without Docker or PostgreSQL (Java 11+ only).
rem Data is kept in embedded H2 databases under %USERPROFILE%\.watchesden (override with AEON_DATA_DIR).
rem Each service opens in its own window; close the windows to stop them.
rem Usage: run-local.bat            build (if needed) and start everything
rem        run-local.bat --build    force a rebuild first

cd /d "%~dp0backend"

if "%1"=="--build" goto build
if not exist api-gateway\target\api-gateway-1.0.0.jar goto build
goto run

:build
echo Building backend (first run downloads dependencies, this can take a few minutes)...
call mvnw.cmd -B -q package -DskipTests || exit /b 1

:run
echo Starting discovery-server...
start "discovery-server" java -jar discovery-server\target\discovery-server-1.0.0.jar
echo Waiting 20 seconds for Eureka to start...
timeout /t 20 /nobreak > nul

start "user-service" java -jar user-service\target\user-service-1.0.0.jar --spring.profiles.active=local
start "product-service" java -jar product-service\target\product-service-1.0.0.jar --spring.profiles.active=local
start "order-service" java -jar order-service\target\order-service-1.0.0.jar --spring.profiles.active=local
start "api-gateway" java -jar api-gateway\target\api-gateway-1.0.0.jar

echo.
echo Services are starting in separate windows (give them about a minute).
echo   API gateway : http://localhost:8080
echo   Swagger UI  : http://localhost:8080/swagger-ui.html
echo   Eureka      : http://localhost:8761
echo Start the frontend in another terminal:  cd frontend ^&^& npm install ^&^& npm run dev
