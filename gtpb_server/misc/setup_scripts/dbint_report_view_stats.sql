--used by portalBase when generating treemap output for report view statistics

create view dbint_recent_report_counts AS
select company, report, count(*) as recent_count
FROM dbint_log_report_view
where age(app_timestamp) < '30 days'::interval
group by company, report;

create view dbint_report_average_counts AS
select company, report, (count(*) / (extract(epoch from age(min(app_timestamp))) / 2592000)) as average_count
from dbint_log_report_view
group by company, report;

create view dbint_report_view_stats AS
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
