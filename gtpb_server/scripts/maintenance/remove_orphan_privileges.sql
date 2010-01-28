-- Workaround fix for a hibernate-related bug that causes orphan privileges
-- Not sure yet what causes this
-- This script should be run on agilebaseschema database

DELETE FROM userobjectprivilege
  WHERE NOT (id IN ( SELECT authenticator_usergeneralprivilege.userprivilegesdirect_id
           FROM authenticator_usergeneralprivilege));
           
DELETE FROM usergeneralprivilege
  WHERE NOT (usergeneralprivilege.id IN ( SELECT authenticator_usergeneralprivilege.userprivilegesdirect_id
           FROM authenticator_usergeneralprivilege));
           
DELETE FROM roleobjectprivilege
  WHERE NOT (id IN ( SELECT authenticator_rolegeneralprivilege.roleprivilegesdirect_id
           FROM authenticator_rolegeneralprivilege));
           
DELETE FROM rolegeneralprivilege
  WHERE NOT (rolegeneralprivilege.id IN ( SELECT authenticator_rolegeneralprivilege.roleprivilegesdirect_id
           FROM authenticator_rolegeneralprivilege));