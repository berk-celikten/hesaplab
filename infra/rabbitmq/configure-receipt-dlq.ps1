# Proje kökünden açıkça çalıştırılır; mevcut kuyruğu silmez/değişmez argüman eklemez.
$ErrorActionPreference = 'Stop'
$settings = @{}
Get-Content (Join-Path $PSScriptRoot '.env.local') | ForEach-Object {
    if ($_ -match '^([^=]+)=(.*)$') { $settings[$matches[1]] = $matches[2] }
}
$auth = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes(
    $settings.RABBITMQ_USER + ':' + $settings.RABBITMQ_PASSWORD))
$headers = @{ Authorization = 'Basic ' + $auth }
$base = 'http://127.0.0.1:15673/api'
function Put($path, $value) {
    $null = Invoke-RestMethod -Uri "$base/$path" -Method Put -Headers $headers -ContentType 'application/json' -Body ($value | ConvertTo-Json -Depth 6 -Compress)
}
# Yalnız yeni HesapLab DLX/DLQ ve binding; mevcut diğer kaynaklara dokunulmaz.
Put 'exchanges/hesaplab/hesaplab.receipt.dlx' @{type='direct';durable=$true;auto_delete=$false;arguments=@{}}
Put 'queues/hesaplab/hesaplab.receipt.dlq' @{durable=$true;auto_delete=$false;arguments=@{}}
$null = Invoke-RestMethod -Uri "$base/bindings/hesaplab/e/hesaplab.receipt.dlx/q/hesaplab.receipt.dlq" -Method Post -Headers $headers -ContentType 'application/json' -Body '{"routing_key":"hesaplab.receipt.dead","arguments":{}}'
Put 'policies/hesaplab/hesaplab-receipt-dlx' @{
    pattern='^hesaplab\.receipt\.queue$'
    'apply-to'='queues'
    priority=50
    definition=@{'dead-letter-exchange'='hesaplab.receipt.dlx';'dead-letter-routing-key'='hesaplab.receipt.dead'}
}
Write-Output 'HesapLab receipt DLX/DLQ ve tam-ad policy hazır.'
