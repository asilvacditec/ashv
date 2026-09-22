# Criar o banco ORCL na VM Vagrant

O script [oracle/create-orcl.sh](../oracle/create-orcl.sh) foi escrito para uma VM **Linux já provisionada com os binários Oracle**, mas sem o banco ORCL. Contribuição: **Aparecido Silva**, mantendo os créditos do ASH Viewer e de seus autores.

Ele cria a instância e o banco pelo **DBCA da própria instalação**, gera um plano `initORCL.planned.ora` e exporta o **`init.ora` efetivo do SPFILE** após a criação. O DBCA também instala o dicionário de dados e executa as etapas internas de criação apropriadas à versão; apenas criar um `init.ora` não seria suficiente para criar um banco utilizável.

## Execução

Entre na VM:

```sh
vagrant ssh
```

Se este repositório estiver compartilhado em `/vagrant`, execute **dentro da VM**:

```sh
# Opcional: verificar descoberta e dimensionamento, sem criar o banco.
sudo bash /vagrant/oracle/create-orcl.sh --dry-run

# Criar ORCL automaticamente, sem perguntas interativas.
sudo bash /vagrant/oracle/create-orcl.sh
```

Caso o compartilhamento tenha outro caminho, ajuste o comando; também é possível copiar o script para a VM. O script pode ser lido de `/vagrant`, mas os **datafiles são criados em filesystem local**, nunca no compartilhamento Vagrant.

Executado como root, o script identifica o dono do binário `oracle` e troca para esse usuário com `runuser` antes da criação. Também pode ser executado diretamente como esse usuário. O usuário precisa pertencer ao grupo OSDBA configurado na instalação e ter acesso de escrita ao Oracle Base e ao diretório de dados. O script não instala Oracle, não cria usuários Linux e não executa `root.sh`.

Se houver mais de uma instalação, informe qual usar:

```sh
sudo bash /vagrant/oracle/create-orcl.sh \
  --oracle-home /u01/app/oracle/product/19.0.0/dbhome_1
```

Para escolher o destino ou limitar memória:

```sh
sudo bash /vagrant/oracle/create-orcl.sh \
  --oracle-home /u01/app/oracle/product/19.0.0/dbhome_1 \
  --oracle-base /u01/app/oracle \
  --data-dir /u02/oradata \
  --memory-mb 2048 \
  --port 1521
```

O diretório pai escolhido precisa ser gravável pelo dono do Oracle Home. Os valores de memória são MiB. O script rejeita um orçamento maior que o calculado como disponível; não usa swap como se fosse RAM.

## Log de debug para diagnóstico

O diagnóstico é automático, sem precisar de `bash -x`. Execute dentro da VM:

```sh
sudo bash /vagrant/oracle/create-orcl.sh --dry-run
```

A primeira linha informa um arquivo exclusivo, por exemplo `/tmp/create-orcl-debug.ABC12345.log`. Ele registra também falhas anteriores à criação dos diretórios: descoberta do Oracle, versão, usuário, limites de recursos, memória, discos candidatos, permissões, listener e artefatos existentes. Cada etapa tem horário UTC; falhas inesperadas incluem linha, pilha de funções e código de saída. O encerramento informa o código final e a última etapa.

Para executar a criação com o mesmo diagnóstico, retire `--dry-run`. Durante DBCA, a saída detalhada continua no `provision/dbca.log`; ao terminar essa etapa, o debug recebe as últimas 80 linhas, com o código de saída original. O mesmo vale para iniciar o listener e validar o banco com SQL*Plus. O log não elimina a proteção contra bancos ou arquivos existentes.

Quando iniciado como root, há um arquivo para root e outro para o usuário Oracle. **O primeiro arquivo inclui a saída do segundo**, inclusive falhas de `runuser`; compartilhe o primeiro. Para ler, substitua o exemplo pelo caminho mostrado na sua execução:

```sh
sudo cat /tmp/create-orcl-debug.ABC12345.log
```

