#!/usr/bin/env bash
# ASH Viewer lab tooling. Contribution: Aparecido Silva. GPL-3.0-or-later.
# Creates a NEW filesystem database using the DBCA from the selected Oracle Home.
set +x
set -Eeuo pipefail
umask 077

die() { printf 'ERRO: %s\n' "$*" >&2; exit 1; }
info() { printf '%s\n' "$*"; }
stage() {
    debug_stage=$1
    printf '[%s] ETAPA: %s\n' "$(date -u '+%Y-%m-%dT%H:%M:%SZ')" "$debug_stage" >&2
}

# Never use xtrace: response-file generation expands database passwords.
redact_debug() { sed -u -E 's/Ora9[a-f0-9]{24}/[SENHA_REMOVIDA]/g'; }

run_logged() {
    local output=$1 label=$2 result=0
    shift 2
    stage "$label"
    "$@" > "$output" 2>&1 || result=$?
    printf 'Resultado %s: codigo=%s; log privado=%s\n' "$label" "$result" "$output" >&2
    printf '%s\n' '--- Ultimas 80 linhas (senhas geradas removidas) ---' >&2
    tail -n 80 -- "$output" | redact_debug >&2
    ((result == 0)) || exit "$result"
}

debug_run() (
    # A private, unique file also covers failures before ORACLE_HOME is known.
    local debug_log
    debug_log=$(mktemp /tmp/create-orcl-debug.XXXXXXXX.log) || exit 1
    printf 'Log de debug: %s\n' "$debug_log"
    (
        set -Eeuo pipefail
        stage inicializacao
        trap cleanup EXIT
        trap 'printf "ERRO inesperado: codigo=%s linha=%s funcao=%s etapa=%s\n" "$?" "$LINENO" "${FUNCNAME[*]}" "$debug_stage" >&2' ERR
        trap 'exit 130' INT
        trap 'exit 143' TERM
        printf 'Bash=%s; sistema=%s; usuario=%s; uid=%s\n' "$BASH_VERSION" "$(uname -srm)" "$(id -un)" "$(id -u)"
        printf 'Limites de recursos:\n'; ulimit -a
        main "$@"
    ) 2>&1 | redact_debug | tee -a "$debug_log"
)
usage() {
    cat <<'HELP'
Uso: bash create-orcl.sh [opcoes]
  --dry-run                 Somente descoberta e plano; nao cria o banco.
  --oracle-home /caminho     Seleciona uma instalacao quando ha mais de uma.
  --oracle-base /caminho     Sobrescreve a base detectada pelo orabase.
  --data-dir /caminho        Diretorio pai; o script reserva <caminho>/ORCL.
  --memory-mb NUMERO         Orcamento SGA+PGA em MiB, sujeito aos limites locais.
  --port NUMERO             Porta TCP do listener (padrao: 1521).
  --non-cdb                 Banco tradicional para Oracle 12c/18c/19c.
  --help                    Esta ajuda.

Execute dentro da VM Linux como dono do Oracle Home, ou como root (runuser).
Sem opcoes: SID ORCL, AL32UTF8, filesystem, senhas aleatorias, DBCA silencioso.
Oracle 10g/11g: non-CDB. Oracle 12c+: CDB ORCL com PDB ORCLPDB1.
Recusa bancos/arquivos existentes. Nao apaga nem retoma criacoes incompletas.
Log de debug automatico em /tmp/create-orcl-debug.*.log, inclusive no dry-run.
HELP
}

safe_path() {
    [[ $1 =~ ^/[a-zA-Z0-9_./+-]+$ ]] || die "Caminho absoluto sem espacos/caracteres especiais necessario: $1"
    [[ /$1/ != *'/../'* && /$1/ != *'/./'* ]] || die "Caminho nao normalizado: $1"
}

valid_home() { [[ -x $1/bin/oracle && -x $1/bin/sqlplus && -x $1/bin/dbca ]]; }

