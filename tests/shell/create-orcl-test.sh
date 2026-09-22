#!/usr/bin/env bash
# Runs on Linux or Git Bash. No Oracle installation or database is contacted.
# Host-probe overrides below are called indirectly by the sourced main function.
# shellcheck disable=SC2329
set -Eeuo pipefail
repo_root=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.." && pwd -P)
mkdir -p "$repo_root/.tools"
TEST_ROOT=$(mktemp -d "$repo_root/.tools/orcl-tests.XXXXXX")
trap '[[ $TEST_ROOT == "$repo_root"/.tools/orcl-tests.* ]] && rm -rf -- "$TEST_ROOT"' EXIT
# shellcheck source=oracle/create-orcl.sh
source "$repo_root/oracle/create-orcl.sh"
fail() { printf 'FAIL: %s\n' "$*" >&2; exit 1; }
assert_contains() { grep -Fq -- "$2" "$1" || fail "Missing $2 in $1"; }
assert_absent() { [[ ! -e $1 ]] || fail "Unexpected file $1"; }

run_case() {
    local status
    set +e
    (set -e; "$1") > "$TEST_ROOT/$1.log" 2>&1
    status=$?
    set -e
    if ((status)); then cat "$TEST_ROOT/$1.log"; fail "$1"; fi
    printf 'PASS %s\n' "$1"
}

expect_failure() {
    local status
    set +e
    (set -e; "$@") > "$TEST_ROOT/expected-failure.log" 2>&1
    status=$?
    set -e
    ((status != 0)) || fail "Expected failure: $*"
}

fixture() {
    fixture_root=$(mktemp -d "$TEST_ROOT/case.XXXXXX")
    export FIXTURE_ROOT=$fixture_root
    export FIXTURE_VERSION=19.0.0.0.0 FIXTURE_DBCA_FAIL=false FIXTURE_SQL_FAIL=false
    ORACLE_HOME=$fixture_root/home
    ORACLE_BASE=$fixture_root/base
    mkdir -p "$ORACLE_HOME/bin" "$ORACLE_HOME/dbs" "$ORACLE_HOME/assistants/dbca/templates" "$ORACLE_BASE" "$fixture_root/data"
    printf 'fixture\n' > "$ORACLE_HOME/assistants/dbca/templates/General_Purpose.dbc"
    printf '#!/usr/bin/env bash\nexit 0\n' > "$ORACLE_HOME/bin/oracle"
    cat > "$ORACLE_HOME/bin/orabase" <<'STUB'
#!/usr/bin/env bash
printf '%s/base\n' "$FIXTURE_ROOT"
STUB
    cat > "$ORACLE_HOME/bin/sqlplus" <<'STUB'
#!/usr/bin/env bash
if [[ ${1:-} == -V ]]; then echo "SQL*Plus: Release $FIXTURE_VERSION - Production"; exit; fi
printf '%s\n' "$*" > "$FIXTURE_ROOT/sqlplus.args"
if [[ $FIXTURE_SQL_FAIL == true ]]; then echo 'ORA-01034: ORACLE not available'; exit 1; fi
sql_file=${!#}; sql_file=${sql_file#@}
pfile=$(sed -n "s/^create pfile='\([^']*\)'.*/\1/p" "$sql_file")
printf "*.db_name='ORCL'\n# Exported by fake SQLPlus\n" > "$pfile"
echo 'ASHV_ORCL_READY|ORCL|READ WRITE'
echo 'ASHV_INSTANCE_READY|ORCL|OPEN'
echo 'ASHV_PDB_READY|ORCLPDB1|READ WRITE'
STUB
    cat > "$ORACLE_HOME/bin/dbca" <<'STUB'
#!/usr/bin/env bash
printf '%s\n' "$*" > "$FIXTURE_ROOT/dbca.args"
rsp=${!#}
cp "$rsp" "$FIXTURE_ROOT/captured.rsp"
sed -n '/[Pp][Aa][Ss][Ss][Ww][Oo][Rr][Dd]/p' "$rsp"
[[ $FIXTURE_DBCA_FAIL != true ]] || exit 7
echo 'Database creation complete (fixture)'
STUB
    cat > "$ORACLE_HOME/bin/lsnrctl" <<'STUB'
#!/usr/bin/env bash
printf '%s\n' "$*" >> "$FIXTURE_ROOT/listener.args"
exit 0
STUB
    chmod +x "$ORACLE_HOME/bin/"*
    # Replace only host probes; real script discovery, guards, files, DBCA invocation,
    # cleanup and verification run against the private fixture.
    uname() { echo Linux; }
    id() { case $1 in -u) echo 1000 ;; -un) echo oracle ;; *) command id "$@" ;; esac; }
    stat() { if [[ ${1:-} == -c && ${2:-} == %U ]]; then echo oracle; else command stat "$@"; fi; }
    read_memory() { total_mb=8192; available_mb=6144; swap_mb=2048; }
    filesystem_info() { free_mb=65536; free_inodes=100000; fs_type=ext4; }
    getconf() { echo 2; }
    ps() { printf 'bash\n'; }
    ss() { printf 'State Recv-Q Send-Q Local_Address:Port Peer_Address:Port\n'; }
    flock() { return 0; }
}

