@echo off
rem Starts the full Kanban stack: backend (8081), frontend (127.0.0.1:5173), Caddy HTTPS (2345).
rem Each process runs in its own window so you can see its logs.

set PROJECT=D:\project\Uni-Kanban
set CADDY=%USERPROFILE%\caddy\caddy.exe
set MVN=C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.16\bin\mvn.cmd

rem --- Credentials (DB_URL, DB_USER, DB_PASS, DB_SCHEMA) ---
rem Kept in deploy\secrets.cmd, which is git-ignored. Never commit that file.
if not exist "%PROJECT%\deploy\secrets.cmd" (
  echo Missing %PROJECT%\deploy\secrets.cmd - copy deploy\secrets.cmd.example and fill it in.
  pause
  exit /b 1
)
call "%PROJECT%\deploy\secrets.cmd"

rem --- Backend ---
rem The console menu reads stdin; the ping pipe keeps stdin open so it doesn't exit.
start "kanban-backend" cmd /k "cd /d %PROJECT% && (ping -n 100000 127.0.0.1 >nul) | "%MVN%" spring-boot:run"

rem --- Frontend (Vite, localhost only) ---
start "kanban-frontend" cmd /k "cd /d %PROJECT%\kanban-frontend && npm run dev"

rem --- Caddy (public HTTPS on 2345, needs router forwards 80 and 2345) ---
start "kanban-caddy" cmd /k ""%CADDY%" run --config "%PROJECT%\deploy\Caddyfile" --adapter caddyfile"

echo Started backend, frontend and Caddy in separate windows.
echo Public URL: https://lucyechkaaa.myddns.me:2345
