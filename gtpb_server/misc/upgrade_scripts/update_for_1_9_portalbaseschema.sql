update module set section = '' where section is null;
update module set colour = regexp_replace(colour,'\.gif','');