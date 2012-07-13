DROP TABLE dbint_user_roles;
DROP TABLE dbint_roles;
DROP TABLE dbint_users;

create table dbint_log_data_change(
app_timestamp timestamp,
company varchar(1000),
app_user varchar(1000),
app_table varchar(1000),
app_action varchar(255),
row_id integer
);

create table dbint_log_login(
app_timestamp timestamp,
company varchar(1000),
app_user varchar(1000),
ip_address varchar(1000)
);

create table dbint_log_report_schema_change(
app_timestamp timestamp,
company varchar(1000),
app_user varchar(1000),
report varchar(1000),
report_group varchar(1000),
app_action varchar(1000),
details varchar(100000)
);

create table dbint_log_table_schema_change(
app_timestamp timestamp,
company varchar(1000),
app_user varchar(1000),
app_table varchar(1000),
app_action varchar(255),
details varchar(100000)
);

create table dbint_log_report_view(
app_timestamp timestamp,
company varchar(1000),
app_user varchar(1000),
report varchar(1000),
report_group varchar(1000)
);