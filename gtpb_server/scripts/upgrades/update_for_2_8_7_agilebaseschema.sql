alter table reportsummarydefn add column rangedirection bool;
alter table reportsummarydefn add column rangepercent integer;
update reportsummarydefn set rangedirection = true;
update reportsummarydefn set rangepercent = 100;
alter table reportsummarydefn alter column rangedirection set not null;
alter table reportsummarydefn alter column rangepercent set not null;
