BEGIN;
-- portalBase will log to these tables
-- and make the info available in various forms to managers and administrators
-- e.g. the management info treemap

CREATE TABLE dbint_log_login(
app_timestamp timestamp,
company varchar(1000),
app_user varchar(1000),
ip_address varchar(1000)
);

CREATE INDEX login_company ON dbint_log_login(company);

CREATE TABLE dbint_log_data_change(
app_timestamp timestamp,
company varchar(1000),
app_user varchar(1000),
app_table varchar(1000),
app_action varchar(255),
row_id integer,
saved_data varchar(100000)
);

CREATE INDEX data_change_app_table ON dbint_log_data_change(app_table);
CREATE INDEX data_change_company ON dbint_log_data_change(company);

CREATE TABLE dbint_log_report_schema_change(
app_timestamp timestamp,
company varchar(1000),
app_user varchar(1000),
report varchar(1000),
app_action varchar(1000),
details varchar(100000)
);

CREATE INDEX report_schema_change_company ON dbint_log_report_schema_change(company);

CREATE TABLE dbint_log_table_schema_change(
app_timestamp timestamp,
company varchar(1000),
app_user varchar(1000),
app_table varchar(1000),
app_action varchar(255),
details varchar(100000)
);

CREATE TABLE dbint_log_report_view(
app_timestamp timestamp,
company varchar(1000),
app_user varchar(1000),
report varchar(1000),
details varchar(100000),
log_entry_id serial
);

CREATE INDEX report_view_company ON dbint_log_report_view(company);
CREATE INDEX report_view_report ON dbint_log_report_view(report);
CREATE INDEX report_view_timestamp ON dbint_log_report_view(app_timestamp);

COMMIT;