@echo off
rem One entry point for the whole stack. All settings come from the project-root .env file.
rem
rem   start-all.cmd            start backend, frontend and Caddy (each in its own window)
rem   start-all.cmd backend    only the backend   (Spring Boot, port BACKEND_PORT)
rem   start-all.cmd frontend   only the frontend  (Vite, port FRONTEND_PORT)
rem   start-all.cmd proxy      only Caddy         (public HTTPS on PUBLIC_PORT)
rem   start-all.cmd check      show what was loaded (password hidden), start nothing

set "ROOT=%~dp0.."
for %%I in ("%ROOT%") do set "ROOT=%%~fI"
set "ENVFILE=%ROOT%\.env"

if not exist "%ENVFILE%" (
  echo Missing %ENVFILE%
  echo Copy .env.example to .env and fill in your values.
  pause
  exit /b 1
)

rem Load KEY=value lines into the environment; lines starting with # are skipped
for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%ENVFILE%") do set "%%A=%%B"

set "MODE=%~1"
if "%MODE%"=="" set "MODE=all"

if /i "%MODE%"=="check" goto check
if /i "%MODE%"=="all" goto all
if /i "%MODE%"=="backend" goto backend_only
if /i "%MODE%"=="frontend" goto frontend_only
if /i "%MODE%"=="proxy" goto proxy_only
echo Unknown mode "%MODE%". Use: all, backend, frontend, proxy or check.
exit /b 1

:all
call :start_backend
call :start_frontend
call :start_proxy
echo Started backend, frontend and Caddy in separate windows.
echo Public URL: https://%PUBLIC_HOST%:%PUBLIC_PORT%
exit /b 0

:backend_only
call :start_backend
exit /b 0

:frontend_only
call :start_frontend
exit /b 0

:proxy_only
call :start_proxy
exit /b 0

:check
echo Loaded from %ENVFILE%
echo   DB_URL=%DB_URL%
echo   DB_USER=%DB_USER%
echo   DB_PASS=****
echo   DB_SCHEMA=%DB_SCHEMA%
echo   BACKEND_PORT=%BACKEND_PORT%   APP_COOKIE_SECURE=%APP_COOKIE_SECURE%
echo   FRONTEND_PORT=%FRONTEND_PORT%
echo   PUBLIC=%PUBLIC_HOST%:%PUBLIC_PORT%
echo   MAVEN_HOME=%MAVEN_HOME%
echo   CADDY_EXE=%CADDY_EXE%
exit /b 0

rem ---- the three parts ----

:start_backend
rem The console menu reads stdin; the ping pipe keeps stdin open so the backend does not exit at once.
start "kanban-backend" cmd /k "cd /d "%ROOT%" && (ping -n 100000 127.0.0.1 >nul) | "%MAVEN_HOME%\bin\mvn.cmd" spring-boot:run"
goto :eof

:start_frontend
start "kanban-frontend" cmd /k "cd /d "%ROOT%\kanban-frontend" && npm run dev"
goto :eof

:start_proxy
rem Needs router forwards: external 80 -> this PC 80 (certificate check) and PUBLIC_PORT -> this PC PUBLIC_PORT
start "kanban-caddy" cmd /k ""%CADDY_EXE%" run --config "%ROOT%\deploy\Caddyfile" --adapter caddyfile"
goto :eof
