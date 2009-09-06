-- routine to be run via OS scheduler at a frequency chosen by administrator, e.g. once a month
-- to prune log tables

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