discover_home() {
    local candidate inventory pointer command_path
    local -a homes=()
    if [[ -n $home_option ]]; then
        valid_home "$home_option" || die "Oracle Home invalido ou sem DBCA: $home_option"
        ORACLE_HOME=$(readlink -f -- "$home_option")
        return
    fi
    if [[ -n ${ORACLE_HOME:-} ]] && valid_home "$ORACLE_HOME"; then
        ORACLE_HOME=$(readlink -f -- "$ORACLE_HOME")
        return
    fi
    while IFS= read -r candidate; do
        [[ -n $candidate ]] || continue
        valid_home "$candidate" || continue
        candidate=$(readlink -f -- "$candidate")
        [[ " ${homes[*]:-} " == *" $candidate "* ]] || homes+=("$candidate")
    done < <(
        for pointer in /etc/oratab /var/opt/oracle/oratab; do
            [[ ! -r $pointer ]] || awk -F: '!/^#/ && NF >= 2 {print $2}' "$pointer"
        done
        for pointer in /etc/oraInst.loc /var/opt/oracle/oraInst.loc; do
            [[ -r $pointer ]] || continue
            inventory=$(sed -n 's/^inventory_loc=//p' "$pointer")
            [[ ! -r $inventory/ContentsXML/inventory.xml ]] ||
                sed -n 's/.* LOC="\([^"]*\)".*/\1/p' "$inventory/ContentsXML/inventory.xml"
        done
        command_path=$(command -v sqlplus || true)
        if [[ -n $command_path ]]; then dirname -- "$(dirname -- "$(readlink -f -- "$command_path")")"; fi
        for pointer in /u01/app/oracle /u02/app/oracle /opt/oracle /opt/app/oracle /home/oracle; do
            [[ -d $pointer ]] || continue
            find "$pointer" -maxdepth 7 -type f -path '*/bin/oracle' -print 2>/dev/null |
                sed 's@/bin/oracle$@@' || true
        done
    )
    ((${#homes[@]})) || die 'Nenhuma instalacao completa encontrada. Use --oracle-home.'
    if ((${#homes[@]} > 1)); then
        printf 'Oracle Homes encontrados:\n'; printf '  %s\n' "${homes[@]}"
        die 'Selecione um deles com --oracle-home; nao e seguro escolher arbitrariamente.'
    fi
    ORACLE_HOME=${homes[0]}
}

detect_version() {
    local banner
    banner=$("$ORACLE_HOME/bin/sqlplus" -V) || die 'sqlplus -V falhou; confira bibliotecas do sistema.'
    printf 'SQL*Plus: %s\n' "$banner" >&2
    version=$(printf '%s\n' "$banner" | sed -nE 's/.*(Release|Version) ([0-9]+\.[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+).*/\2/p' | tail -n 1)
    [[ -n $version ]] || die 'Nao foi possivel interpretar a versao do SQL*Plus.'
    IFS=. read -r major minor _patch update _revision <<< "$version"
    case $major in 10|11|12|18|19|21|23) ;; *) die "Versao $version ainda nao contemplada; nao sera criado banco por tentativa." ;; esac
    [[ ${ORACLE_HOME,,} != *dbhomexe* && ${ORACLE_HOME,,} != *dbhomefree* ]] ||
        die 'XE/Free usa provisionamento proprio; este script nao altera essa instalacao.'
    cdb=false
    if ((major >= 12)) && [[ $non_cdb == false ]]; then cdb=true; fi
    if ((major >= 21)) && [[ $non_cdb == true ]]; then die 'Oracle 21+ exige arquitetura CDB.'; fi
    if [[ $cdb == true && $major == 12 && $minor == 1 && $update -lt 2 ]]; then
        die 'Para CDB, use 12.1.0.2 ou posterior; 12.1.0.1 requer --non-cdb.'
    fi
}

read_memory() {
    local input=${1:-/proc/meminfo}
    total_mb=$(awk '/^MemTotal:/ {print int($2/1024)}' "$input")
    available_mb=$(awk '/^MemAvailable:/ {print int($2/1024)}' "$input")
    if [[ -z $available_mb ]]; then
        available_mb=$(awk '/^(MemFree|Buffers|Cached):/ {n+=$2} END {print int(n/1024)}' "$input")
    fi
    swap_mb=$(awk '/^SwapFree:/ {print int($2/1024)}' "$input")
    [[ $total_mb =~ ^[0-9]+$ && $available_mb =~ ^[0-9]+$ ]] || die 'Memoria local nao identificada.'
}

plan_memory() {
    reserve_mb=$((total_mb / 4))
    ((reserve_mb >= 768)) || reserve_mb=768
    budget_mb=$((total_mb / 2))
    ((budget_mb <= available_mb - reserve_mb)) || budget_mb=$((available_mb - reserve_mb))
    ((budget_mb <= 8192)) || budget_mb=8192
    budget_mb=$((budget_mb / 64 * 64))
    minimum_mb=512
    ((major < 12)) || minimum_mb=1024
    [[ $cdb != true ]] || minimum_mb=1536
    ((budget_mb >= minimum_mb)) || die "RAM livre insuficiente: orcamento ${budget_mb} MiB; minimo ${minimum_mb}. Aumente a RAM da VM ou libere memoria."
    memory_mb=${memory_option:-$budget_mb}
    [[ $memory_mb =~ ^[1-9][0-9]{2,4}$ ]] || die '--memory-mb deve ser um inteiro em MiB.'
    ((memory_mb >= minimum_mb && memory_mb <= budget_mb)) || die "Memoria solicitada fora de ${minimum_mb}..${budget_mb} MiB."
    sga_mb=$((memory_mb * 3 / 4 / 64 * 64))
    pga_mb=$((memory_mb - sga_mb))
}

nearest_parent() {
    local path=$1
    while [[ ! -e $path ]]; do path=$(dirname -- "$path"); done
    [[ -d $path ]] || die "Nao e diretorio: $path"
    printf '%s\n' "$path"
}

filesystem_info() {
    local path=$1
    free_mb=$(df -Pk -- "$path" | awk 'END {print int($4/1024)}')
    free_inodes=$(df -Pi -- "$path" | awk 'END {print $4}')
    fs_type=$(stat -f -c %T -- "$path")
    [[ $free_mb =~ ^[0-9]+$ && $free_inodes =~ ^[0-9]+$ ]] || die "Espaco livre nao identificado em $path"
}

local_filesystem() {
    case $1 in vbox*|9p|nfs*|cifs|smb*|fuse*|tmpfs|ramfs) return 1 ;; *) return 0 ;; esac
}

select_storage() {
    local candidate parent best_free=-1
    local -a candidates=()
    if [[ -n $data_option ]]; then candidates=("$data_option")
    else candidates=(/u02/oradata /u01/oradata /oradata "$ORACLE_BASE/oradata" /opt/oracle/oradata); fi
    data_root=''
    for candidate in "${candidates[@]}"; do
        safe_path "$candidate"
        parent=$(nearest_parent "$candidate")
        printf 'Disco candidato=%s; ancestral=%s\n' "$candidate" "$parent" >&2
        [[ -w $parent ]] || { info "Sem permissao de escrita: $parent" >&2; continue; }
        filesystem_info "$parent"
        printf 'Filesystem=%s; livre_MiB=%s; inodes=%s\n' "$fs_type" "$free_mb" "$free_inodes" >&2
        local_filesystem "$fs_type" || continue
        ((free_inodes >= 1024)) || continue
        if ((free_mb > best_free)); then data_root=$candidate; best_free=$free_mb; fi
    done
    [[ -n $data_root ]] || die 'Nenhum filesystem local gravavel encontrado. Prepare um diretorio do usuario Oracle e use --data-dir.'
    disk_mb=$best_free
    minimum_disk_mb=12288
    [[ $cdb != true ]] || minimum_disk_mb=20480
    ((disk_mb >= minimum_disk_mb)) || die "Disco insuficiente em $data_root: ${disk_mb} MiB livres; minimo ${minimum_disk_mb}."
    fra_mb=$((disk_mb / 10))
    ((fra_mb >= 2048)) || fra_mb=2048
    ((fra_mb <= 8192)) || fra_mb=8192
    instance_root=$data_root/ORCL
    work_dir=$instance_root/provision
    data_dir=$instance_root/data
    recovery_dir=$instance_root/recovery
}

check_existing() {
    local file
    for file in /etc/oratab /var/opt/oracle/oratab; do
        if [[ -r $file ]] && awk -F: 'toupper($1)=="ORCL" {found=1} END {exit !found}' "$file"; then
            die "ORCL ja registrado em $file. Nao sera recriado."
        fi
    done
    if ps -eo comm= | awk 'toupper($1)=="ORA_PMON_ORCL" {found=1} END {exit !found}'; then die 'Instancia ORCL ja esta em execucao.'; fi
    for file in "$dbs_dir/initORCL.ora" "$dbs_dir/spfileORCL.ora" "$dbs_dir/orapwORCL" \
                "$ORACLE_HOME/dbs/initORCL.ora" "$ORACLE_HOME/dbs/spfileORCL.ora" "$ORACLE_HOME/dbs/orapwORCL" \
                "$ORACLE_BASE/admin/ORCL" "$instance_root"; do
        [[ ! -e $file && ! -L $file ]] || die "Artefato existente: $file. Nenhum arquivo sera sobrescrito."
    done
}

inspect_listener() {
    local sockets
    if command -v ss >/dev/null; then sockets=$(ss -ltn)
    elif command -v netstat >/dev/null; then sockets=$(netstat -ltn)
    else die 'Instale iproute (ss) ou net-tools (netstat) para verificar a porta do listener.'; fi
    listener_address="(ADDRESS=(PROTOCOL=TCP)(HOST=127.0.0.1)(PORT=$port))"
    reuse_listener=false
    if awk -v port="$port" '$4 ~ (":" port "$") {found=1} END {exit !found}' <<< "$sockets"; then
        "$ORACLE_HOME/bin/lsnrctl" status "$listener_address" ||
            die "Porta $port ocupada, mas nao responde como listener Oracle local. Use --port."
        reuse_listener=true
    fi
}

build_parameters() {
    parallel_max=$((cpu_count * 2))
    ((parallel_max <= 16)) || parallel_max=16
    init_pairs=("db_name=ORCL" "sga_target=${sga_mb}M" "sga_max_size=${sga_mb}M"
        "pga_aggregate_target=${pga_mb}M" 'processes=300' 'open_cursors=300'
        "parallel_max_servers=$parallel_max" "db_create_file_dest=$data_dir"
        "db_recovery_file_dest=$recovery_dir" "db_recovery_file_dest_size=${fra_mb}M"
        "audit_file_dest=$work_dir/adump" "local_listener=$listener_address")
    if ((major >= 11)); then
        init_pairs+=('memory_target=0' 'memory_max_target=0' "diagnostic_dest=$ORACLE_BASE")
    else
        init_pairs+=("background_dump_dest=$work_dir/bdump" "user_dump_dest=$work_dir/udump" "core_dump_dest=$work_dir/cdump")
    fi
    [[ $cdb != true ]] || init_pairs+=('enable_pluggable_database=true')
    init_csv=$(IFS=,; printf '%s' "${init_pairs[*]}")
}

write_init() {
    local pair key value
    printf '# ORCL: plano calculado; o DBCA aplica estes valores via initParams.\n'
    printf '# Depois da criacao, init.ora sera exportado do SPFILE efetivo.\n'
    for pair in "${init_pairs[@]}"; do
        key=${pair%%=*}; value=${pair#*=}
        printf "*.%s='%s'\n" "$key" "$value"
    done
}

write_response() {
    # Password values contain only alphanumerics. They never appear in argv.
    if ((major < 12 || (major == 12 && minor == 1))); then
        local schema="$major.$minor.0"
        ((major != 10)) || schema=10.0.0
        cat <<EOF
[GENERAL]
RESPONSEFILE_VERSION = "$schema"
OPERATION_TYPE = "createDatabase"
[CREATEDATABASE]
GDBNAME = "ORCL"
SID = "ORCL"
TEMPLATENAME = "General_Purpose.dbc"
SYSPASSWORD = "$sys_password"
SYSTEMPASSWORD = "$system_password"
DATAFILEDESTINATION = "$data_dir"
RECOVERYAREADESTINATION = "$recovery_dir"
STORAGETYPE = "FS"
CHARACTERSET = "AL32UTF8"
NATIONALCHARACTERSET = "AL16UTF16"
EMCONFIGURATION = "NONE"
SAMPLESCHEMA = FALSE
INITPARAMS = "$init_csv"
EOF
        if ((major >= 11)); then printf 'AUTOMATICMEMORYMANAGEMENT = "FALSE"\nTOTALMEMORY = "%s"\n' "$memory_mb"; fi
        if [[ $cdb == true ]]; then
            printf 'CREATEASCONTAINERDATABASE = "true"\nNUMBEROFPDBS = 1\nPDBNAME = "ORCLPDB1"\nPDBADMINPASSWORD = "%s"\n' "$pdb_password"
        elif ((major >= 12)); then printf 'CREATEASCONTAINERDATABASE = "false"\n'; fi
        [[ $reuse_listener == true ]] || printf 'LISTENERS = "ORCL_LISTENER"\n'
    else
        local schema="$major.0.0"
        ((major != 12)) || schema=12.2.0
        cat <<EOF
responseFileVersion=/oracle/assistants/rspfmt_dbca_response_schema_v$schema
gdbName=ORCL
sid=ORCL
databaseConfigType=SI
templateName=General_Purpose.dbc
sysPassword=$sys_password
systemPassword=$system_password
storageType=FS
datafileDestination=$data_dir
recoveryAreaDestination=$recovery_dir
characterSet=AL32UTF8
nationalCharacterSet=AL16UTF16
emConfiguration=NONE
sampleSchema=false
automaticMemoryManagement=false
totalMemory=$memory_mb
initParams=$init_csv
createAsContainerDatabase=$cdb
EOF
        if [[ $cdb == true ]]; then
            printf 'numberOfPDBs=1\npdbName=ORCLPDB1\npdbAdminPassword=%s\n' "$pdb_password"
        fi
        [[ $reuse_listener == true ]] || printf 'listeners=ORCL_LISTENER\n'
    fi
    return 0
}

random_password() {
    local random
    random=$(od -An -N12 -tx1 /dev/urandom | tr -d ' \n') || die 'Falha ao ler /dev/urandom.'
    [[ $random =~ ^[a-f0-9]{24}$ ]] || die 'Gerador aleatorio nao retornou os bytes esperados.'
    printf 'Ora9%s' "$random"
}

cleanup() {
    local result=$?
    trap - EXIT ERR
    set +e
    [[ -z ${response_file:-} ]] || rm -f -- "$response_file"
    if ((result != 0)) && [[ -n ${work_dir_created:-} ]]; then
        printf 'Criacao interrompida. Arquivos preservados em %s; consulte os logs privados.\n' "$work_dir" >&2
        printf 'Nao execute novamente sem diagnosticar os artefatos parciais. Nenhum banco foi apagado.\n' >&2
    fi
    printf '[%s] FIM: codigo=%s; ultima_etapa=%s\n' "$(date -u '+%Y-%m-%dT%H:%M:%SZ')" "$result" "${debug_stage:-desconhecida}" >&2
    exit "$result"
}

provision() {
    stage 'reserva dos diretorios e lock'
    # A per-home flock covers simultaneous invocations using different data roots.
    exec 9>"$dbs_dir/.ashv-create-ORCL.lock"
    flock -n 9 || die 'Outra criacao de ORCL esta em andamento neste Oracle Home.'
    check_existing
    mkdir -p -- "$data_root"
    mkdir -- "$instance_root" || die "Nao foi possivel reservar $instance_root"
    mkdir -p -- "$work_dir" "$data_dir" "$recovery_dir" "$work_dir/adump" "$work_dir/bdump" "$work_dir/udump" "$work_dir/cdump"
    work_dir_created=true
    trap cleanup EXIT
    trap 'exit 130' INT
    trap 'exit 143' TERM
    # Avoid a login.sql from the invocation directory during local SYSDBA checks.
    cd -- "$work_dir"
    write_init > "$work_dir/initORCL.planned.ora"
    write_init > "$work_dir/init.ora"
    {
        printf 'ORACLE_SID=ORCL\nORACLE_HOME=%s\nORACLE_BASE=%s\n' "$ORACLE_HOME" "$ORACLE_BASE"
        printf 'version=%s\ncdb=%s\ncpu_count=%s\n' "$version" "$cdb" "$cpu_count"
        printf 'total_mb=%s\navailable_mb=%s\nsga_mb=%s\npga_mb=%s\ndisk_free_mb=%s\n' "$total_mb" "$available_mb" "$sga_mb" "$pga_mb" "$disk_mb"
    } > "$work_dir/plan.txt"
    sys_password=$(random_password); system_password=$(random_password); pdb_password=$(random_password)
    {
        printf 'SYS_PASSWORD=%s\nSYSTEM_PASSWORD=%s\n' "$sys_password" "$system_password"
        [[ $cdb != true ]] || printf 'PDBADMIN_PASSWORD=%s\n' "$pdb_password"
    } > "$work_dir/credentials.env"
    response_file=$work_dir/dbca.rsp
    write_response > "$response_file"
    unset sys_password system_password pdb_password

    if [[ $reuse_listener == false ]]; then
        mkdir -- "$work_dir/network"
        export TNS_ADMIN=$work_dir/network
        cat > "$TNS_ADMIN/listener.ora" <<EOF
ORCL_LISTENER = (DESCRIPTION_LIST=(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=0.0.0.0)(PORT=$port))))
EOF
        run_logged "$work_dir/listener.log" 'iniciar listener' "$ORACLE_HOME/bin/lsnrctl" start ORCL_LISTENER
    fi
    {
        printf 'export ORACLE_SID=ORCL\nexport ORACLE_HOME=%q\nexport ORACLE_BASE=%q\n' "$ORACLE_HOME" "$ORACLE_BASE"
        [[ -z ${TNS_ADMIN:-} ]] || printf 'export TNS_ADMIN=%q\n' "$TNS_ADMIN"
        # Expand these variables when the generated environment is sourced.
        # shellcheck disable=SC2016
        printf 'export PATH="$ORACLE_HOME/bin:$PATH"\nexport LD_LIBRARY_PATH="$ORACLE_HOME/lib${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"\nunset TWO_TASK LOCAL ORACLE_PDB_SID\n'
    } > "$work_dir/orcl.env"
    info "Criando ORCL com DBCA $version. Pode levar varios minutos. Log: $work_dir/dbca.log"
    run_logged "$work_dir/dbca.log" 'DBCA criar ORCL' "$ORACLE_HOME/bin/dbca" -silent -createDatabase -responseFile "$response_file"
    rm -f -- "$response_file"
    response_file=''
    cat > "$work_dir/verify.sql" <<EOF
whenever oserror exit failure
whenever sqlerror exit failure
set echo off feedback off heading off pagesize 0 linesize 200 trimspool on
create pfile='$work_dir/init.ora' from spfile;
alter system register;
select 'ASHV_ORCL_READY|' || name || '|' || open_mode from v\$database;
select 'ASHV_INSTANCE_READY|' || instance_name || '|' || status from v\$instance;
EOF
    if [[ $cdb == true ]]; then
        cat >> "$work_dir/verify.sql" <<'EOF'
alter pluggable database ORCLPDB1 save state;
select 'ASHV_PDB_READY|' || name || '|' || open_mode from v$pdbs where name='ORCLPDB1';
EOF
    fi
    printf 'exit success\n' >> "$work_dir/verify.sql"
    run_logged "$work_dir/verify.log" 'validacao SQL e exportacao PFILE' "$ORACLE_HOME/bin/sqlplus" -L -s / as sysdba @"$work_dir/verify.sql"
    grep -qx 'ASHV_ORCL_READY|ORCL|READ WRITE' "$work_dir/verify.log" || die 'Banco nao confirmado como ORCL READ WRITE.'
    grep -qx 'ASHV_INSTANCE_READY|ORCL|OPEN' "$work_dir/verify.log" || die 'Instancia ORCL nao confirmada como OPEN.'
    [[ $cdb != true ]] || grep -qx 'ASHV_PDB_READY|ORCLPDB1|READ WRITE' "$work_dir/verify.log" || die 'PDB nao confirmada como READ WRITE.'
    [[ -s $work_dir/init.ora ]] || die 'PFILE nao foi exportado.'
    printf 'complete\n' > "$work_dir/SUCCESS"
    info "ORCL criado e aberto. init.ora efetivo: $work_dir/init.ora"
    info "Credenciais privadas (modo 600): $work_dir/credentials.env"
    info "Ambiente: source $work_dir/orcl.env"
    if [[ $cdb == true ]]; then info "Servico para aplicacoes: ORCLPDB1, porta $port"
    else info "Servico para aplicacoes: ORCL, porta $port"; fi
    info 'O script nao configura encaminhamento de portas Vagrant nem inicializacao automatica no boot.'
}

main() {
    stage 'argumentos e ferramentas locais'
    ((BASH_VERSINFO[0] >= 4)) || die 'Bash 4 ou superior necessario.'
    local -a original_args=("$@")
    local option owner script_path base_config tool
    home_option='' base_option='' data_option='' memory_option='' port=1521 dry_run=false non_cdb=false
    while (($#)); do
        option=$1; shift
        case $option in
            --help|-h) usage; return ;;
            --dry-run) dry_run=true ;;
            --non-cdb) non_cdb=true ;;
            --oracle-home|--oracle-base|--data-dir|--memory-mb|--port)
                (($#)) || die "Valor ausente para $option"
                case $option in
                    --oracle-home) home_option=$1 ;; --oracle-base) base_option=$1 ;;
                    --data-dir) data_option=$1 ;; --memory-mb) memory_option=$1 ;; --port) port=$1 ;;
                esac
                shift ;;
            *) die "Opcao desconhecida: $option" ;;
        esac
    done
    [[ $(uname -s) == Linux ]] || die 'Execute dentro da VM Linux, nao no Windows/Git Bash.'
    if [[ ! $port =~ ^[1-9][0-9]{0,4}$ ]] || ((port < 1024 || port > 65535)); then
        die 'Porta deve estar entre 1024 e 65535.'
    fi
    for tool in awk sed df stat ps readlink find flock od tr grep; do command -v "$tool" >/dev/null || die "Comando necessario: $tool"; done
    stage 'descoberta Oracle Home'
    discover_home
    info "ORACLE_HOME=$ORACLE_HOME"
    safe_path "$ORACLE_HOME"
    owner=$(stat -c %U -- "$ORACLE_HOME/bin/oracle")
    [[ $owner != root && $owner != UNKNOWN ]] || die 'O binario oracle deve pertencer ao usuario de instalacao, nao a root.'
    if (( $(id -u) == 0 )); then
        stage "troca de usuario para $owner (o log externo inclui o log do filho)"
        command -v runuser >/dev/null || die 'runuser ausente. Execute como usuario Oracle.'
        script_path=$(readlink -f -- "${BASH_SOURCE[0]}")
        local handoff_result=0
        runuser -u "$owner" -- bash "$script_path" "${original_args[@]}" --oracle-home "$ORACLE_HOME" || handoff_result=$?
        return "$handoff_result"
    fi
    [[ $(id -un) == "$owner" ]] || die "Execute como $owner: sudo -iu $owner bash /caminho/create-orcl.sh"
    export ORACLE_HOME ORACLE_SID=ORCL
    export PATH="$ORACLE_HOME/bin:$PATH" LC_ALL=C
    export LD_LIBRARY_PATH="$ORACLE_HOME/lib${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"
    unset TWO_TASK LOCAL ORACLE_PDB_SID SQLPATH
    stage 'Oracle Base e diretorio de parametros'
    if [[ -n $base_option ]]; then ORACLE_BASE=$base_option
    elif [[ -x $ORACLE_HOME/bin/orabase ]]; then ORACLE_BASE=$("$ORACLE_HOME/bin/orabase")
    elif [[ $ORACLE_HOME == */product/* ]]; then ORACLE_BASE=${ORACLE_HOME%%/product/*}
    else die 'ORACLE_BASE nao identificado; use --oracle-base.'; fi
    safe_path "$ORACLE_BASE"
    export ORACLE_BASE
    info "ORACLE_BASE=$ORACLE_BASE"
    [[ -d $ORACLE_BASE && -w $ORACLE_BASE ]] || die "ORACLE_BASE deve existir e ser gravavel: $ORACLE_BASE"
    dbs_dir=$ORACLE_HOME/dbs
    if [[ -x $ORACLE_HOME/bin/orabaseconfig ]]; then
        base_config=$("$ORACLE_HOME/bin/orabaseconfig")
        safe_path "$base_config"
        dbs_dir=$base_config/dbs
    fi
    [[ -d $dbs_dir && -w $dbs_dir ]] || die "Diretorio de parametros nao gravavel: $dbs_dir"
    [[ -r $ORACLE_HOME/assistants/dbca/templates/General_Purpose.dbc ]] || die 'Template General_Purpose.dbc ausente; instalacao incompleta ou edicao nao contemplada.'
    [[ -x $ORACLE_HOME/bin/lsnrctl ]] || die 'lsnrctl ausente.'
    info "dbs_dir=$dbs_dir"
    stage 'versao e arquitetura'
    detect_version
    stage 'memoria e CPU'
    read_memory
    info "RAM total=$total_mb MiB; disponivel=$available_mb MiB; swap livre=$swap_mb MiB"
    plan_memory
    cpu_count=$(getconf _NPROCESSORS_ONLN)
    [[ $cpu_count =~ ^[1-9][0-9]*$ ]] || die 'Quantidade de CPUs nao identificada.'
    stage 'selecao de disco'
    select_storage
    stage 'espaco temporario'
    filesystem_info /tmp
    info "/tmp: filesystem=$fs_type; livre=$free_mb MiB; inodes=$free_inodes"
    ((free_mb >= 1024 && free_inodes >= 1024)) || die '/tmp precisa de pelo menos 1 GiB livre e 1024 inodes.'
    stage 'verificacao de instancia e artefatos existentes'
    check_existing
    stage 'verificacao do listener'
    inspect_listener
    stage 'plano de criacao'
    build_parameters
    info "Oracle $version | usuario $owner | ORACLE_HOME=$ORACLE_HOME"
    info "RAM total/livre: $total_mb/$available_mb MiB | swap livre: $swap_mb MiB | CPUs: $cpu_count"
    info "SGA: $sga_mb MiB | PGA alvo: $pga_mb MiB | CDB: $cdb"
    info "Destino: $instance_root | disco livre: $disk_mb MiB | FRA: $fra_mb MiB"
    info "Listener TCP $port | reutilizar: $reuse_listener"
    if [[ $dry_run == true ]]; then write_init; info 'DRY RUN: nenhum banco criado.'; return; fi
    provision
}

if [[ ${BASH_SOURCE[0]} == "$0" ]]; then debug_run "$@"; fi
