-- Historical session-sampling collector; validate on the target version/edition.
-- Apply grants-base.sql separately.
GRANT SELECT ON SYS.V_$SESSION TO ASH_READER;
GRANT SELECT ON SYS.V_$SESSTAT TO ASH_READER;
GRANT SELECT ON SYS.V_$MYSTAT TO ASH_READER;
