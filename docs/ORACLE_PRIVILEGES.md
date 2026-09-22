# Conta dedicada e privilégios Oracle

Os exemplos destinam-se a um DBA no ambiente de teste. Não use `SYSTEM` como usuário de monitoramento, nem conceda `DBA`, `SELECT ANY DICTIONARY` ou `SELECT_CATALOG_ROLE` apenas para executar o viewer. Crie uma conta local no container/PDB apropriado e conceda os acessos por objeto.

## Base de leitura

Depois de criar a conta `ASH_READER` com uma senha definida fora do repositório, o DBA pode revisar e executar [oracle/grants-base.sql](../oracle/grants-base.sql). Os acessos correspondem às consultas do código:

| Objeto | Uso |
| --- | --- |
| `SYS.V_$DATABASE`, `SYS.V_$INSTANCE` | Identidade do banco e instância |
| `SYS.V_$PARAMETER` | Parâmetros como `cpu_count` |
| `SYS.DBA_USERS` | Mapeamento de ID para nome de usuário |
| `SYS.V_$SQL` | Texto e tipo de SQL |
| `SYS.V_$SQL_PLAN` | Planos de execução |

O pool tenta definir módulo/ação com `DBMS_APPLICATION_INFO`. Esse acesso costuma existir via `PUBLIC`; o script não adiciona execução de pacotes automaticamente. Se a política local o removeu, o DBA decide se concede `EXECUTE ON SYS.DBMS_APPLICATION_INFO`; a ausência não deve impedir a coleta.

## Coleta ASH

O modo EE consulta `SYS.V_$ACTIVE_SESSION_HISTORY`. O grant está separado em [oracle/grants-ash.sql](../oracle/grants-ash.sql). A disponibilidade da view ou a concessão de SELECT não comprovam direito de uso. A Oracle inclui essa view no Diagnostics Pack, conforme [Licensing Information](https://docs.oracle.com/en/database/oracle/oracle-database/19/dblic/Licensing-Information.html). O responsável pelo banco deve confirmar o licenciamento aplicável antes da coleta.

## Coleta por sessões

O coletor histórico SE usa `V_$SESSION`, `V_$SESSTAT` e `V_$MYSTAT`: [oracle/grants-session.sql](../oracle/grants-session.sql). Esse caminho também precisa de homologação específica na versão/edição utilizada; conceder esses objetos não valida automaticamente toda a compatibilidade ou o licenciamento do ambiente.

## Funcionalidades opcionais e legadas

Trace via `SYS.DBMS_MONITOR` altera o estado da sessão e não é necessário para monitoramento de leitura. A conta padrão não recebe esse privilégio. O DBA pode executar trace por seus próprios procedimentos administrativos.

Os coletores 8i/9i consultam views de estruturas `X$` (`SYS.X_$KSUSE`, `SYS.X_$KSUSECST`) e `V_$EVENT_NAME`. Permanecem como código histórico, sem homologação com o driver atual. Não execute automaticamente os comandos administrativos do `readme.txt` original em bancos modernos.

## CDB, PDB e RAC

Conecte-se ao serviço e container que serão monitorados. Não conceda `CONTAINER=ALL` indiscriminadamente. O aplicativo usa views `V$` locais; não agrega instâncias via `GV$` e o modelo não separa dados por `CON_ID`. A homologação inicial deve usar uma instância e um escopo de container bem definido.
