# Instalação e execução

## Requisitos

- JDK 17; Java 21 está incluído na matriz de CI.
- Apache Maven 3.9.x, instalado conforme [as instruções oficiais](https://maven.apache.org/install).
- Acesso HTTPS ao Maven Central durante o primeiro build.
- Ambiente gráfico para Swing e acesso de rede ao Oracle para coleta online.

```sh
java -version
mvn -version
mvn clean verify
```

O Maven compila também os fontes adaptados em `third-party/src`, copia os recursos e gera `target/ash-viewer.jar` com um manifesto que referencia `target/lib/`. Nenhuma classe versionada em `bin` é usada.

Execute `run.bat` no Windows ou `sh run.sh` em sistemas Unix. Os scripts mudam para a pasta do projeto, fixando o local relativo de perfis e históricos, e reservam até 768 MB de heap. Para ajustar memória, execute manualmente a partir da raiz:

```sh
java -Xmx1g -jar target/ash-viewer.jar
```

O `build.xml` é apenas uma ponte para Maven, para quem ainda chama Ant; Maven é a configuração mantida. A interface mantém rótulos e idiomas originais.

## Conexão

Use usuário dedicado e o campo `Service name`. Para PDB, informe o serviço da PDB. Novos perfis usam `jdbc:oracle:thin:@//host:1521/servico`. A leitura dos perfis históricos `host:porta:SID` foi mantida; a interface já convertia historicamente esse último campo em serviço na conexão.

Não coloque usuário/senha na URL. O driver utilizado é `oracle.jdbc.OracleDriver`. O campo de senha informa que o valor não é salvo; ao reabrir um perfil a senha é solicitada antes de conectar. A consulta offline não solicita senha.

## Ambiente local desta entrega

O JDK Temurin e Maven utilizados para validar foram extraídos em `.tools/`, sem alterar o Java global ou o `PATH` permanente. Essa pasta é ignorada pelo Git. Em uma nova máquina, instale as ferramentas normalmente. A versão exata e os resultados estão em [VALIDATION.md](VALIDATION.md).

## Problemas comuns

| Sintoma | Verificação |
| --- | --- |
| `java` ou `mvn` não encontrado | Verificar `JAVA_HOME`, `PATH` e abrir novo terminal |
| `UnsupportedClassVersionError` | Executar com Java 17 ou superior |
| JAR não encontrado | Executar `mvn clean verify` antes do script |
| Dependência ausente em modo offline | Fazer o primeiro build com acesso ao Maven Central |
| Erro de serviço Oracle | Conferir host, porta, listener e service name da PDB |
| `ORA-00942` / `ORA-01031` | Revisar os grants específicos em ORACLE_PRIVILEGES.md |
| Perfil antigo não aceita senha | Redigitar a senha; arquivos `.pwd` não são mais lidos |

Há avisos de depreciação no código legado. Eles não devem ser confundidos com sucesso de homologação Oracle ou cobertura integral da interface.
