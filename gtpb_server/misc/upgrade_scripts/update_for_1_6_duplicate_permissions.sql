-- Run this script until the final SELECT query returns no results

-- remove duplicate user privileges
CREATE TABLE duplicate_user_object_privileges AS
   SELECT max(usergeneralprivilege.id) AS id
     FROM appuser INNER JOIN usergeneralprivilege ON appuser.internalusername = usergeneralprivilege.user_internalusername
                   LEFT JOIN userobjectprivilege ON usergeneralprivilege.id = userobjectprivilege.id
                   LEFT JOIN tabledefn ON tabledefn.internaltablename = userobjectprivilege.table_internaltablename
    GROUP BY appuser.username, usergeneralprivilege.privilegetype, tabledefn.tablename
   HAVING count(appuser.username) > 1
    ORDER BY appuser.username;

DELETE
  FROM authenticator_usergeneralprivilege
 WHERE userprivilegesdirect_id IN (select id from duplicate_user_object_privileges);

DELETE
  FROM userobjectprivilege
 WHERE id IN (select id from duplicate_user_object_privileges);

DELETE
  FROM usergeneralprivilege
 WHERE id IN (select id from duplicate_user_object_privileges);

DROP TABLE duplicate_user_object_privileges;

-- remove duplicate role privileges
CREATE TABLE duplicate_role_object_privileges AS
   SELECT max(rolegeneralprivilege.id) AS id
     FROM approle INNER JOIN rolegeneralprivilege ON approle.internalrolename = rolegeneralprivilege.role_internalrolename
                   LEFT JOIN roleobjectprivilege ON rolegeneralprivilege.id = roleobjectprivilege.id
                   LEFT JOIN tabledefn ON tabledefn.internaltablename = roleobjectprivilege.table_internaltablename
    GROUP BY approle.rolename, rolegeneralprivilege.privilegetype, tabledefn.tablename
   HAVING count(approle.rolename) > 1
    ORDER BY approle.rolename;

DELETE
  FROM authenticator_rolegeneralprivilege
 WHERE roleprivilegesdirect_id IN (select id from duplicate_role_object_privileges);

DELETE
  FROM roleobjectprivilege
 WHERE id IN (select id from duplicate_role_object_privileges);

DELETE
  FROM rolegeneralprivilege
 WHERE id IN (select id from duplicate_role_object_privileges);

DROP TABLE duplicate_role_object_privileges;

-- select any remaining duplicate user/role privileges
SELECT max(usergeneralprivilege.id) AS ID
  FROM appuser INNER JOIN usergeneralprivilege ON appuser.internalusername = usergeneralprivilege.user_internalusername
                LEFT JOIN userobjectprivilege ON usergeneralprivilege.id = userobjectprivilege.id
                LEFT JOIN tabledefn ON tabledefn.internaltablename = userobjectprivilege.table_internaltablename
 GROUP BY appuser.username, usergeneralprivilege.privilegetype, tabledefn.tablename
HAVING count(appuser.username) > 1
UNION
SELECT max(rolegeneralprivilege.id) AS ID
  FROM approle INNER JOIN rolegeneralprivilege ON approle.internalrolename = rolegeneralprivilege.role_internalrolename
                LEFT JOIN roleobjectprivilege ON rolegeneralprivilege.id = roleobjectprivilege.id
                LEFT JOIN tabledefn ON tabledefn.internaltablename = roleobjectprivilege.table_internaltablename
 GROUP BY approle.rolename, rolegeneralprivilege.privilegetype, tabledefn.tablename
HAVING count(approle.rolename) > 1;