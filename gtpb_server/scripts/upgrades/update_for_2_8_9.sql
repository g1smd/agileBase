alter table tabledefn add column tableformpublic boolean;
update tabledefn set tableformpublic = false;
alter table tabledefn alter column tableformpublic set not null;
