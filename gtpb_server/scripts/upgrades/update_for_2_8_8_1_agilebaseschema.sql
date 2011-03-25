alter table relationfielddefn add column defaulttonull boolean;
update relationfielddefn set defaulttonull = false;
alter table relationfielddefn alter column defaulttonull set not null;
