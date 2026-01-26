@echo off
@echo off
echo THIS FILE IS DEPRECATED.
echo Use start-server.bat in the repository root instead.
echo Command to run from repository root:
echo    start-server.bat
pause
endlocal)  echo Server started. PID not detected automatically; stop script will find processes by jar name.) else (  echo Server started. PID %_PID% saved to server.pid  echo %_PID%>"%cd%\server.pid"if defined _PID ()  if not defined _PID set _PID=%%Afor /f "tokens=2 delims==" %%A in ('wmic process where "CommandLine like '%%TrackVer-AA-1.0-SNAPSHOT-shaded.jar%%'" get ProcessId /value 2^>nul') do (
nrem Try to obtain the PID of the started process using WMIC (first match)start "TrackVer" /min cmd /c "java -jar "%cd%\%JAR%" > "%cd%\server.log" 2> "%cd%\server.err""
nrem Start java in a new minimized cmd window and redirect output to logsnecho Starting TrackVer server (detached)...