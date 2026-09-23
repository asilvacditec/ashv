#Requires -Version 5.1
<#
.SYNOPSIS
    Prepara ambiente Java/Maven no Windows 11.

.DESCRIPTION
    - Instala Eclipse Temurin JDK 17
    - Instala Eclipse Temurin JDK 21
    - Configura Java 17 como JAVA_HOME padrao
    - Cria JAVA17_HOME e JAVA21_HOME
    - Instala Apache Maven 3.9.x
    - Configura MAVEN_HOME
    - Configura PATH
    - Testa java/javaC
    - Testa Maven
    - Testa HTTPS para Maven Central
    - Testa resolucao de dependencia via Maven
    - Pode ser executado novamente sem reinstalar desnecessariamente

.NOTES
    Executar PowerShell como Administrador.
#>

$ErrorActionPreference = "Stop"

# ============================================================
# CONFIGURACAO
# ============================================================

$MavenVersion = "3.9.16"

$Jdk17Package = "EclipseAdoptium.Temurin.17.JDK"
$Jdk21Package = "EclipseAdoptium.Temurin.21.JDK"

$MavenInstallRoot = "C:\Tools"
$MavenHome        = "$MavenInstallRoot\apache-maven-$MavenVersion"

$MavenUrl = "https://dlcdn.apache.org/maven/maven-3/$MavenVersion/binaries/apache-maven-$MavenVersion-bin.zip"

$MavenCentralUrl = "https://repo.maven.apache.org/maven2/"

# ============================================================
# FUNCOES
# ============================================================

function Write-Step {
    param([string]$Message)

    Write-Host ""
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host $Message -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor Cyan
}

function Write-OK {
    param([string]$Message)
    Write-Host "[OK] $Message" -ForegroundColor Green
}

function Write-Warn {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Write-Fail {
    param([string]$Message)
    Write-Host "[ERRO] $Message" -ForegroundColor Red
}

function Test-Administrator {

    $identity = [Security.Principal.WindowsIdentity]::GetCurrent()

    $principal = New-Object Security.Principal.WindowsPrincipal($identity)

    return $principal.IsInRole(
        [Security.Principal.WindowsBuiltInRole]::Administrator
    )
}

function Test-WingetPackage {

    param(
        [Parameter(Mandatory=$true)]
        [string]$PackageId
    )

    $result = winget list `
        --id $PackageId `
        --exact `
        --accept-source-agreements 2>$null |
        Out-String

    return ($result -match [regex]::Escape($PackageId))
}

function Install-WingetPackage {

    param(
        [Parameter(Mandatory=$true)]
        [string]$PackageId
    )

    if (Test-WingetPackage $PackageId) {

        Write-OK "$PackageId ja esta instalado."

    }
    else {

        Write-Host "Instalando $PackageId..." -ForegroundColor Yellow

        winget install `
            --id $PackageId `
            --exact `
            --silent `
            --accept-package-agreements `
            --accept-source-agreements

        if ($LASTEXITCODE -ne 0) {
            throw "Falha ao instalar $PackageId. ExitCode: $LASTEXITCODE"
        }

        Write-OK "$PackageId instalado."
    }
}

function Get-JdkPath {

    param(
        [Parameter(Mandatory=$true)]
        [int]$MajorVersion
    )

    $BasePath = "C:\Program Files\Eclipse Adoptium"

    if (-not (Test-Path $BasePath)) {
        throw "Diretorio Eclipse Adoptium nao encontrado: $BasePath"
    }

    $jdk = Get-ChildItem $BasePath -Directory |
        Where-Object {
            $_.Name -match "^jdk-$MajorVersion(\.|-)"
        } |
        Sort-Object Name -Descending |
        Select-Object -First 1

    if (-not $jdk) {
        throw "JDK $MajorVersion nao encontrado em $BasePath"
    }

    return $jdk.FullName
}

function Add-SystemPath {

    param(
        [Parameter(Mandatory=$true)]
        [string]$PathEntry
    )

    $machinePath =
        [Environment]::GetEnvironmentVariable(
            "Path",
            "Machine"
        )

    $entries = $machinePath -split ";"

    if ($entries -contains $PathEntry) {

        Write-OK "PATH ja contem: $PathEntry"
        return
    }

    $newPath = $machinePath.TrimEnd(";") + ";" + $PathEntry

    [Environment]::SetEnvironmentVariable(
        "Path",
        $newPath,
        "Machine"
    )

    Write-OK "Adicionado ao PATH: $PathEntry"
}

function Refresh-CurrentPath {

    $machinePath =
        [Environment]::GetEnvironmentVariable(
            "Path",
            "Machine"
        )

    $userPath =
        [Environment]::GetEnvironmentVariable(
            "Path",
            "User"
        )

    $env:Path = "$machinePath;$userPath"
}

# ============================================================
# CABECALHO
# ============================================================

Clear-Host

