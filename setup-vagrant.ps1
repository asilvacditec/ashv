#Requires -Version 5.1
#Requires -RunAsAdministrator

<#
.SYNOPSIS
    Instala e valida HashiCorp Vagrant no Windows 11.

.DESCRIPTION
    - Verifica privilegios administrativos
    - Verifica winget
    - Detecta instalacao existente do Vagrant
    - Instala HashiCorp Vagrant via winget
    - Atualiza PATH da sessao atual
    - Valida Vagrant
    - Valida VirtualBox
    - Valida Git
    - Verifica provider VirtualBox
    - Lista plugins instalados
    - Executa diagnosticos basicos

.NOTES
    Requisitos:
      Windows 11
      PowerShell 5.1+
      VirtualBox ja instalado
      Git ja instalado

    Execute PowerShell como Administrador.
#>

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# ============================================================
# CONFIGURACAO
# ============================================================

$VagrantPackageId = "Hashicorp.Vagrant"

$LogDirectory = Join-Path $env:TEMP "VagrantSetup"
$LogFile = Join-Path `
    $LogDirectory `
    ("setup-vagrant-{0}.log" -f (Get-Date -Format "yyyyMMdd-HHmmss"))

# ============================================================
# FUNCOES
# ============================================================

function Write-Section {

    param(
        [Parameter(Mandatory)]
        [string]$Message
    )

    Write-Host ""
    Write-Host "============================================================" `
        -ForegroundColor Cyan

    Write-Host $Message -ForegroundColor Cyan

    Write-Host "============================================================" `
        -ForegroundColor Cyan
}

function Write-OK {

    param([string]$Message)

    Write-Host "[OK]   $Message" -ForegroundColor Green
}

function Write-Warn {

    param([string]$Message)

    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Write-Fail {

    param([string]$Message)

    Write-Host "[ERRO] $Message" -ForegroundColor Red
}

function Refresh-EnvironmentPath {

    $MachinePath = [Environment]::GetEnvironmentVariable(
        "Path",
        "Machine"
    )

    $UserPath = [Environment]::GetEnvironmentVariable(
        "Path",
        "User"
    )

    $env:Path = "$MachinePath;$UserPath"
}

function Get-CommandPath {

    param(
        [Parameter(Mandatory)]
        [string]$Command
    )

    $cmd = Get-Command $Command `
        -ErrorAction SilentlyContinue

    if ($cmd) {
        return $cmd.Source
    }

    return $null
}

function Test-WingetPackageInstalled {

    param(
        [Parameter(Mandatory)]
        [string]$PackageId
    )

    try {

        $output = winget list `
            --id $PackageId `
            --exact `
            --accept-source-agreements 2>$null |
            Out-String

        return (
            $output -match [regex]::Escape($PackageId)
        )

    }
    catch {

        return $false
    }
}

# ============================================================
# INICIO DO LOG
# ============================================================

