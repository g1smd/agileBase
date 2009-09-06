CREATE OR REPLACE VIEW dbint_user_roles AS 
 SELECT appuser.username, 'gtpbeveryone' AS rolename
   FROM appuser
 UNION
 SELECT appuser.username, approle.rolename
   FROM appuser INNER JOIN approle_appuser ON appuser.internalusername = approle_appuser.usersdirect_internalusername
                INNER JOIN approle ON approle_appuser.approle_internalrolename = approle.internalrolename;
ALTER TABLE dbint_user_roles OWNER TO gtpb;

-- Remove the TreeMap related persisted data
ALTER TABLE basereportdefn DROP COLUMN reporttreemapdirect_id;
DROP TABLE reporttreemapdefn;