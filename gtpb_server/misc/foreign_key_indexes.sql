--These are potentially useful for the portalbaseschema database
--which is a Hibernate database. Hibernate doesn't create its own indexes
--In tests so far, adding these indexes hasn't resulted in a performance benefit
--so they are only kept for potential use in certain setups

-- It is not necessary to run this script to use portalBase

create index abstractfield_fkb71bb5d8c9795fed on abstractfield(tablecontainingfield_internaltablename);
create index abstractreportfield_fk8fbdf28451ebcbcb on abstractreportfield(reportfieldisfromdirect_internalreportname);
create index abstractreportfield_fk8fbdf284be3a91f1 on abstractreportfield(parentreport_internalreportname);
create index basereportdefn_fkb7c42a2e4c2aa233 on basereportdefn(parenttable_internaltablename);
create index joinclause_fkb5a42b593da44dc6 on joinclause(rightreportfielddirect_id);
create index joinclause_fkb5a42b596021050d on joinclause(lefttablefielddirect_internalfieldname);
create index joinclause_fkb5a42b59b2b80831 on joinclause(leftreportfielddirect_id);
create index joinclause_fkb5a42b59ce6561e2 on joinclause(righttablefielddirect_internalfieldname);
create index reportfielddefn_fk5615222f7a76b5ba on reportfielddefn(basefield_internalfieldname);
create index reportsort_fke9efe1124e02eed1 on reportsort(sortreportfield_id);
create index rolegeneralprivilege_fk4976df5f8632a396 on rolegeneralprivilege(role_internalrolename);
create index roleobjectprivilege_fk7e23eebcbbb5ca1d on roleobjectprivilege(table_internaltablename);
create index usergeneralprivilege_fkefa900b4a647d10b on usergeneralprivilege(user_internalusername);
create index userobjectprivilege_fk392dcec7bbb5ca1d on userobjectprivilege(table_internaltablename);