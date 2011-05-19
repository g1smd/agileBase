update abstractfield set printoutsetting ='NAME_AND_VALUE';
update abstractfield set printoutsetting ='VALUE_ONLY' where internalfieldname in (SELECT internalfieldname FROM filefielddefn);
update abstractfield set printoutsetting ='VALUE_ONLY' where internalfieldname in (SELECT internalfieldname FROM textfielddefn where useslookupdirect = false);