As senhas aleatórias geradas pelo script são substituídas por `[SENHA_REMOVIDA]` no debug, inclusive se o DBCA as repetir. Não são registrados o arquivo de credenciais, o arquivo de resposta completo ou um dump do ambiente. Revise o conteúdo antes de compartilhar: caminhos, usuário local e mensagens dos utilitários aparecem, e o filtro não identifica qualquer segredo externo que um utilitário personalizado possa imprimir. Não compartilhe `credentials.env`, `dbca.rsp` ou logs brutos do Oracle. O debug é criado com permissão 600 em `/tmp`, inclusive no dry-run, e pode desaparecer na limpeza de temporários da VM.

## Descoberta e dimensionamento

| Item | Comportamento |
| --- | --- |
| Oracle Home | Usa `--oracle-home`, depois `ORACLE_HOME` válido; caso contrário examina `oratab`, inventário, `sqlplus` no PATH e caminhos usuais em `/u01`, `/u02`, `/opt` e `/home/oracle` |
| Versão | Lê `sqlplus -V` da instalação selecionada |
| Oracle Base | Usa `--oracle-base`, utilitário `orabase` ou caminho convencional anterior a `/product/` |
| Oracle Home somente leitura | Consulta `orabaseconfig` quando disponível para localizar `dbs` |
| RAM | Lê `/proc/meminfo`; usa `MemAvailable`, com aproximação por memória livre/cache nos kernels antigos |
| Reserva do SO | Pelo menos 768 MiB ou 25% da RAM total, o que for maior |
| SGA + PGA | Até 50% da RAM total, limitado à memória disponível após a reserva e a 8 GiB; arredondado para blocos de 64 MiB |
| Distribuição | Aproximadamente 75% SGA e 25% PGA; PGA é um alvo, não um limite rígido de consumo |
| Memória mínima calculada | 512 MiB para 10g/11g, 1 GiB para non-CDB moderno, 1,5 GiB para CDB |
| CPU | Registra CPUs online e limita `parallel_max_servers` a duas vezes esse número, no máximo 16 |
| Dados | Escolhe o maior espaço livre entre diretórios candidatos graváveis; `--data-dir` tem prioridade |
| Disco mínimo | 12 GiB livres para non-CDB e 20 GiB para CDB; pelo menos 1.024 inodes livres |
| FRA | Dimensiona o limite em 10% do espaço livre, entre 2 e 8 GiB, no mesmo destino dos dados |
| Temporários | Exige 1 GiB e 1.024 inodes livres em `/tmp` |

Esses são critérios conservadores para laboratório, não uma avaliação de capacidade para produção. O espaço livre é medido **dentro da VM**; o script não mede o disco físico do host Windows nem garante espaço futuro para datafiles com autoextend. Os arquivos de dados, FRA, logs e crescimento compartilham capacidade. A FRA não é um backup automático.

A política usa SGA/PGA e desativa AMM nas versões que possuem `memory_target`, evitando depender do tamanho de `/dev/shm` para AMM. Não modifica parâmetros de kernel, HugePages, limites do usuário, SELinux ou firewall. A instalação dos binários e os pré-requisitos do Oracle devem estar corretos; o DBCA ainda pode reportar requisitos adicionais.

## Versões e arquitetura

| Oracle detectado | Resultado planejado |
| --- | --- |
| 10g / 11g | Banco tradicional, SID e serviço `ORCL`, resposta DBCA no formato legado |
| 12.1.0.2 / 12.2 / 18c / 19c | CDB `ORCL` com uma PDB `ORCLPDB1`; opção `--non-cdb` disponível |
| 12.1.0.1 | Requer `--non-cdb` neste script |
| 21c / 23 | CDB obrigatório, com PDB `ORCLPDB1` |

Todas as opções usam `AL32UTF8` e conjunto nacional `AL16UTF16`. O nome solicitado `ORCL` identifica a instância e o banco container; em CDB, as aplicações normalmente conectam ao serviço `ORCLPDB1`.

