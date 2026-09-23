# Testes e validação Oracle

Para preparar o ambiente Windows, consulte os [scripts de apoio aos testes](../test-support/README.md): instalação de Java/Maven e preparação do host Vagrant para o laboratório Oracle.

## Suíte local

O módulo SQL Server inclui testes JDBC simulados no build e um roteiro SQL de
integração manual em `sqlserver/verify.sql`. Instruções e limites estão em
[SQL Server experimental](SQLSERVER.md). A execução da suíte local não comprova
a instalação ou o funcionamento dos jobs em uma instância real.

```sh
mvn clean verify
```

Os testes padrão não conectam a um banco externo. Cobrem:

- gravação/leitura de metadados do perfil, exclusão do `.pwd` correspondente, rejeição de caminhos e credenciais embutidas;
- URLs de serviço, formato histórico e IPv6 entre colchetes;
- versões Oracle 8 a 23 na seleção do coletor, incluindo 12.1, 12.2 e 19;
- empréstimo, reutilização, limite e fechamento do pool com driver simulado;
- passagem de amostra JDBC simulada pelo coletor real até a persistência Berkeley DB, incluindo timestamp com milissegundos e consulta incremental;
- reabertura da base e consulta por índice primário e secundário;
- agregação de CPU/I/O, reset e planos SQL sem duplicação;
- geração de JPEG pelo ImageIO padrão.

Os relatórios ficam em `target/surefire-reports`. A integração contínua executa a suíte em Windows/Linux com Java 17/21. A existência do workflow não substitui a consulta dos resultados no GitHub.

## Integração opcional com Oracle 12c

O perfil `oracle-integration` executa testes `*IT` via Maven Failsafe. Ele não roda no build padrão ou no CI público. Os parâmetros são variáveis de ambiente locais:

| Variável | Conteúdo |
| --- | --- |
| `ASHV_ORACLE_URL` | URL sem credenciais, por exemplo `jdbc:oracle:thin:@//host:1521/servico` |
| `ASHV_ORACLE_USER` | Conta dedicada de teste |
| `ASHV_ORACLE_PASSWORD` | Senha, informada localmente sem publicar no Git |
| `ASHV_DIAGNOSTICS_PACK_AUTHORIZED` | `true` somente se o uso de ASH estiver autorizado no ambiente |

No PowerShell, informe a senha sem colocá-la no histórico de comandos:

```powershell
$env:ASHV_ORACLE_URL = Read-Host 'URL JDBC do ambiente de teste'
$env:ASHV_ORACLE_USER = Read-Host 'Usuario de teste'
$secret = Read-Host 'Senha' -AsSecureString
$pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secret)
try {
    $env:ASHV_ORACLE_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    mvn -Poracle-integration verify
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
    Remove-Item Env:ASHV_ORACLE_PASSWORD
    $secret.Dispose()
}
```

Esse comando testa conexão e consultas de leitura. Sem a variável de autorização de ASH, o teste de coleta é explicitamente **ignorado**, não aprovado. Quando houver autorização para usar ASH, defina a variável antes de executar o perfil; remova-a ao terminar. O teste usa uma pasta temporária de histórico e exige pelo menos uma amostra persistida. Um banco totalmente ocioso pode falhar por falta de amostras; providencie uma carga de teste conhecida sem usar produção.

Resultados ficam em `target/failsafe-reports`. Sem as variáveis obrigatórias, o teste de conexão falha com uma indicação de configuração ausente. Não publique logs brutos sem revisar nomes, SQLs ou informações do ambiente.

## Roteiro de homologação manual

1. Registrar versão exata (12.1/12.2), edição, patchset, driver, JDK, instância e container.
2. Revisar grants com o DBA e escolher ASH EE ou coleta por sessões, conforme o ambiente.
3. Executar os testes de integração e registrar também testes ignorados.
4. Na interface, criar perfil, fechar/reabrir, confirmar nova solicitação de senha e testar cancelamento.
5. Gerar carga conhecida de CPU e espera, acompanhar coleta por alguns ciclos e comparar contagens/tempos com consultas autorizadas do DBA.
6. Validar Top Activity, drilldown de SQL/sessão, plano, relatório e exportação gráfica.
7. Fechar, reabrir offline e comparar o histórico. Testar desconexão/reconexão em ambiente controlado.
8. Registrar divergências e repetir para cada versão adicional. Não declarar 19c ou posterior homologado com base somente no resultado do 12c.

Não há cobertura completa de todos os fluxos Swing, todos os coletores, falhas de rede ou migração entre versões de armazenamento.
