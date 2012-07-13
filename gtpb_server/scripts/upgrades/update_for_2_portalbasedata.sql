CREATE INDEX report_view_company ON dbint_log_report_view(company);
CREATE INDEX data_change_company ON dbint_log_data_change(company);
CREATE INDEX table_schema_change_company ON dbint_log_table_schema_change(company);
CREATE INDEX login_company ON dbint_log_login(company);
CREATE INDEX report_schema_change_company ON dbint_log_report_schema_change(company);
CREATE INDEX report_view_report ON dbint_log_report_view(report);
CREATE INDEX data_change_app_table ON dbint_log_data_change(app_table);
CREATE INDEX table_schema_change_app_table ON dbint_log_table_schema_change(app_table);