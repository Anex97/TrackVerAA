# Run basic API tests against the local TrackVer server
# Usage: powershell -ExecutionPolicy Bypass -File .\scripts\run-api-tests.ps1

$base = 'http://localhost:4567'
Write-Host "Base URL: $base"

# Credentials from SeedDB
$testEmail = 'usuario@trackver.com'
$testPass = '1234'
 
# Inicializar contador de errores y lista de pasos fallidos
$errors = 0
$failed = @()

try {
    Write-Host "1) Login as $testEmail"
    $login = Invoke-RestMethod -Uri "$base/api/login" -Method Post -Body @{ correo=$testEmail; contrasena=$testPass } -ContentType 'application/x-www-form-urlencoded' -ErrorAction Stop
    Write-Host "  -> Login response: " ($login | ConvertTo-Json -Compress)
    if (-not $login.id) { throw "Login returned no id" }
    $uid = $login.id
} catch {
    Write-Error "Login failed: $_"
    $errors = $errors + 1
    $failed += 'Login'
    Write-Host "\n--- Summary ---"
    Write-Host "Success: $([bool]($errors -eq 0))"
    Write-Host "Errors: $errors"
    if ($failed.Count -gt 0) { Write-Host "Failed steps: $($failed -join ', ')" }
    exit 1
}

try {
    Write-Host "2) List vehicles for user $uid"
    $vehList = Invoke-RestMethod -Uri "$base/api/vehiculos?usuarioId=$uid" -Method Get -ErrorAction Stop
    Write-Host "  -> Vehicles: " ($vehList | ConvertTo-Json -Compress)
} catch {
    Write-Error "List vehicles failed: $_"
}

try {
    Write-Host "3) Create a test vehicle"
    $placas = ('TS' + (Get-Random -Maximum 99999))
    $create = Invoke-RestMethod -Uri "$base/api/vehiculos" -Method Post -Body @{ marca='PS_Test'; modelo='ModelX'; placas=$placas; anio='2020'; usuarioId=$uid } -ContentType 'application/x-www-form-urlencoded' -ErrorAction Stop
    Write-Host "  -> Create response: " ($create | ConvertTo-Json -Compress)
    $vehId = $create.id
    if (-not $vehId) { throw "Create vehicle failed (no id)" }
} catch {
    Write-Error "Create vehicle failed: $_"
    $errors = $errors + 1
    $failed += 'CreateVehicle'
    Write-Host "\n--- Summary ---"
    Write-Host "Success: $([bool]($errors -eq 0))"
    Write-Host "Errors: $errors"
    if ($failed.Count -gt 0) { Write-Host "Failed steps: $($failed -join ', ')" }
    exit 1
}

try {
    Write-Host "4) Confirm vehicle created (list)"
    $vehList2 = Invoke-RestMethod -Uri "$base/api/vehiculos?usuarioId=$uid" -Method Get -ErrorAction Stop
    Write-Host "  -> Vehicles now: " ($vehList2 | ConvertTo-Json -Compress)
} catch {
    Write-Error "List after create failed: $_"
}

try {
    Write-Host "5) Register a position for the new vehicle"
    $pos = Invoke-RestMethod -Uri "$base/api/posiciones" -Method Post -Body @{ lat='19.4326'; lon='-99.1332'; usuarioId=$uid; vehiculoId=$vehId; descripcion='Prueba desde script' } -ContentType 'application/x-www-form-urlencoded' -ErrorAction Stop
    Write-Host "  -> Register position response: " ($pos | ConvertTo-Json -Compress)
} catch {
    Write-Error "Register position failed: $_"
    $errors = $errors + 1
    $failed += 'RegisterPosition'
}

try {
    Write-Host "6) List positions for user"
    $posList = Invoke-RestMethod -Uri "$base/api/posiciones?usuarioId=$uid" -Method Get -ErrorAction Stop
    Write-Host "  -> Positions: " ($posList | ConvertTo-Json -Compress)
} catch {
    Write-Error "List positions failed: $_"
}

try {
    Write-Host "7) Delete the test vehicle (requires password)"
    $del = Invoke-RestMethod -Uri "$base/api/vehiculos/delete" -Method Post -Body @{ id=$vehId; usuarioId=$uid; password=$testPass } -ContentType 'application/x-www-form-urlencoded' -ErrorAction Stop
    Write-Host "  -> Delete response: " ($del | ConvertTo-Json -Compress)
} catch {
    Write-Error "Delete vehicle failed: $_"
    $errors = $errors + 1
    $failed += 'DeleteVehicle'
}

try {
    Write-Host "8) Confirm deletion (list)"
    $vehList3 = Invoke-RestMethod -Uri "$base/api/vehiculos?usuarioId=$uid" -Method Get -ErrorAction Stop
    Write-Host "  -> Vehicles after delete: " ($vehList3 | ConvertTo-Json -Compress)
} catch {
    Write-Error "List after delete failed: $_"
}

Write-Host "\n--- Summary ---"
Write-Host "Success: $([bool]($errors -eq 0))"
Write-Host "Errors: $errors"
if ($failed.Count -gt 0) { Write-Host "Failed steps: $($failed -join ', ')" }
Write-Host "All tests completed. If any step errored, check the messages above."