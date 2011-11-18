BEGIN;
--used by agileBase when generating treemap output for report view statistics

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
WHERE age(app_timestamp) > '1 day'::interval AND age(app_timestamp) < '6 months'::interval
GROUP BY dbint_log_report_view.company, dbint_log_report_view.report;

create or replace view dbint_report_view_stats AS
select rac.company, rac.report, rac.average_count, (((coalesce(rrc.recent_count,0) - rac.average_count) // coalesce(rrc.recent_count,0)) * 100) as percentage_increase
FROM dbint_report_average_counts rac LEFT JOIN dbint_recent_report_counts rrc
ON rac.company = rrc.company and rac.report = rrc.report;

CREATE TABLE dbint_report_view_stats_materialized (
    company character varying(1000),
    report character varying(1000),
    average_count double precision,
    percentage_increase double precision
);

CREATE INDEX drvsm_company_idx ON dbint_report_view_stats_materialized USING btree (company);
CREATE INDEX drvsm_report_index ON dbint_report_view_stats_materialized USING btree (report);

CREATE VIEW activity_check AS
    SELECT dbint_log_report_view.app_timestamp, dbint_log_report_view.company, dbint_log_report_view.app_user,
dbint_log_report_view.report, dbint_log_report_view.details, dbint_log_report_view.log_entry_id
FROM dbint_log_report_view ORDER BY dbint_log_report_view.app_timestamp DESC LIMIT 100;

COMMIT;