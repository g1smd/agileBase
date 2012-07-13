BEGIN;
UPDATE appuser SET usertype='LIMITED' WHERE usertype='EXTERNAL';
UPDATE appuser SET usertype='EXECUTIVE_DASHBOARD' WHERE usertype='EXECUTIVE';
UPDATE appuser SET usertype='FULL' WHERE usertype='OPERATIONAL';
UPDATE appuser SET usertype='FULL' WHERE usertype='DATA_INPUT';
UPDATE appuser SET usertype='FULL' WHERE usertype='MANAGERIAL';
COMMIT;