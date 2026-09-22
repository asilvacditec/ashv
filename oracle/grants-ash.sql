-- Only after the DBA confirms Diagnostics Pack entitlement for this environment.
-- Apply grants-base.sql separately.
GRANT SELECT ON SYS.V_$ACTIVE_SESSION_HISTORY TO ASH_READER;
