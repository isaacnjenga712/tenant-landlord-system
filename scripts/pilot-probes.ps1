# Pilot cross-tenant probe scripts — run on Day 3.

$ErrorActionPreference = "Stop"

$login = Invoke-RestMethod -Method Post `
    -Uri "http://localhost:8080/api/v1/auth/login" `
    -ContentType "application/json" `
    -Body (@{email="org1test@example.com"; password="TestPass123!"} | ConvertTo-Json)

$token = $login.accessToken

Write-Host "=== Probe 1: Cross-tenant GET ===" -ForegroundColor Cyan
$probe1 = curl.exe -s -i "http://localhost:8081/api/v1/notifications/inbox?page=0&size=5" `
    -H "Authorization: Bearer $token" `
    -H "X-Org-Id: 00000000-0000-0000-0000-000000000002" `
    -H "X-User-PublicId: 739ed6b8-5bb5-476b-921b-78b166a67b73"
Write-Host $probe1

Write-Host "`n=== Probe 2: Cross-tenant WRITE ===" -ForegroundColor Cyan
$inbox = Invoke-RestMethod -Method Get `
    -Uri "http://localhost:8081/api/v1/notifications/inbox?page=0&size=1" `
    -Headers @{
        "Authorization"   = "Bearer $token"
        "X-Org-Id"        = "00000000-0000-0000-0000-000000000001"
        "X-User-PublicId" = "739ed6b8-5bb5-476b-921b-78b166a67b73"
    }

if ($inbox.items.Count -gt 0) {
    $id = $inbox.items[0].id
    $probe2 = curl.exe -s -i -X PATCH "http://localhost:8081/api/v1/notifications/inbox/$id/read" `
        -H "Authorization: Bearer $token" `
        -H "X-Org-Id: 00000000-0000-0000-0000-000000000002" `
        -H "X-User-PublicId: 739ed6b8-5bb5-476b-921b-78b166a67b73"
    Write-Host $probe2
} else {
    Write-Host "No notifications to probe" -ForegroundColor Yellow
}