O script exige Bash 4+, ferramentas GNU usuais, `flock`, `ss` ou `netstat`, DBCA, SQL*Plus, listener e o template `General_Purpose.dbc`. Não contempla RAC, ASM, Oracle XE/Free, bancos existentes, migrações ou outros sistemas operacionais. Versões desconhecidas são recusadas. A presença do template e os caminhos de instalação podem variar por edição; configurações não reconhecidas devem ser analisadas antes de adaptar o script.

**Esses caminhos de versão foram implementados e testados com comandos simulados; ainda não foram homologados criando bancos Oracle reais.**

## Arquivos gerados

O resumo do script informa o destino selecionado. Para um destino pai `/u02/oradata`, a organização é:

```text
/u02/oradata/ORCL/
  data/                         # DBCA pode criar subdiretórios próprios aqui
  recovery/                     # fast recovery area
  provision/
    plan.txt                    # versão e recursos detectados
    initORCL.planned.ora         # parâmetros calculados antes do DBCA
    init.ora                    # parâmetros efetivos, exportados do SPFILE
    credentials.env             # senhas aleatórias de SYS, SYSTEM e PDBADMIN
    orcl.env                    # ambiente para tarefas administrativas
    dbca.log
    listener.log                # quando o script inicia seu próprio listener
    verify.sql
    verify.log
    network/listener.ora        # quando cria um listener próprio
    SUCCESS                     # somente após validar banco e PDB abertos
```

Os arquivos nascem com permissões restritas: diretórios 700 e arquivos 600. As senhas são distintas, geradas por `/dev/urandom`, não aparecem nos argumentos dos processos nem na saída normal. O arquivo temporário `dbca.rsp`, que também contém senhas, é removido ao concluir ou falhar normalmente. `SIGKILL` ou queda de energia podem impedir a limpeza; nesse caso ele permanece protegido no diretório privado. Os logs do DBCA devem ser tratados como sensíveis.

Para ver as credenciais, entre como o dono da instalação e leia localmente `credentials.env`. Não publique esse arquivo nem os logs brutos. Não use `SYS`/`SYSTEM` como usuário de monitoramento do ASH Viewer: crie depois a conta dedicada descrita em [ORACLE_PRIVILEGES.md](ORACLE_PRIVILEGES.md).

O `init.ora` exportado reflete as decisões finais do DBCA, inclusive caminhos dos controlfiles. Ele pode diferir do plano. O banco normalmente utiliza o SPFILE criado pelo DBCA; a exportação é um arquivo textual para inspeção e recuperação administrativa.

O endereço `LOCAL_LISTENER` é aplicado com `ALTER SYSTEM ... SCOPE=BOTH` depois da criação, antes de exportar o PFILE e executar `ALTER SYSTEM REGISTER`. Ele não integra o `INITPARAMS` enviado ao DBCA: isso evita passar um descritor Oracle Net com parênteses e sinais de igualdade pelo parser do arquivo de resposta. O plano inicial não inclui esse parâmetro; o PFILE efetivo o inclui. A porta escolhida por `--port` é preservada.

Para DBCA 10g/11g, `sga_target`, `pga_aggregate_target` e `db_recovery_file_dest_size` são enviados em MiB inteiros, sem sufixo, no `INITPARAMS`; `sga_max_size` é enviado em bytes. O trace real do DBCA 11.2.0.4 mostrou que os três primeiros campos são multiplicados internamente por 1048576, enquanto `sga_max_size` é preservado. Enviar sufixo `M` produziu `Unexpected error!!!`; enviar bytes nesses três campos inflou os valores por outro fator de 1048576. O plano permanece em MiB e `TOTALMEMORY` continua em MiB. Os testes simulados reproduzem essa diferença de unidades; a criação com a correção ainda precisa ser confirmada na VM. Não aumente limites do kernel para acomodar valores inflados por erro de unidade.

## Listener e acesso pelo host

No Oracle 11g, o script omite `memory_max_target` e mantém `memory_target=0` e `AUTOMATICMEMORYMANAGEMENT=FALSE`. No laboratório 11.2.0.4, mesmo com SGA/PGA corretamente dimensionadas, a presença explícita de `memory_max_target=0` causou ORA-00843/ORA-00849 na inicialização. A omissão evita impor esse máximo explícito; não aumenta a SGA nem altera parâmetros do kernel. A correção ainda requer validação na VM.

