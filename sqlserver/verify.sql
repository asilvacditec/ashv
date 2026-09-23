-- Run only in an isolated test repository after install.sql. Changes roll back.
:ON ERROR EXIT
SET NOCOUNT ON;
SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRANSACTION;
    UPDATE ashv.Configuration SET retention_days = 30 WHERE id = 1;
    DECLARE @before bigint = COALESCE((SELECT MAX(sample_id) FROM ashv.Sample), 0);
    EXEC ashv.Collect;
    IF NOT EXISTS (SELECT 1 FROM ashv.Sample WHERE sample_id > @before)
        THROW 51100, 'Collector did not write a heartbeat.', 1;
    IF EXISTS (SELECT 1 FROM ashv.Sample s WHERE sample_id > @before
        AND active_requests <> (SELECT COUNT(*) FROM ashv.RequestSample r WHERE r.sample_id = s.sample_id))
        THROW 51101, 'Request count mismatch.', 1;
    DECLARE @old bigint, @recent bigint;
    INSERT ashv.Sample(sampled_at_utc) VALUES (DATEADD(day, -31, SYSUTCDATETIME()));
    SET @old = SCOPE_IDENTITY();
    INSERT ashv.RequestSample(sample_id, session_id, login_time, request_id, request_start_time,
        status, cpu_time_ms, elapsed_time_ms, logical_reads)
    VALUES (@old, 52, GETDATE(), 0, GETDATE(), 'running', 0, 0, 0);
    INSERT ashv.Sample(sampled_at_utc) VALUES (DATEADD(day, -29, SYSUTCDATETIME()));
    SET @recent = SCOPE_IDENTITY();
    EXEC ashv.Purge @batch_size = 1000, @max_batches = 1000;
    IF EXISTS (SELECT 1 FROM ashv.Sample WHERE sample_id = @old)
       OR EXISTS (SELECT 1 FROM ashv.RequestSample WHERE sample_id = @old)
        THROW 51102, 'Expired sample or dependent request remains.', 1;
    IF NOT EXISTS (SELECT 1 FROM ashv.Sample WHERE sample_id = @recent)
        THROW 51103, 'Purge removed a retained sample.', 1;
    ROLLBACK;
    PRINT 'PASS: collection heartbeat, request count, retention and cascade (rolled back).';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;
END CATCH;
