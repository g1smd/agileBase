CREATE VIEW dbint_user_roles AS
SELECT appuser.username, 'gtpbeveryone' AS rolename
FROM appuser
UNION 
SELECT appuser.username, approle.rolename
FROM appuser
  JOIN approle_appuser ON appuser.internalusername::text = approle_appuser.usersdirect_internalusername::text
  JOIN approle ON approle_appuser.approle_internalrolename::text = approle.internalrolename::text;