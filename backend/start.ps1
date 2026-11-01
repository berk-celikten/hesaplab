param(
    [string]$JavaHome = $env:JAVA_HOME,
    [int]$Port = 8081
)
$ErrorActionPreference = 'Stop'
if (-not $JavaHome) {
    $JavaHome = 'C:/Program Files/Android/openjdk/jdk-21.0.8'
}
if (-not (Test-Path (Join-Path $JavaHome 'bin/javac.exe'))) {
    throw 'JDK 21 yolunu -JavaHome ile belirtin.'
}
if (Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue) {
    throw "Port $Port kullanımda. Başka süreci kapatmadan -Port ile boş port seçin."
}
# Yalnız bu PowerShell sürecinin ayarları; kalıcı ortam değişikliği yapılmaz.
$previousJavaHome = $env:JAVA_HOME
$previousPort = $env:HESAPLAB_PORT
Push-Location $PSScriptRoot
try {
    $env:JAVA_HOME = $JavaHome
    $env:HESAPLAB_PORT = "$Port"
    & .\mvnw.cmd spring-boot:run
    if ($LASTEXITCODE -ne 0) { throw 'Backend başlatılamadı.' }
} finally {
    $env:JAVA_HOME = $previousJavaHome
    $env:HESAPLAB_PORT = $previousPort
    Pop-Location
}
