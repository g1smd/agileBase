begin;

-- remove obsolete info that isn't logged any more
update dbint_log_data_change
set saved_data = regexp_replace(saved_data,E', Last modified \\[Auto\\]\\=.*','}');

update dbint_log_data_change
set saved_data = regexp_replace(saved_data,E'^New data \\=','');

-- add a unique log entry id and set it so that the largest number represents the newest entry
alter table dbint_log_data_change add column log_entry_id serial;

update dbint_log_data_change set log_entry_id=sub.rn
from 
(select log_entry_id lei, row_number() over (partition by 1 order by app_timestamp) rn from dbint_log_data_change) as sub
where log_entry_id = sub.lei;

commit;
