-- Routine to be run via OS scheduler at a frequency chosen by administrator
-- Once a day is recommended so the statistics are materialized regularly

-- remove old log entries

DELETE FROM dbint_log_data_change WHERE age(app_timestamp) > '1 year'::interval;
DELETE FROM dbint_log_login WHERE age(app_timestamp) > '1 year'::interval;
DELETE FROM dbint_log_report_schema_change WHERE age(app_timestamp) > '1 year'::interval;
DELETE FROM dbint_log_report_view WHERE age(app_timestamp) > '1 year'::interval;
DELETE FROM dbint_log_table_schema_change WHERE age(app_timestamp) > '1 year'::interval;

-- remove close-duplicate entries from report view log to save space
-- i.e. when a user is typing a filter, frequently every keypress gets logged
-- but we only really need to store the final filter value
-- so this removes the intermediate steps.
-- For example, the rows produced by
-- SELECT app_timestamp, app_user, details FROM dbint_log_report_view ORDER BY app_timestamp DESC;
----
-- 03/07/2009 17:12:25.487 | cathy          | Session filters = {name=diane sm}, row limit = 50
-- 03/07/2009 17:12:25.384 | cathy          | Session filters = {name=diane s}, row limit = 50
-- 03/07/2009 17:12:25.262 | cathy          | Session filters = {name=diane }, row limit = 50
-- 03/07/2009 17:12:25.174 | cathy          | Session filters = {name=diane}, row limit = 50
-- 03/07/2009 17:12:25.068 | cathy          | Session filters = {name=dian}, row limit = 50
-- 03/07/2009 17:12:24.995 | cathy          | Session filters = {name=dia}, row limit = 50
-- 03/07/2009 17:12:24.857 | cathy          | Session filters = {name=di}, row limit = 50
-- 03/07/2009 17:12:24.799 | cathy          | Session filters = {name=d}, row limit = 50
-- would become just
-- 03/07/2009 17:12:25.487 | cathy          | Session filters = {name=diane sm}, row limit = 50

DELETE FROM dbint_log_report_view WHERE log_entry_id IN (
SELECT rv_inner.log_entry_id
FROM dbint_log_report_view rv_outer INNER JOIN dbint_log_report_view rv_inner
  ON rv_inner.log_entry_id + 1 = rv_outer.log_entry_id
  AND rv_inner.app_user = rv_outer.app_user
  AND rv_inner.report = rv_outer.report
  AND rv_inner.details != '' AND rv_outer.details != ''
  AND position(regexp_replace(rv_inner.details,'\}.*$','') in regexp_replace(rv_outer.details,'\}.*$','')) = 1
ORDER BY rv_outer.log_entry_id
);

-- Similarly, remove close-dups in the other direction,
-- i.e. when a user is deleting a filter character by character.

DELETE FROM dbint_log_report_view WHERE log_entry_id IN (
SELECT rv_inner.log_entry_id
FROM dbint_log_report_view rv_outer INNER JOIN dbint_log_report_view rv_inner
  ON rv_inner.log_entry_id - 1 = rv_outer.log_entry_id
  AND rv_inner.app_user = rv_outer.app_user
  AND rv_inner.report = rv_outer.report
  AND rv_inner.details != '' AND rv_outer.details != ''
  AND position(regexp_replace(rv_inner.details,'\}.*$','') in regexp_replace(rv_outer.details,'\}.*$','')) = 1
ORDER BY rv_outer.log_entry_id
);

-- Do the same sort of thing for the data_change log

DELETE FROM dbint_log_data_change WHERE log_entry_id IN (
SELECT dc_inner.log_entry_id
FROM dbint_log_data_change dc_outer INNER JOIN dbint_log_data_change dc_inner
  ON dc_inner.log_entry_id + 1 = dc_outer.log_entry_id
  AND dc_inner.app_user = dc_outer.app_user
  AND dc_inner.app_table = dc_outer.app_table
  AND dc_inner.app_action = 'update record'
  AND dc_outer.app_action = 'update record'
  AND dc_inner.saved_data != '{}' AND dc_outer.saved_data != '{}'
  AND position(regexp_replace(dc_inner.saved_data,'\}$','') in dc_outer.saved_data) = 1
ORDER BY dc_outer.log_entry_id
);

-- materialize statistics report

DELETE FROM dbint_report_view_stats_materialized;
INSERT INTO dbint_report_view_stats_materialized(company, report, average_count, percentage_increase)
  SELECT company, report, average_count, percentage_increase FROM dbint_report_view_stats;