Se a porta já responder como listener Oracle, ele é reutilizado sem reinício. Se estiver livre, o script inicia `ORCL_LISTENER` com configuração privada, ouvindo em `0.0.0.0`. Se a porta estiver ocupada por outro serviço, a criação é interrompida; use `--port`.

O script registra a instância no listener local, mas **não altera a rede Vagrant**. Para conectar pelo Windows, configure previamente uma rede privada da VM ou o encaminhamento da porta no seu Vagrantfile. O endereço e a porta usados no ASH Viewer devem corresponder a essa configuração.

Para preparar uma sessão administrativa depois:

```sh
source /u02/oradata/ORCL/provision/orcl.env
sqlplus / as sysdba
```

O script deixa o banco aberto e salva o estado da PDB nas versões contempladas. Não instala serviço systemd, não altera inicialização automática no boot e não modifica manualmente entradas existentes de `oratab`; o DBCA pode registrar a nova instância conforme a instalação local. Após reiniciar a VM, use os mecanismos administrativos da sua instalação para iniciar listener e banco.

## Reexecução e falhas

O script recusa `ORCL` já registrado em `oratab`, processo PMON existente, PFILE/SPFILE/password file com esse SID, diretório administrativo ou destino de dados já existente. Usa `flock` por Oracle Home e reserva o diretório de dados com `mkdir` exclusivo.

Se a criação falhar, a segunda execução também será recusada por encontrar os artefatos parciais. Isso é intencional: diagnostique `dbca.log`, `verify.log` e o estado real do Oracle antes de decidir como recuperar o laboratório. **Não há opção `--force`, `DROP DATABASE`, limpeza recursiva de datafiles ou tentativa automática de recriação.**

## Verificação do script

No laboratório Oracle 11.2.0.4, foi observada uma falha `TNS-04414` / `TNS-04605` durante `Copying database files`, apontando um `(` inesperado em `ADDRESS`. A passagem do descritor `LOCAL_LISTENER` em `INITPARAMS` foi tratada como causa provável e removida desse caminho; a correção ainda requer validação real na VM. O aviso anterior sobre buffer cache mínimo de 16 MB, isoladamente, não identifica a causa da falha. A atualização do script não recupera os artefatos da tentativa anterior: examine o log interno em `$ORACLE_BASE/cfgtoollogs/dbca/ORCL/ORCL.log` e o estado da instância antes de tentar criar novamente.

```sh
bash -n oracle/create-orcl.sh
shellcheck -x oracle/create-orcl.sh tests/shell/create-orcl-test.sh
bash tests/shell/create-orcl-test.sh
```

A suíte simula executáveis Oracle e recursos do servidor: dimensionamento, dry-run, criação em formatos antigos/modernos, credenciais, reexecução, falhas DBCA/SQL, listener, memória/disco insuficientes e caminhos inválidos. Não precisa de Oracle nem cria banco. Há um workflow dedicado a esses checks.

Os 13 cenários simulados incluem falha antes do provisionamento, erro inesperado de comando, preservação do código de saída do DBCA, remoção de senhas do debug e falha na troca de usuário. A criação real na VM Vagrant ainda precisa ser executada no ambiente do usuário; esses testes não certificam os binários ou a configuração da VM.

Referências oficiais para os formatos e a criação automatizada: [DBCA e criação de banco](https://docs.oracle.com/en/database/oracle/oracle-database/19/admin/creating-and-configuring-an-oracle-database.html), [exemplo de resposta Oracle 12.1](https://github.com/oracle/docker-images/blob/main/OracleDatabase/SingleInstance/dockerfiles/12.1.0.2/dbca.rsp.tmpl) e [exemplo de resposta Oracle 19c](https://github.com/oracle/docker-images/blob/main/OracleDatabase/SingleInstance/dockerfiles/19.3.0/dbca.rsp.tmpl).
