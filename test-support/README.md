# Apoio ao ambiente de testes

Esta pasta reúne os scripts de preparação do Windows para compilar e testar o
ASH Viewer e preparar o laboratório com VM Oracle. São utilitários opcionais de
apoio; os testes da aplicação ficam em [`tests/`](../tests/).

## Scripts

| Script | Finalidade |
| --- | --- |
| [setup-java-maven.ps1](setup-java-maven.ps1) | Instala Temurin JDK 17 e 21 via winget e Maven 3.9.16 em `C:\Tools`; configura `JAVA17_HOME`, `JAVA21_HOME`, `JAVA_HOME` (17), `MAVEN_HOME` e o PATH do sistema. Valida Java/Maven e testa acesso e download no Maven Central. |
| [setup-vagrant.ps1](setup-vagrant.ps1) | Instala Vagrant via winget quando necessário, verifica Git e VirtualBox, lista plugins e boxes, testa acesso ao catálogo e prepara `C:\OracleLab`. Registra um log em `%TEMP%\VagrantSetup\setup-vagrant-*.log`. |

## Execução

Use Windows 11, PowerShell 5.1 ou superior e `winget` disponível, com acesso à
internet para os downloads. Abra o PowerShell **como Administrador** e, na raiz
do repositório, execute o script correspondente ao ambiente que deseja preparar:

```powershell
.\test-support\setup-java-maven.ps1

# Para o laboratório com VM, com Git e VirtualBox já instalados:
.\test-support\setup-vagrant.ps1
```

Os scripts instalam ferramentas e alteram o ambiente local. Após a execução,
reabra o terminal e a IDE para carregar as variáveis atualizadas. Confira as
mensagens de cada etapa: alguns diagnósticos de conectividade apenas imprimem
avisos ou erros e não interrompem a execução.

## Próximos passos

Para compilar e executar a suíte local, na raiz do repositório:

```powershell
java -version
mvn -version
mvn clean verify
```

O setup do Vagrant prepara o host; não baixa uma box, não cria uma VM e não
instala o Oracle. Com a VM e os binários Oracle provisionados, consulte
[a criação do banco no laboratório](../docs/ORACLE_VAGRANT.md), incluindo o
`oracle/dbca.sh` para 11gR2. Os procedimentos de teste da aplicação estão em
[Testes e validação Oracle](../docs/TESTING.md).
