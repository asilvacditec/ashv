#!/usr/bin/env bash
# ASH Viewer lab tooling. Contribution: Aparecido Silva. GPL-3.0-or-later.
# Oracle 11gR2: direct DBCA invocation validated in the user's lab.
set +x
set -Eeuo pipefail
umask 077

: "${ORACLE_BASE:?ORACLE_BASE nao esta definida}"
: "${ORACLE_HOME:?ORACLE_HOME nao esta definida}"
: "${ORACLE_SID:?ORACLE_SID nao esta definida}"
export ORACLE_BASE ORACLE_HOME ORACLE_SID

DBCA="$ORACLE_HOME/bin/dbca"
SQLPLUS="$ORACLE_HOME/bin/sqlplus"
[[ -x "$DBCA" && -x "$SQLPLUS" ]] || {
    echo 'ERRO: DBCA ou SQL*Plus nao encontrado no ORACLE_HOME.' >&2
    exit 1
}

DATA_DIR="$ORACLE_BASE/oradata"
FRA_DIR="$ORACLE_BASE/fast_recovery_area"
mkdir -p -- "$DATA_DIR" "$FRA_DIR"

printf 'Oracle Database 11gR2 - DBCA Silent\nORACLE_BASE = %s\nORACLE_HOME = %s\nORACLE_SID = %s\nDatafiles = %s\nFRA = %s\nMemory = 1024 MB\n' \
    "$ORACLE_BASE" "$ORACLE_HOME" "$ORACLE_SID" "$DATA_DIR" "$FRA_DIR"

# Read silently to avoid storing passwords in the script or shell history.
read -r -s -p 'Senha SYS: ' sys_password
printf '\n'
read -r -s -p 'Senha SYSTEM: ' system_password
printf '\n'
[[ -n "$sys_password" && -n "$system_password" ]] || {
    echo 'ERRO: as senhas nao podem estar vazias.' >&2
    exit 1
}

"$DBCA" -silent -createDatabase \
    -templateName General_Purpose.dbc \
    -gdbname "$ORACLE_SID" \
    -sid "$ORACLE_SID" \
    -sysPassword "$sys_password" \
    -systemPassword "$system_password" \
    -emConfiguration NONE \
    -storageType FS \
    -datafileDestination "$DATA_DIR" \
    -recoveryAreaDestination "$FRA_DIR" \
    -characterSet AL32UTF8 \
    -nationalCharacterSet AL16UTF16 \
    -totalMemory 1024 \
    -databaseType MULTIPURPOSE
unset sys_password system_password

echo 'Validando database'
"$SQLPLUS" -L -s / as sysdba <<'SQL'
whenever oserror exit failure
whenever sqlerror exit failure
set pagesize 100
set linesize 200
column instance_name format a15
column version format a15
column status format a12
column name format a12
column open_mode format a15
column database_role format a20
select instance_name, version, status from v$instance;
select name, open_mode, database_role from v$database;
declare
    instance_status varchar2(20);
    database_mode varchar2(20);
begin
    select status into instance_status from v$instance;
    select open_mode into database_mode from v$database;
    if instance_status <> 'OPEN' or database_mode <> 'READ WRITE' then
        raise_application_error(-20001, 'Banco nao esta OPEN / READ WRITE');
    end if;
end;
/
exit success
SQL

printf 'Oracle %s READY\n' "$ORACLE_SID"
