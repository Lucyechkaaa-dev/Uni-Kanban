@echo off

REM Check if Node.js is installed
node -v >nul 2>&1
if errorlevel 1 (
    echo ERROR: Node.js is not installed or not in PATH
    echo Please install Node.js from https://nodejs.org/
    pause
    exit /b 1
)

echo Node.js found: 
node -v
echo.

REM Check if npm dependencies are installed
if not exist "node_modules" (
    echo npm dependencies not found. Installing...
    call npm install
    if errorlevel 1 (
        echo ERROR: npm install failed
        pause
        exit /b 1
    )
)

REM Start development server
echo Starting Vite development server on http://localhost:5173
echo Press Ctrl+C to stop the server
echo.

call npm run dev 

pause
