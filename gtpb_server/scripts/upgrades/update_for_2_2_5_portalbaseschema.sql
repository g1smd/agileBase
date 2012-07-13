BEGIN;
UPDATE textfielddefn SET useslookupdirect = useslookup;
UPDATE textfielddefn SET contentsizedirect = contentsize;
ALTER TABLE textfielddefn DROP COLUMN useslookup;
ALTER TABLE textfielddefn DROP COLUMN contentsize;
COMMIT;