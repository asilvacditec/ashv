-- ASH Viewer experimental repository v1. Run in a dedicated, existing database.
-- SQL Server 2012+. Execute batches with SSMS or sqlcmd (GO support).
:ON ERROR EXIT
SET NOCOUNT ON;
IF CONVERT(int, PARSENAME(CONVERT(varchar(30), SERVERPROPERTY('ProductVersion')), 4)) < 11
    THROW 51000, 'SQL Server 2012 or later is required.', 1;
IF DB_NAME() IN ('master', 'model', 'msdb', 'tempdb')
    THROW 51000, 'Select a dedicated monitoring database first.', 1;
IF SCHEMA_ID('ashv') IS NULL EXEC('CREATE SCHEMA ashv AUTHORIZATION dbo');
GO
IF OBJECT_ID('ashv.Configuration') IS NULL
BEGIN
    CREATE TABLE ashv.Configuration (
        id int NOT NULL PRIMARY KEY CHECK (id = 1),
        schema_version int NOT NULL CHECK (schema_version = 1),
        retention_days int NOT NULL CHECK (retention_days BETWEEN 1 AND 3650),
        capture_sql_text bit NOT NULL
    );
    INSERT ashv.Configuration VALUES (1, 1, 30, 0);
END;
IF OBJECT_ID('ashv.Sample') IS NULL
BEGIN
    CREATE TABLE ashv.Sample (
        sample_id bigint IDENTITY PRIMARY KEY,
        sampled_at_utc datetime2(3) NOT NULL,
        active_requests int NOT NULL DEFAULT 0
    );
    CREATE INDEX IX_Sample_Time ON ashv.Sample(sampled_at_utc, sample_id);
END;
IF OBJECT_ID('ashv.RequestSample') IS NULL
BEGIN
    CREATE TABLE ashv.RequestSample (
        sample_id bigint NOT NULL REFERENCES ashv.Sample(sample_id) ON DELETE CASCADE,
        session_id smallint NOT NULL,
        login_time datetime NOT NULL,
        request_id int NOT NULL,
        request_start_time datetime NOT NULL,
        database_name nvarchar(128) NULL,
        login_name nvarchar(128) NULL,
        host_name nvarchar(128) NULL,
        program_name nvarchar(128) NULL,
        status nvarchar(30) NOT NULL,
        wait_type nvarchar(60) NULL,
        blocking_session_id smallint NULL,
        cpu_time_ms bigint NOT NULL,
        elapsed_time_ms bigint NOT NULL,
        logical_reads bigint NOT NULL,
        query_hash binary(8) NULL,
        sql_text nvarchar(2000) NULL,
        PRIMARY KEY (sample_id, session_id, request_id)
    );
END;
IF DATABASE_PRINCIPAL_ID('ashv_reader') IS NULL CREATE ROLE ashv_reader;
GRANT SELECT ON SCHEMA::ashv TO ashv_reader;
GO
IF OBJECT_ID('ashv.Collect', 'P') IS NULL EXEC('CREATE PROCEDURE ashv.Collect AS RETURN');
GO
ALTER PROCEDURE ashv.Collect
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    -- Fail visibly rather than recording a misleading empty sample.
    DECLARE @major int = CONVERT(int, PARSENAME(CONVERT(varchar(30), SERVERPROPERTY('ProductVersion')), 4));
    IF COALESCE(IS_SRVROLEMEMBER('sysadmin'), 0) <> 1 AND
       ((@major < 16 AND COALESCE(HAS_PERMS_BY_NAME(NULL, NULL, 'VIEW SERVER STATE'), 0) <> 1)
       OR (@major >= 16 AND COALESCE(HAS_PERMS_BY_NAME(NULL, NULL, 'VIEW SERVER PERFORMANCE STATE'), 0) <> 1))
        THROW 51001, 'Collector needs server DMV permissions.', 1;
    DECLARE @lock int, @sample bigint, @text bit;
    BEGIN TRY
        BEGIN TRANSACTION;
        EXEC @lock = sys.sp_getapplock @Resource = 'ashv.Collect',
            @LockMode = 'Exclusive', @LockOwner = 'Transaction', @LockTimeout = 0;
        IF @lock < 0 THROW 51002, 'Another collector is running.', 1;
        SELECT @text = capture_sql_text FROM ashv.Configuration WHERE id = 1;
        IF @text IS NULL THROW 51003, 'Missing configuration.', 1;
        INSERT ashv.Sample(sampled_at_utc) VALUES (SYSUTCDATETIME());
        SET @sample = SCOPE_IDENTITY();
        INSERT ashv.RequestSample (sample_id, session_id, login_time, request_id,
            request_start_time, database_name, login_name, host_name, program_name,
            status, wait_type, blocking_session_id, cpu_time_ms, elapsed_time_ms,
            logical_reads, query_hash, sql_text)
        SELECT @sample, r.session_id, s.login_time, r.request_id, r.start_time,
            DB_NAME(r.database_id), s.login_name, s.host_name, s.program_name,
            r.status, r.wait_type, r.blocking_session_id,
            r.cpu_time, r.total_elapsed_time, r.logical_reads, r.query_hash,
            CASE WHEN @text = 1 THEN LEFT(t.text, 2000) END
        FROM sys.dm_exec_requests AS r
        JOIN sys.dm_exec_sessions AS s ON s.session_id = r.session_id
        OUTER APPLY sys.dm_exec_sql_text(CASE WHEN @text = 1 THEN r.sql_handle END) AS t
        WHERE s.is_user_process = 1 AND r.session_id <> @@SPID;
        UPDATE ashv.Sample SET active_requests = @@ROWCOUNT WHERE sample_id = @sample;
        COMMIT;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO
IF OBJECT_ID('ashv.Purge', 'P') IS NULL EXEC('CREATE PROCEDURE ashv.Purge AS RETURN');
GO
ALTER PROCEDURE ashv.Purge
    @batch_size int = 100, @max_batches int = 100
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF @batch_size IS NULL OR @max_batches IS NULL
       OR @batch_size NOT BETWEEN 1 AND 1000 OR @max_batches NOT BETWEEN 1 AND 1000
        THROW 51004, 'Invalid purge limits.', 1;
    DECLARE @days int, @cutoff datetime2(3), @n int = 0, @deleted int = 1;
    SELECT @days = retention_days FROM ashv.Configuration WHERE id = 1;
    IF @days IS NULL THROW 51003, 'Missing configuration.', 1;
    SET @cutoff = DATEADD(day, -@days, SYSUTCDATETIME());
    WHILE @n < @max_batches AND @deleted > 0
    BEGIN
        ;WITH old_samples AS (
            SELECT TOP (@batch_size) * FROM ashv.Sample
            WHERE sampled_at_utc < @cutoff ORDER BY sampled_at_utc, sample_id
        )
        DELETE FROM old_samples;
        SET @deleted = @@ROWCOUNT;
        SET @n += 1;
    END;
END;
GO
