BEGIN;
--used by portalBase when generating treemap output for report view statistics

create or replace view dbint_recent_report_counts as
SELECT company, report, count(*) as recent_count
FROM dbint_log_report_view
WHERE age(app_timestamp) < '30 days'::interval
GROUP BY company, report;

--don't include the most recent day's count in the average
--so that new reports will still be able to have a percentage increase or decrease
create or replace view dbint_report_average_counts as
SELECT company, report, count(*)::double precision / (GREATEST(date_part('epoch'::text, age(min(dbint_log_report_view.app_timestamp))), 2592000::double precision) / 2592000::double precision) AS average_count
FROM dbint_log_report_view
WHERE age(app_timestamp) > '1 day'::interval
GROUP BY dbint_log_report_view.company, dbint_log_report_view.report;

create or replace view dbint_report_view_stats AS
select rac.company, rac.report, rac.average_count, (((coalesce(rrc.recent_count,0) - rac.average_count) // coalesce(rrc.recent_count,0)) * 100) as percentage_increase
FROM dbint_report_average_counts rac LEFT JOIN dbint_recent_report_counts rrc
ON rac.company = rrc.company and rac.report = rrc.report;

create table dbint_report_view_stats_materialized(
company varchar(1000),
report varchar(1000),
average_count double precision,
percentage_increase double precision);

create index drvsm_company_idx on dbint_report_view_stats_materialized(company);
create index drvsm_report_idx on dbint_report_view_stats_materialized(report);

COMMIT;