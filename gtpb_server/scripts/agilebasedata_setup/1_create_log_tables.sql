-- agileBase will log to these tables
-- and make the info available in various forms to managers and administrators
-- e.g. the management info treemap
BEGIN;

CREATE TABLE dbint_log_report_view (
    app_timestamp timestamp without time zone,
    company character varying(1000),
    app_user character varying(1000),
    report character varying(1000),
    details character varying(100000),
    log_entry_id serial not null
);

CREATE INDEX report_view_company ON dbint_log_report_view USING btree (company);
CREATE INDEX report_view_report ON dbint_log_report_view USING btree (report);
CREATE INDEX report_view_timestamp ON dbint_log_report_view USING btree (app_timestamp);

CREATE TABLE dbint_log_data_change (
    app_timestamp timestamp without time zone,
    company character varying(1000),
    app_user character varying(1000),
    app_table character varying(1000),
    app_action character varying(255),
    row_id integer,
    saved_data character varying(100000),
    log_entry_id serial not null
);

CREATE INDEX data_change_app_table ON dbint_log_data_change USING btree (app_table);
CREATE INDEX data_change_company ON dbint_log_data_change USING btree (company);

CREATE TABLE dbint_log_login (
    app_timestamp timestamp without time zone,
    company character varying(1000),
    app_user character varying(1000),
    ip_address character varying(1000),
    details character varying(100000)
);

CREATE INDEX login_company ON dbint_log_login USING btree (company);

CREATE TABLE dbint_log_report_schema_change (
    app_timestamp timestamp without time zone,
    company character varying(1000),
    app_user character varying(1000),
    report character varying(1000),
    app_action character varying(1000),
    details character varying(100000)
);

CREATE INDEX report_schema_change_company ON dbint_log_report_schema_change USING btree (company);

CREATE TABLE dbint_log_table_schema_change (
    app_timestamp timestamp without time zone,
    company character varying(1000),
    app_user character varying(1000),
    app_table character varying(1000),
    app_action character varying(255),
    details character varying(100000)
); 

CREATE INDEX table_schema_change_app_table ON dbint_log_table_schema_change USING btree (app_table);
CREATE INDEX table_schema_change_company ON dbint_log_table_schema_change USING btree (company);

CREATE TABLE dbint_comments (
    created timestamp without time zone,
    text character varying(100000),
    author character varying(10000),
    internalfieldname character varying(1000),
    rowid integer
);

CREATE INDEX comments_idx ON dbint_comments USING btree (internalfieldname, rowid);

CREATE VIEW dbint_backup_commands AS
SELECT (((('pg_dump -d agilebasedata -U postgres -f /var/local/backup/'::text || distinct_tables.app_table::text) || '_'::text) || regexp_replace(now()::text, '\\W'::text, '-'::text, 'g'::text)) || '.sql -t '::text) || distinct_tables.app_table::text
   FROM ( SELECT DISTINCT dbint_log_data_change.app_table
           FROM dbint_log_data_change
          WHERE dbint_log_data_change.app_timestamp > (now() - '3 days'::interval)) distinct_tables;
          


COMMIT;