Write-Host ""
Write-Host "Windows Java/Maven Environment Setup" -ForegroundColor Cyan
Write-Host "------------------------------------"
Write-Host "JDK 17 + JDK 21 + Maven $MavenVersion"
Write-Host ""

# ============================================================
# 1. ADMINISTRADOR
# ============================================================

Write-Step "1. Verificando privilegios administrativos"

if (-not (Test-Administrator)) {

    Write-Fail "Este script precisa ser executado como Administrador."

    Write-Host ""
    Write-Host "Abra PowerShell como Administrador e execute novamente."

    exit 1
}

Write-OK "PowerShell executado como Administrador."

# ============================================================
# 2. WINGET
# ============================================================

Write-Step "2. Verificando winget"

if (-not (Get-Command winget -ErrorAction SilentlyContinue)) {

    throw @"
winget nao encontrado.

Verifique se o 'App Installer' da Microsoft esta instalado.
"@
}

$WingetVersion = winget --version

Write-OK "winget encontrado: $WingetVersion"

# ============================================================
# 3. JDK 17
# ============================================================

Write-Step "3. Instalando/verificando Eclipse Temurin JDK 17"

Install-WingetPackage $Jdk17Package

# ============================================================
# 4. JDK 21
# ============================================================

Write-Step "4. Instalando/verificando Eclipse Temurin JDK 21"

Install-WingetPackage $Jdk21Package

# ============================================================
# 5. LOCALIZAR JDKs
# ============================================================

Write-Step "5. Localizando instalacoes Java"

$Java17Home = Get-JdkPath 17
$Java21Home = Get-JdkPath 21

Write-Host "JDK 17: $Java17Home"
Write-Host "JDK 21: $Java21Home"

# ============================================================
# 6. VARIAVEIS JAVA
# ============================================================

Write-Step "6. Configurando variaveis Java"

[Environment]::SetEnvironmentVariable(
    "JAVA17_HOME",
    $Java17Home,
    "Machine"
)

[Environment]::SetEnvironmentVariable(
    "JAVA21_HOME",
    $Java21Home,
    "Machine"
)

#
# Java 17 sera o Java default
#

[Environment]::SetEnvironmentVariable(
    "JAVA_HOME",
    $Java17Home,
    "Machine"
)

$env:JAVA17_HOME = $Java17Home
$env:JAVA21_HOME = $Java21Home
$env:JAVA_HOME   = $Java17Home

Write-OK "JAVA17_HOME configurado."
Write-OK "JAVA21_HOME configurado."
Write-OK "JAVA_HOME configurado para JDK 17."

# ============================================================
# 7. PATH JAVA
# ============================================================

Write-Step "7. Configurando Java no PATH"

#
# Usamos %JAVA_HOME%\bin para permitir troca futura
#

Add-SystemPath "%JAVA_HOME%\bin"

# ============================================================
# 8. MAVEN
# ============================================================

Write-Step "8. Instalando/verificando Apache Maven $MavenVersion"

