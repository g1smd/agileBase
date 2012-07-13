CREATE TABLE dbint_comments(
  created timestamp,
  text varchar(100000),
  author varchar(10000),
  internalfieldname varchar(1000),
  rowid integer
);

create index comments_idx on dbint_comments(internalfieldname, rowid);