test_memory_and_old_linux() {
    cat > "$TEST_ROOT/meminfo" <<'EOF'
MemTotal:       4194304 kB
MemFree:        1048576 kB
Buffers:         262144 kB
Cached:         1048576 kB
SwapFree:       2097152 kB
EOF
    read_memory "$TEST_ROOT/meminfo"
    [[ $total_mb == 4096 && $available_mb == 2304 && $swap_mb == 2048 ]] || fail 'Old-kernel memory fallback'
    major=11 cdb=false memory_option=''
    plan_memory
    [[ $memory_mb == 1280 && $sga_mb == 960 && $pga_mb == 320 ]] || fail 'Memory reserve not preserved'
    available_mb=512
    expect_failure plan_memory
}

test_dry_run() {
    fixture
    main --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data" --dry-run > "$fixture_root/output"
    assert_contains "$fixture_root/output" 'DRY RUN'
    assert_contains "$fixture_root/output" "*.sga_target='3072M'"
    assert_absent "$fixture_root/data/ORCL"
    assert_absent "$fixture_root/dbca.args"
    assert_absent "$ORACLE_HOME/dbs/.ashv-create-ORCL.lock"
}

test_modern_creation_and_secrets() {
    fixture
    main --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data" > "$fixture_root/output"
    local run=$fixture_root/data/ORCL/provision secret
    [[ -f $run/SUCCESS ]] || fail 'Missing success marker'
    assert_contains "$run/init.ora" 'Exported by fake SQLPlus'
    assert_contains "$fixture_root/captured.rsp" 'createAsContainerDatabase=true'
    assert_contains "$fixture_root/captured.rsp" 'pdbName=ORCLPDB1'
    assert_contains "$fixture_root/captured.rsp" 'memory_target=0'
    if grep -qi 'local_listener=' "$fixture_root/captured.rsp"; then fail 'Oracle Net descriptor passed through DBCA parser'; fi
    assert_contains "$run/verify.sql" "alter system set local_listener='(ADDRESS=(PROTOCOL=TCP)(HOST=127.0.0.1)(PORT=1521))' scope=both;"
    assert_contains "$fixture_root/dbca.args" '-silent -createDatabase -responseFile'
    assert_absent "$run/dbca.rsp"
    secret=$(sed -n 's/^SYS_PASSWORD=//p' "$run/credentials.env")
    [[ ${#secret} == 28 ]] || fail 'Password length'
    if grep -Fq "$secret" "$fixture_root/output" "$fixture_root/dbca.args" "$run/orcl.env"; then fail 'Password exposed'; fi
    if [[ $(command uname -s) == Linux ]]; then
        [[ $(command stat -c %a "$run/credentials.env") == 600 ]] || fail 'Credentials permissions'
    fi
    expect_failure main --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data"
    assert_contains "$TEST_ROOT/expected-failure.log" 'Artefato existente'
}

test_legacy_11g() {
    fixture
    export FIXTURE_VERSION=11.2.0.4.0
    main --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data" --port 1522 --memory-mb 2048 > "$fixture_root/output"
    assert_contains "$fixture_root/captured.rsp" 'RESPONSEFILE_VERSION = "11.2.0"'
    assert_contains "$fixture_root/captured.rsp" 'SID = "ORCL"'
    assert_contains "$fixture_root/captured.rsp" 'sga_target=1536,'
    assert_contains "$fixture_root/captured.rsp" 'sga_max_size=1610612736,'
    assert_contains "$fixture_root/captured.rsp" 'pga_aggregate_target=512,'
    assert_contains "$fixture_root/captured.rsp" 'db_recovery_file_dest_size=6553,'
    # Reproduce the per-field scaling observed in the real 11.2.0.4 trace.
    local params target maximum fra
    params=$(sed -n 's/^INITPARAMS = "\(.*\)"$/\1/p' "$fixture_root/captured.rsp" | tr ',' '\n')
    target=$(sed -n 's/^sga_target=//p' <<< "$params")
    maximum=$(sed -n 's/^sga_max_size=//p' <<< "$params")
    fra=$(sed -n 's/^db_recovery_file_dest_size=//p' <<< "$params")
    ((target * 1048576 == maximum && fra * 1048576 == 6871318528)) || fail 'Legacy DBCA scaling inflates memory or FRA'
    assert_contains "$fixture_root/captured.rsp" 'TOTALMEMORY = "2048"'
    assert_contains "$fixture_root/captured.rsp" 'memory_target=0'
    if grep -qi 'memory_max_target' "$fixture_root/captured.rsp" "$fixture_root/data/ORCL/provision/initORCL.planned.ora"; then
        fail '11g must not receive explicit MEMORY_MAX_TARGET=0 with positive SGA'
    fi
    assert_contains "$fixture_root/data/ORCL/provision/initORCL.planned.ora" "*.sga_target='1536M'"
    if grep -qi 'local_listener=' "$fixture_root/captured.rsp"; then fail '11g response contains nested Oracle Net descriptor'; fi
    local sql=$fixture_root/data/ORCL/provision/verify.sql
    assert_contains "$sql" "alter system set local_listener='(ADDRESS=(PROTOCOL=TCP)(HOST=127.0.0.1)(PORT=1522))' scope=both;"
    awk '/^alter system set local_listener=/ {configured=1} /^create pfile=/ {if (!configured) exit 1; exported=1} /^alter system register;/ {if (!exported) exit 1; registered=1} END {if (!registered) exit 1}' "$sql" || fail 'Listener must be set before PFILE export and registration'
    if grep -q 'PDB\|CREATEASCONTAINER' "$fixture_root/captured.rsp"; then fail 'Legacy PDB parameters'; fi
}

test_legacy_10g() {
    fixture
    export FIXTURE_VERSION=10.2.0.5.0
    main --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data" > "$fixture_root/output"
    assert_contains "$fixture_root/captured.rsp" 'RESPONSEFILE_VERSION = "10.0.0"'
    assert_contains "$fixture_root/captured.rsp" 'sga_target=3072,'
    assert_contains "$fixture_root/captured.rsp" 'sga_max_size=3221225472,'
    assert_contains "$fixture_root/captured.rsp" 'db_recovery_file_dest_size=6553,'
    if grep -q 'memory_target\|diagnostic_dest\|AUTOMATICMEMORY' "$fixture_root/captured.rsp"; then fail 'Unsupported 10g parameters'; fi
}

test_12c_modes() {
    fixture
    export FIXTURE_VERSION=12.1.0.2.0
    main --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data" > "$fixture_root/output"
    assert_contains "$fixture_root/captured.rsp" 'CREATEASCONTAINERDATABASE = "true"'
    fixture
    export FIXTURE_VERSION=12.2.0.1.0
    main --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data" --non-cdb > "$fixture_root/output"
    assert_contains "$fixture_root/captured.rsp" 'schema_v12.2.0'
    assert_contains "$fixture_root/captured.rsp" 'createAsContainerDatabase=false'
}

test_failed_dbca_preserves_state() {
    fixture
    export FIXTURE_DBCA_FAIL=true
    expect_failure main --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data"
    local run=$fixture_root/data/ORCL/provision
    assert_absent "$run/SUCCESS"
    assert_absent "$run/dbca.rsp"
    [[ -f $run/credentials.env && -f $run/dbca.log ]] || fail 'Failure artifacts lost'
    assert_absent "$fixture_root/sqlplus.args"
}

test_failed_verification() {
    fixture
    export FIXTURE_SQL_FAIL=true
    expect_failure main --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data"
    assert_absent "$fixture_root/data/ORCL/provision/SUCCESS"
    assert_contains "$fixture_root/data/ORCL/provision/verify.log" 'ORA-01034'
}

test_existing_database_and_lock() {
    fixture
    printf 'existing\n' > "$ORACLE_HOME/dbs/spfileORCL.ora"
    expect_failure main --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data"
    assert_absent "$fixture_root/dbca.args"
    assert_contains "$ORACLE_HOME/dbs/spfileORCL.ora" 'existing'
    fixture
    flock() { return 1; }
    expect_failure main --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data"
    assert_absent "$fixture_root/data/ORCL"
}

test_resource_and_version_guards() {
    fixture
    export FIXTURE_VERSION=21.0.0.0.0
    expect_failure main --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data" --non-cdb
    export FIXTURE_VERSION=19.0.0.0.0
    expect_failure main --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data" --memory-mb 90000
    filesystem_info() { free_mb=1024; free_inodes=10000; fs_type=ext4; }
    expect_failure main --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data"
    filesystem_info() { free_mb=65536; free_inodes=10000; fs_type=vboxsf; }
    expect_failure main --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data"
    assert_absent "$fixture_root/dbca.args"
}

test_listener_reuse() {
    fixture
    ss() { printf 'LISTEN 0 128 0.0.0.0:1521 0.0.0.0:*\n'; }
    main --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data" > "$fixture_root/output"
    assert_contains "$fixture_root/listener.args" 'status'
    if grep -q '^start' "$fixture_root/listener.args"; then fail 'Existing listener restarted'; fi
    assert_absent "$fixture_root/data/ORCL/provision/network"
    fixture
    ss() { printf 'LISTEN 0 128 0.0.0.0:1521 0.0.0.0:*\n'; }
    printf '#!/usr/bin/env bash\nexit 1\n' > "$ORACLE_HOME/bin/lsnrctl"
    expect_failure main --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data"
    assert_contains "$TEST_ROOT/expected-failure.log" 'Porta 1521 ocupada'
    assert_absent "$fixture_root/dbca.args"
}

test_input_and_running_instance_guards() {
    fixture
    expect_failure main --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data" --port 70000
    expect_failure main --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/../escape"
    export FIXTURE_VERSION=99.0.0.0.0
    expect_failure main --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data"
    export FIXTURE_VERSION=19.0.0.0.0
    ps() { printf 'ora_pmon_orcl\n'; }
    expect_failure main --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data"
    assert_absent "$fixture_root/dbca.args"
}

test_debug_logs() {
    fixture
    mktemp() {
        if [[ ${1:-} == /tmp/create-orcl-debug.XXXXXXXX.log ]]; then
            command mktemp "$fixture_root/debug.XXXXXXXX.log"
        else command mktemp "$@"; fi
    }
    expect_failure debug_run --port 70000
    local log
    log=$(find "$fixture_root" -name 'debug.*.log' -print -quit)
    assert_contains "$log" 'Porta deve estar'
    assert_contains "$log" 'FIM: codigo=1'
    fixture
    printf '#!/usr/bin/env bash\necho biblioteca-ausente >&2\nexit 23\n' > "$ORACLE_HOME/bin/orabase"
    expect_failure debug_run --oracle-home "$ORACLE_HOME" --dry-run
    log=$(find "$fixture_root" -name 'debug.*.log' -print -quit)
    assert_contains "$log" 'biblioteca-ausente'
    assert_contains "$log" 'ERRO inesperado: codigo=23 linha='
    assert_contains "$log" 'FIM: codigo=23'
    fixture
    export FIXTURE_DBCA_FAIL=true
    local status=0 secret
    set +e
    (set -e; debug_run --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data") > "$fixture_root/output" 2>&1
    status=$?
    set -e
    [[ $status == 7 ]] || fail 'DBCA exit status lost by debug pipeline'
    log=$(find "$fixture_root" -name 'debug.*.log' -print -quit)
    assert_contains "$log" 'Resultado DBCA criar ORCL: codigo=7'
    assert_contains "$log" '[SENHA_REMOVIDA]'
    assert_contains "$log" 'FIM: codigo=7'
    while IFS='=' read -r _key secret; do
        if grep -Fq "$secret" "$log" "$fixture_root/output"; then fail 'Debug leaked password'; fi
    done < "$fixture_root/data/ORCL/provision/credentials.env"
    assert_absent "$fixture_root/data/ORCL/provision/dbca.rsp"
    fixture
    debug_run --oracle-home "$ORACLE_HOME" --data-dir "$fixture_root/data" --dry-run > "$fixture_root/output"
    log=$(find "$fixture_root" -name 'debug.*.log' -print -quit)
    assert_contains "$log" 'DRY RUN'
    assert_contains "$log" 'FIM: codigo=0'
    assert_absent "$fixture_root/data/ORCL"
    fixture
    id() { case $1 in -u) echo 0 ;; -un) echo root ;; *) command id "$@" ;; esac; }
    runuser() { echo 'runuser simulado: acesso negado' >&2; return 42; }
    expect_failure debug_run --oracle-home "$ORACLE_HOME" --dry-run
    log=$(find "$fixture_root" -name 'debug.*.log' -print -quit)
    assert_contains "$log" 'troca de usuario para oracle'
    assert_contains "$log" 'runuser simulado: acesso negado'
    assert_contains "$log" 'FIM: codigo=42'
}

run_case test_memory_and_old_linux
run_case test_dry_run
run_case test_modern_creation_and_secrets
run_case test_legacy_11g
run_case test_legacy_10g
run_case test_12c_modes
run_case test_failed_dbca_preserves_state
run_case test_failed_verification
run_case test_existing_database_and_lock
run_case test_resource_and_version_guards
run_case test_listener_reuse
run_case test_input_and_running_instance_guards
run_case test_debug_logs
printf 'All 13 shell scenarios passed (Oracle commands simulated).\n'
