BEGIN;
-- This routine creates an alterantive division operator
-- that doesn't throw an error on a divide by zero
-- but rather returns null

CREATE OR REPLACE FUNCTION gtpb_divide(integer, integer) RETURNS integer
AS 'SELECT $1 / NULLIF($2,0);'
LANGUAGE SQL
IMMUTABLE
RETURNS NULL ON NULL INPUT;

CREATE OR REPLACE FUNCTION gtpb_divide(double precision, double precision) RETURNS double precision
AS 'SELECT $1 / NULLIF($2,0);'
LANGUAGE SQL
IMMUTABLE
RETURNS NULL ON NULL INPUT;

CREATE OR REPLACE FUNCTION gtpb_divide(double precision, integer) RETURNS double precision
AS 'SELECT $1 / NULLIF($2,0);'
LANGUAGE SQL
IMMUTABLE
RETURNS NULL ON NULL INPUT;

CREATE OR REPLACE FUNCTION gtpb_divide(integer, double precision) RETURNS double precision
AS 'SELECT $1 / NULLIF($2,0);'
LANGUAGE SQL
IMMUTABLE
RETURNS NULL ON NULL INPUT;

CREATE OPERATOR // (
PROCEDURE = gtpb_divide,
LEFTARG = integer,
RIGHTARG = integer
);

CREATE OPERATOR // (
PROCEDURE = gtpb_divide,
LEFTARG = double precision,
RIGHTARG = double precision
);

CREATE OPERATOR // (
PROCEDURE = gtpb_divide,
LEFTARG = double precision,
RIGHTARG = integer
);

CREATE OPERATOR // (
PROCEDURE = gtpb_divide,
LEFTARG = integer,
RIGHTARG = double precision
);

COMMIT;
