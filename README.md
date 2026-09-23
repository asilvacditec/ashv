# ASH Viewer

Aplicação desktop Java Swing para analisar atividade de sessões Oracle, com gráficos de Top Activity, detalhamento de SQL e sessões, planos de execução e histórico local Berkeley DB.

Esta branch também inclui uma [integração experimental com SQL Server 2012+](docs/SQLSERVER.md): coleta agendada na instância, retenção configurável e uma tela de leitura do histórico. Execute `run.bat --sqlserver` (ou `sh run.sh --sqlserver`) após o build. A homologação em SQL Server real está pendente.

Desenvolvido originalmente por **Alex Kardapolov**. Modernização e manutenção atual: **Aparecido Silva**. Os créditos, cabeçalhos e avisos dos demais autores foram preservados: [AUTHORS.md](AUTHORS.md) e [notice.txt](notice.txt).

## Compilar e executar

Instale um JDK 17 e Maven 3.9.x, configure `JAVA_HOME` para o diretório do JDK e disponibilize `mvn` no `PATH`.

No Windows, os scripts opcionais em [test-support](test-support/README.md) preparam Java/Maven e Vagrant para o ambiente de testes.

```sh
mvn clean verify
```

No Windows, execute `run.bat`. No Linux/macOS, execute `sh run.sh`. O pacote fica em `target/ash-viewer.jar`, acompanhado de `target/lib/`; mantenha os dois juntos. Os scripts usam `JAVA_HOME` ou o Java do `PATH`.

Crie uma conexão com host, porta, serviço Oracle e usuário dedicado. O campo historicamente chamado SID é utilizado como **service name**, inclusive para PDB. As senhas ficam somente na memória da sessão e são solicitadas novamente ao reabrir a aplicação. O modo offline permite consultar históricos sem conectar ao Oracle.

## Situação da modernização

- Build Maven com Java 17, dependências declaradas e scripts portáveis.
- Perfis sem armazenamento de senha; criptografia fixa antiga removida.
- Código da aplicação em `src/org/ash`; fontes de terceiros adaptados em `third-party/src`.
- Testes locais de conexão simulada, coleta, persistência, agregação e perfis; CI em Windows/Linux e Java 17/21.
- Oracle 12c é o primeiro ambiente real previsto para homologação. **12c, 19c e posteriores ainda não foram homologados em banco real nesta entrega.**
- Berkeley DB 3.3.75 e SwingX 1.0 permanecem dependências legadas explícitas. Há código antigo e avisos de depreciação; a modernização não equivale a uma reescrita completa.

## Documentação

| Documento | Conteúdo |
| --- | --- |
| [Backup e versão original](docs/BACKUP.md) | Tag, commit, cópias e restauração isolada |
| [Instalação](docs/INSTALL.md) | Build, execução e solução de problemas |
| [Arquitetura](docs/ARCHITECTURE.md) | Módulos, fluxo e separação de terceiros |
| [Segurança](docs/SECURITY.md) | Senhas, migração dos perfis e dados locais |
| [Privilégios Oracle](docs/ORACLE_PRIVILEGES.md) | Conta dedicada e grants por funcionalidade |
| [Compatibilidade](docs/COMPATIBILITY.md) | Matriz de evidências e limites |
| [Testes](docs/TESTING.md) | Suíte local e integração Oracle 12c |
| [SQL Server experimental](docs/SQLSERVER.md) | Instalação do repositório, jobs, retenção e consulta do histórico |
| [Criar ORCL na VM Vagrant](docs/ORACLE_VAGRANT.md) | Descoberta da instalação, dimensionamento e criação automática com DBCA |
| [Mudanças](CHANGELOG.md) | Alterações e pendências |
| [Terceiros](third-party/README.md) | Dependências preservadas e adaptações |

O acesso a `V$ACTIVE_SESSION_HISTORY` integra o Oracle Diagnostics Pack; confirme o direito de uso no ambiente antes de selecionar a coleta ASH. Consulte [a documentação oficial da Oracle](https://docs.oracle.com/en/database/oracle/oracle-database/19/dblic/Licensing-Information.html) e a documentação de privilégios deste projeto.

O projeto mantém a licença original [GPL versão 3 ou posterior](license.txt). As bibliotecas possuem seus próprios termos e avisos. Os arquivos `readme.txt` e `notice.txt` são registros históricos; as instruções atuais estão nesta documentação Markdown.