if (-not (Test-Path $LogDirectory)) {

    New-Item `
        -ItemType Directory `
        -Path $LogDirectory `
        -Force |
        Out-Null
}

Start-Transcript `
    -Path $LogFile `
    -Append |
    Out-Null

try {

    Clear-Host

    Write-Host ""
    Write-Host "VAGRANT SETUP - WINDOWS 11" `
        -ForegroundColor Cyan

    Write-Host "HashiCorp Vagrant + VirtualBox"
    Write-Host ""

    # ========================================================
    # 1. WINDOWS
    # ========================================================

    Write-Section "1. Verificando sistema operacional"

    $OS = Get-CimInstance Win32_OperatingSystem

    Write-Host "Sistema : $($OS.Caption)"
    Write-Host "Versao  : $($OS.Version)"
    Write-Host "Build   : $($OS.BuildNumber)"
    Write-Host "64 bits : $([Environment]::Is64BitOperatingSystem)"

    if (-not [Environment]::Is64BitOperatingSystem) {
        throw "Sistema operacional 64 bits necessario."
    }

    Write-OK "Sistema operacional compativel."

    # ========================================================
    # 2. WINGET
    # ========================================================

    Write-Section "2. Verificando Windows Package Manager"

    $WingetPath = Get-CommandPath "winget"

    if (-not $WingetPath) {

        throw @"
winget nao foi encontrado.

Instale/atualize o Microsoft App Installer e execute novamente.
"@
    }

    $WingetVersion = winget --version

    Write-Host "Executavel: $WingetPath"
    Write-Host "Versao    : $WingetVersion"

    Write-OK "winget disponivel."

    # ========================================================
    # 3. GIT
    # ========================================================

    Write-Section "3. Verificando Git"

    $GitPath = Get-CommandPath "git"

    if ($GitPath) {

        Write-Host "Executavel:"
        Write-Host "  $GitPath"

        Write-Host ""
        git --version

        Write-OK "Git encontrado."

    }
    else {

        Write-Warn "Git nao foi encontrado no PATH."
        Write-Warn "A instalacao do Vagrant continuara."
    }

    # ========================================================
    # 4. VIRTUALBOX
    # ========================================================

    Write-Section "4. Verificando Oracle VirtualBox"

    $VBoxManageCandidates = @(

        "$env:ProgramFiles\Oracle\VirtualBox\VBoxManage.exe",

        "${env:ProgramFiles(x86)}\Oracle\VirtualBox\VBoxManage.exe"

    )

    $VBoxManage = $null

    foreach ($candidate in $VBoxManageCandidates) {

        if (
            $candidate -and
            (Test-Path $candidate)
        ) {

            $VBoxManage = $candidate
            break
        }
    }

    if (-not $VBoxManage) {

        $VBoxCommand = Get-Command "VBoxManage.exe" `
            -ErrorAction SilentlyContinue

        if ($VBoxCommand) {
            $VBoxManage = $VBoxCommand.Source
        }
    }

    if ($VBoxManage) {

        Write-Host "VBoxManage:"
        Write-Host "  $VBoxManage"

        Write-Host ""

        $VBoxVersion = & $VBoxManage --version

        Write-Host "VirtualBox:"
        Write-Host "  $VBoxVersion"

        Write-OK "VirtualBox encontrado."

    }
    else {

        Write-Warn "VBoxManage.exe nao encontrado."
        Write-Warn "Confirme a instalacao do VirtualBox."
    }

    # ========================================================
    # 5. VAGRANT EXISTENTE
    # ========================================================

    Write-Section "5. Verificando instalacao existente do Vagrant"

    Refresh-EnvironmentPath

    $VagrantPath = Get-CommandPath "vagrant"

    if ($VagrantPath) {

        Write-Host "Vagrant ja esta disponivel:"
        Write-Host "  $VagrantPath"

        Write-Host ""

        vagrant --version

        Write-OK "Vagrant ja instalado."

    }
    else {

        Write-Host "Vagrant nao encontrado no PATH."
    }

    # ========================================================
    # 6. INSTALACAO
    # ========================================================

    Write-Section "6. Instalando HashiCorp Vagrant"

    $PackageInstalled =
        Test-WingetPackageInstalled $VagrantPackageId

    if ($PackageInstalled) {

        Write-OK "Pacote $VagrantPackageId ja instalado."

    }
    else {

        Write-Host "Instalando:"
        Write-Host "  $VagrantPackageId"
        Write-Host ""

        winget install `
            --id $VagrantPackageId `
            --exact `
            --silent `
            --accept-package-agreements `
            --accept-source-agreements

        $WingetExitCode = $LASTEXITCODE

        if ($WingetExitCode -ne 0) {

            throw (
                "Falha na instalacao do Vagrant. " +
                "winget ExitCode: $WingetExitCode"
            )
        }

        Write-OK "Vagrant instalado."
    }

    # ========================================================
    # 7. ATUALIZAR PATH
    # ========================================================

    Write-Section "7. Atualizando PATH da sessao"

    Refresh-EnvironmentPath

    #
    # Local padrao utilizado pelo instalador MSI do Vagrant.
    #

    $VagrantBin = "C:\HashiCorp\Vagrant\bin"

    if (
        (Test-Path $VagrantBin) -and
        ($env:Path -notlike "*$VagrantBin*")
    ) {

        $env:Path = "$VagrantBin;$env:Path"
    }

    $VagrantPath = Get-CommandPath "vagrant"

    if (-not $VagrantPath) {

        Write-Warn "Vagrant foi instalado, mas ainda nao esta no PATH."
        Write-Warn "Feche e abra novamente o PowerShell."

    }
    else {

        Write-Host "Vagrant:"
        Write-Host "  $VagrantPath"

        Write-OK "Vagrant disponivel no PATH."
    }

    # ========================================================
    # 8. VALIDAR VAGRANT
    # ========================================================

    Write-Section "8. Validando Vagrant"

    if (-not $VagrantPath) {

        throw @"
Nao foi possivel executar Vagrant nesta sessao.

Feche o PowerShell, abra novamente e execute:

    vagrant --version
"@
    }

    $VagrantVersion = vagrant --version

    Write-Host $VagrantVersion

    if ($LASTEXITCODE -ne 0) {
        throw "Falha ao executar vagrant --version."
    }

    Write-OK "Vagrant funcional."

    # ========================================================
    # 9. VALIDAR PLUGINS
    # ========================================================

    Write-Section "9. Verificando plugins Vagrant"

    $PluginOutput = vagrant plugin list

    Write-Host $PluginOutput

    if ($LASTEXITCODE -ne 0) {

        Write-Warn "Nao foi possivel listar plugins."

    }
    else {

        Write-OK "Sistema de plugins funcional."
    }

    # ========================================================
    # 10. VAGRANT HOME
    # ========================================================

    Write-Section "10. Verificando Vagrant Home"

    $VagrantHome = Join-Path $env:USERPROFILE ".vagrant.d"

    Write-Host "Vagrant Home:"
    Write-Host "  $VagrantHome"

    if (-not (Test-Path $VagrantHome)) {

        New-Item `
            -ItemType Directory `
            -Path $VagrantHome `
            -Force |
            Out-Null
    }

    Write-OK "Vagrant Home disponivel."

    # ========================================================
    # 11. BOXES
    # ========================================================

    Write-Section "11. Verificando boxes Vagrant existentes"

    $Boxes = vagrant box list

    Write-Host $Boxes

    Write-OK "Consulta de boxes concluida."

    # ========================================================
    # 12. VALIDAR VIRTUALBOX NOVAMENTE
    # ========================================================

    Write-Section "12. Validando VirtualBox para uso pelo Vagrant"

    if ($VBoxManage) {

        $VBoxVersion = & $VBoxManage --version

        Write-Host "VirtualBox detectado:"
        Write-Host "  $VBoxVersion"

        Write-OK "Provider VirtualBox disponivel no sistema."

    }
    else {

        Write-Warn "Nao foi possivel validar VBoxManage."
    }

    # ========================================================
    # 13. CRIAR DIRETORIO DO LAB
    # ========================================================

    Write-Section "13. Preparando diretorio opcional de laboratorio"

    $LabRoot = "C:\OracleLab"

    if (-not (Test-Path $LabRoot)) {

        New-Item `
            -ItemType Directory `
            -Path $LabRoot `
            -Force |
            Out-Null

        Write-OK "Diretorio criado: $LabRoot"

    }
    else {

        Write-OK "Diretorio ja existe: $LabRoot"
    }

    # ========================================================
    # 14. TESTE DE CONECTIVIDADE
    # ========================================================

    Write-Section "14. Testando acesso ao Vagrant Cloud"

    try {

        $Response = Invoke-WebRequest `
            -Uri "https://portal.cloud.hashicorp.com/vagrant/discover" `
            -Method Head `
            -TimeoutSec 30 `
            -UseBasicParsing

        Write-Host "HTTP Status: $($Response.StatusCode)"

        Write-OK "Acesso HTTPS ao catalogo Vagrant funcionando."

    }
    catch {

        Write-Warn "Teste HTTPS nao foi concluido."

        Write-Host ""
        Write-Host $_.Exception.Message
        Write-Host ""

        Write-Warn "Proxy/firewall corporativo pode estar interferindo."
    }

    # ========================================================
    # 15. RESUMO
    # ========================================================

    Write-Section "15. RESULTADO FINAL"

    Write-Host ""

    Write-Host "VAGRANT" -ForegroundColor White

    vagrant --version

    Write-Host ""

    Write-Host "Executavel:"
    Write-Host "  $VagrantPath"

    Write-Host ""

    Write-Host "VIRTUALBOX" -ForegroundColor White

    if ($VBoxManage) {

        Write-Host "  $(& $VBoxManage --version)"

    }
    else {

        Write-Host "  Nao validado"
    }

    Write-Host ""

    Write-Host "GIT" -ForegroundColor White

    if ($GitPath) {

        git --version

    }
    else {

        Write-Host "  Nao encontrado no PATH"
    }

    Write-Host ""

    Write-Host "VAGRANT HOME"
    Write-Host "  $VagrantHome"

    Write-Host ""

    Write-Host "ORACLE LAB"
    Write-Host "  $LabRoot"

    Write-Host ""

    Write-Host "LOG"
    Write-Host "  $LogFile"

    Write-Host ""

    Write-Host "============================================================" `
        -ForegroundColor Green

    Write-Host " VAGRANT CONFIGURADO COM SUCESSO" `
        -ForegroundColor Green

    Write-Host "============================================================" `
        -ForegroundColor Green

    Write-Host ""

    Write-Host "Comandos para validacao manual:"
    Write-Host ""

    Write-Host "  vagrant --version"
    Write-Host "  vagrant plugin list"
    Write-Host "  vagrant box list"
    Write-Host ""

    Write-Host "Recomendacao:"
    Write-Host "Feche e abra novamente o PowerShell antes de"
    Write-Host "iniciar seu primeiro ambiente Vagrant."
    Write-Host ""
}
catch {

    Write-Host ""
    Write-Fail "A instalacao/configuracao encontrou um problema."

    Write-Host ""
    Write-Host "Detalhes:"
    Write-Host $_.Exception.Message
    Write-Host ""

    Write-Host "Log:"
    Write-Host "  $LogFile"
    Write-Host ""

    exit 1
}
finally {

    try {
        Stop-Transcript | Out-Null
    }
    catch {
        # Nenhuma acao necessaria.
    }
}