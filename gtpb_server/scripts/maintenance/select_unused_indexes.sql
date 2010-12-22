select indexrelname from pg_stat_user_indexes where idx_scan=0 and position('_pkey' in indexrelname) = 0 and position('_key' in indexrelname) = 0 and position('dbint' in relname) = 0;
