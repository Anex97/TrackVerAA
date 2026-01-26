@echo off
setlocal
if exist server.pid (
  set /p PID=<server.pid
  if defined PID (
    echo Stopping PID %PID% ...
    taskkill /PID %PID% /F >nul 2>&1
    del /q server.pid
    echo Servidor detenido
    endlocal
    exit /b 0
  )
)
echo No server.pid found or empty — attempting to find process by jar name...

rem First try: WMIC (available on many Windows versions)
set _FOUND=0
for /f "usebackq tokens=*" %%I in (`wmic process where "CommandLine like '%%TrackVer-AA-1.0-SNAPSHOT-shaded.jar%%'" get ProcessId 2^>nul`) do (
  rem skip empty lines and header
  for /f "tokens=*" %%P in ("%%I") do (
    set "line=%%P"
    if not "!line!"=="" (
      rem if line is numeric, treat as PID
      for /f "tokens=* delims= " %%X in ("!line!") do (
        set "maybe=%%X"
        rem check if digits
        echo !maybe! | findstr /r "^[0-9][0-9]*$" >nul 2>&1 && (
          set "_KPID=!maybe!"
          echo Killing !_KPID! ...
          taskkill /PID !_KPID! /F >nul 2>&1 || echo Failed killing !_KPID!
          set _FOUND=1
        )
      )
    )
  )
)

if "%_FOUND%"=="1" (
  if exist server.pid del /q server.pid
  echo Servidor detenido
  endlocal
  exit /b 0
)

rem Last resort: use PowerShell to find and stop processes (works even if tasklist/wmic missing)
echo WMIC did not find processes; trying PowerShell fallback...
powershell -NoProfile -Command "try { Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -and $_.CommandLine -match 'TrackVer-AA-1.0-SNAPSHOT-shaded.jar' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force; Write-Output ('Stopped ' + $_.ProcessId) } } catch { Write-Output 'PowerShell fallback failed: ' + $_.Exception.Message; exit 1 }"
if exist server.pid del /q server.pid
echo If processes were running, they should now be stopped.
endlocal
