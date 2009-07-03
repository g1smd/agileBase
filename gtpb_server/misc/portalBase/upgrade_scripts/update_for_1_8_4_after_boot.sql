-- portalbaseschema

UPDATE abstractfield SET fieldnamedirect='Wiki page [Auto]' where fieldnamedirect='Wiki page';
UPDATE abstractfield SET fieldnamedirect='Creation time [Auto]' where fieldnamedirect='GTPB Date created';
UPDATE abstractfield SET fieldnamedirect='Created by [Auto]' where fieldnamedirect='GTPB Created by';
UPDATE abstractfield SET fieldnamedirect='Last modified [Auto]' where fieldnamedirect='GTPB Last modified';
UPDATE abstractfield SET fieldnamedirect='Modified by [Auto]' where fieldnamedirect='GTPB Modified by';

update tabledefn set recordslockable=false;
alter table tabledefn drop column deleted;
alter table tabledefn drop column audittable_internaltablename;

-- portalbasedata

ALTER TABLE dbint_log_report_view DROP COLUMN report_group;
ALTER TABLE dbint_log_report_schema_change DROP COLUMN report_group;
DELETE FROM dbint_log_login;
DELETE FROM dbint_log_report_view;
DELETE FROM dbint_log_data_change;
DELETE FROM dbint_log_report_schema_change;
DELETE FROM dbint_log_table_schema_change;