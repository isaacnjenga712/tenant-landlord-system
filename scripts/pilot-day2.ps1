# Day 2 — Active usage: run the workflow 5 times with both users.

$ErrorActionPreference = "Stop"

$orgId = "00000000-0000-0000-0000-000000000001"
$password = "TestPass123!"

$users = @(
    @{ email = "org1test@example.com"; publicId = "739ed6b8-5bb5-476b-921b-78b166a67b73" },
    @{ email = "pilot2@example.com";   publicId = "76db1f70-85fc-4a38-88cf-9db78e3c2dbf" }
)

for ($i = 1; $i -le 5; $i++) {
    $user = $users[($i - 1) % 2]
    Write-Host "Iteration $i - $($user.email)" -ForegroundColor Cyan

    # 1. Login
    $loginBody = @{ email = $user.email; password = $password } | ConvertTo-Json
    $login = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/v1/auth/login' -ContentType 'application/json' -Body $loginBody
    $token = $login.accessToken
    Write-Host "  Login OK" -ForegroundColor Green

    # 2. Fetch inbox using curl.exe (handles & correctly)
    $inboxJson = curl.exe -s 'http://localhost:8081/api/v1/notifications/inbox?page=0&size=5' `
        -H "Authorization: Bearer $token" `
        -H "X-Org-Id: $orgId" `
        -H "X-User-PublicId: $($user.publicId)"
    $inbox = $inboxJson | ConvertFrom-Json
    Write-Host "  Inbox: $($inbox.total) total" -ForegroundColor Green

    # 3. Mark first item as read (PATCH has no query params — safe)
    if ($inbox.items.Count -gt 0) {
        $id = $inbox.items[0].id
        $readUrl = "http://localhost:8081/api/v1/notifications/inbox/$id/read"
        Invoke-RestMethod -Method Patch -Uri $readUrl -Headers @{
            "Authorization"   = "Bearer $token"
            "X-Org-Id"        = $orgId
            "X-User-PublicId" = $user.publicId
        } | Out-Null
        Write-Host "  Marked read: $id" -ForegroundColor Green
    }

    Start-Sleep -Seconds 1
}

Write-Host '=== Workflow complete: 5 iterations ===' -ForegroundColor Green