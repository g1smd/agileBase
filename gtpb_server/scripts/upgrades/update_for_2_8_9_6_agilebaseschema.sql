begin;

alter table integerfielddefn add column storescurrency boolean;
update integerfielddefn set storescurrency = false;
alter table integerfielddefn alter column storescurrency set not null;

alter table decimalfielddefn add column storescurrency boolean;
update decimalfielddefn set storescurrency = false;
alter table decimalfielddefn alter column storescurrency set not null;

commit;