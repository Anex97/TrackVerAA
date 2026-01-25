@echo off
set JAR=target\TrackVer-AA-1.0-SNAPSHOT-shaded.jar
if not exist "%JAR%" (
  echo JAR not found: %JAR%
  exit /b 1
)

echo Starting TrackVer server (detached)...

rem Start java in a new minimized cmd window and redirect output to logs
start "TrackVer" /min cmd /c "java -jar "%cd%\%JAR%" > "%cd%\server.log" 2> "%cd%\server.err""

rem Give the OS a moment for the process to start
timeout /t 1 /nobreak >nul

rem Try to obtain the PID of the started process using WMIC (first match)
for /f "tokens=2 delims==" %%A in ('wmic process where "CommandLine like '%%TrackVer-AA-1.0-SNAPSHOT-shaded.jar%%'" get ProcessId /value 2^>nul') do (
  if not defined _PID set _PID=%%A
)
if defined _PID (
  echo %_PID%>"%cd%\server.pid"
  echo Server started. PID %_PID% saved to server.pid
  echo Servidor funcionando (PID %_PID%)
) else (
  echo Server started. PID not detected automatically; stop script will find processes by jar name.
  echo Servidor funcionando
)
