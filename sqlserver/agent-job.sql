-- SQL Server 2012+. Run in the monitoring database as a DBA with Agent access.
:ON ERROR EXIT
SET NOCOUNT ON;
DECLARE @database sysname = DB_NAME(), @job uniqueidentifier;
DECLARE @interval_minutes int = 1; -- Independent of retention_days.
IF OBJECT_ID('ashv.Collect', 'P') IS NULL OR OBJECT_ID('ashv.Purge', 'P') IS NULL
    THROW 51010, 'Run install.sql in this database first.', 1;
IF @interval_minutes NOT BETWEEN 1 AND 59
    THROW 51011, 'Interval must be 1..59 minutes.', 1;
IF LEN(@database) > 110
    THROW 51014, 'Monitoring database name must be at most 110 characters for job names.', 1;
DECLARE @collect sysname = N'ASHV Collect - ' + @database,
        @purge sysname = N'ASHV Purge - ' + @database;
IF EXISTS (SELECT 1 FROM msdb.dbo.sysjobs WHERE name IN (@collect, @purge))
    THROW 51012, 'Job already exists; edit it explicitly in SQL Server Agent.', 1;
BEGIN TRY
    BEGIN TRANSACTION;
    EXEC msdb.dbo.sp_add_job @job_name = @collect, @enabled = 1, @job_id = @job OUTPUT;
    EXEC msdb.dbo.sp_add_jobstep @job_id = @job, @step_name = N'Collect',
        @subsystem = N'TSQL', @database_name = @database,
        @command = N'EXEC ashv.Collect;', @on_success_action = 1, @on_fail_action = 2;
    EXEC msdb.dbo.sp_add_jobschedule @job_id = @job, @name = @collect,
        @freq_type = 4, @freq_interval = 1, @freq_subday_type = 4,
        @freq_subday_interval = @interval_minutes, @active_start_time = 0;
    EXEC msdb.dbo.sp_add_jobserver @job_id = @job;
    EXEC msdb.dbo.sp_add_job @job_name = @purge, @enabled = 1, @job_id = @job OUTPUT;
    EXEC msdb.dbo.sp_add_jobstep @job_id = @job, @step_name = N'Purge',
        @subsystem = N'TSQL', @database_name = @database,
        @command = N'EXEC ashv.Purge;', @on_success_action = 1, @on_fail_action = 2;
    EXEC msdb.dbo.sp_add_jobschedule @job_id = @job, @name = @purge,
        @freq_type = 4, @freq_interval = 1, @freq_subday_type = 8,
        @freq_subday_interval = 1, @active_start_time = 0;
    EXEC msdb.dbo.sp_add_jobserver @job_id = @job;
    COMMIT;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;
END CATCH;