if (-not (Test-Path $MavenInstallRoot)) {

    New-Item `
        -ItemType Directory `
        -Path $MavenInstallRoot `
        -Force |
        Out-Null
}

if (Test-Path "$MavenHome\bin\mvn.cmd") {

    Write-OK "Maven $MavenVersion ja esta instalado."

}
else {

    $TempZip =
        Join-Path $env:TEMP "apache-maven-$MavenVersion-bin.zip"

    Write-Host "Download:"
    Write-Host $MavenUrl
    Write-Host ""

    Invoke-WebRequest `
        -Uri $MavenUrl `
        -OutFile $TempZip `
        -UseBasicParsing

    Write-OK "Download concluido."

    Write-Host "Extraindo Maven..."

    Expand-Archive `
        -Path $TempZip `
        -DestinationPath $MavenInstallRoot `
        -Force

    Remove-Item $TempZip -Force

    if (-not (Test-Path "$MavenHome\bin\mvn.cmd")) {
        throw "Instalacao do Maven nao foi encontrada em $MavenHome"
    }

    Write-OK "Maven $MavenVersion instalado."
}

# ============================================================
# 9. MAVEN_HOME
# ============================================================

Write-Step "9. Configurando MAVEN_HOME"

[Environment]::SetEnvironmentVariable(
    "MAVEN_HOME",
    $MavenHome,
    "Machine"
)

$env:MAVEN_HOME = $MavenHome

Write-OK "MAVEN_HOME=$MavenHome"

# ============================================================
# 10. PATH MAVEN
# ============================================================

Write-Step "10. Configurando Maven no PATH"

Add-SystemPath "%MAVEN_HOME%\bin"

Refresh-CurrentPath

#
# Garante funcionamento imediato nesta sessao
#

$env:Path =
    "$Java17Home\bin;$MavenHome\bin;$env:Path"

# ============================================================
# 11. VALIDAR JAVA 17
# ============================================================

Write-Step "11. Validando Java 17"

Write-Host ""
Write-Host "JAVA_HOME:"
Write-Host $env:JAVA_HOME

Write-Host ""
Write-Host "java -version:"
Write-Host ""

& "$Java17Home\bin\java.exe" -version

if ($LASTEXITCODE -ne 0) {
    throw "Falha na validacao do Java 17."
}

Write-Host ""
Write-Host "javac -version:"
Write-Host ""

& "$Java17Home\bin\javac.exe" -version

if ($LASTEXITCODE -ne 0) {
    throw "Falha na validacao do javac 17."
}

Write-OK "Java 17 funcional."

# ============================================================
# 12. VALIDAR JAVA 21
# ============================================================

Write-Step "12. Validando Java 21"

Write-Host ""
Write-Host "Java 21:"
Write-Host ""

& "$Java21Home\bin\java.exe" -version

if ($LASTEXITCODE -ne 0) {
    throw "Falha na validacao do Java 21."
}

Write-Host ""
Write-Host "javac 21:"
Write-Host ""

& "$Java21Home\bin\javac.exe" -version

if ($LASTEXITCODE -ne 0) {
    throw "Falha na validacao do javac 21."
}

Write-OK "Java 21 funcional."

# ============================================================
# 13. JAVA DEFAULT
# ============================================================

Write-Step "13. Validando Java default"

java -version

if ($LASTEXITCODE -ne 0) {
    throw "Comando java nao encontrado no PATH."
}

Write-OK "Java default disponivel."

# ============================================================
# 14. MAVEN
# ============================================================

Write-Step "14. Validando Maven"

mvn -version

if ($LASTEXITCODE -ne 0) {
    throw "Maven nao esta funcionando corretamente."
}

Write-OK "Maven funcional."

# ============================================================
# 15. HTTPS MAVEN CENTRAL
# ============================================================

Write-Step "15. Testando HTTPS para Maven Central"

try {

    $response = Invoke-WebRequest `
        -Uri $MavenCentralUrl `
        -Method Head `
        -UseBasicParsing `
        -TimeoutSec 30

    Write-OK "HTTPS Maven Central acessivel."

    Write-Host "HTTP Status: $($response.StatusCode)"
    Write-Host "URL: $MavenCentralUrl"

}
catch {

    Write-Fail "Nao foi possivel acessar Maven Central."

    Write-Host ""
    Write-Host "Possiveis causas:"
    Write-Host " - Proxy corporativo"
    Write-Host " - Firewall"
    Write-Host " - DNS"
    Write-Host " - SSL inspection"
    Write-Host " - Bloqueio HTTPS"
    Write-Host ""

    Write-Host $_.Exception.Message
}

# ============================================================
# 16. TESTE DNS/TCP
# ============================================================

Write-Step "16. Testando conectividade TCP Maven Central"

$TcpTest = Test-NetConnection `
    repo.maven.apache.org `
    -Port 443 `
    -WarningAction SilentlyContinue

if ($TcpTest.TcpTestSucceeded) {

    Write-OK "TCP/443 para repo.maven.apache.org funcionando."

}
else {

    Write-Warn "TCP/443 para Maven Central falhou."
}

# ============================================================
# 17. TESTE REAL MAVEN CENTRAL
# ============================================================

Write-Step "17. Testando download real via Maven"

Write-Host "Tentando resolver uma dependencia do Maven Central..."
Write-Host ""

mvn dependency:get `
    "-Dartifact=org.apache.commons:commons-lang3:3.17.0" `
    "-Dtransitive=false"

if ($LASTEXITCODE -eq 0) {

    Write-OK "Maven conseguiu baixar dependencia do Maven Central."

}
else {

    Write-Fail "Maven nao conseguiu resolver dependencia."
}

# ============================================================
# 18. RESULTADO
# ============================================================

Write-Step "18. Resumo da instalacao"

Write-Host ""

Write-Host "JAVA17_HOME:"
Write-Host "  $Java17Home"
Write-Host ""

Write-Host "JAVA21_HOME:"
Write-Host "  $Java21Home"
Write-Host ""

Write-Host "JAVA_HOME:"
Write-Host "  $Java17Home"
Write-Host ""

Write-Host "MAVEN_HOME:"
Write-Host "  $MavenHome"
Write-Host ""

Write-Host "Java default:"
java -version

Write-Host ""

Write-Host "Maven:"
mvn -version

Write-Host ""

Write-Host "============================================================" -ForegroundColor Green
Write-Host " AMBIENTE CONFIGURADO COM SUCESSO" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green

Write-Host ""
Write-Host "Java 17 = default"
Write-Host "Java 21 = disponivel para CI/builds especificos"
Write-Host ""
Write-Host "JAVA_HOME   = $Java17Home"
Write-Host "JAVA17_HOME = $Java17Home"
Write-Host "JAVA21_HOME = $Java21Home"
Write-Host "MAVEN_HOME  = $MavenHome"
Write-Host ""

Write-Host "Recomendacao:"
Write-Host "Feche e abra novamente PowerShell/Terminal/IDE para"
Write-Host "carregar as novas variaveis de ambiente."
Write-Host ""