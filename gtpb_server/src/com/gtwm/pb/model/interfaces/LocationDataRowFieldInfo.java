package com.gtwm.pb.model.interfaces;

/**
 * A report field value for a postcode field, that also stores latitude and longitude
 */
public interface LocationDataRowFieldInfo extends DataRowFieldInfo {

	public Double getLatitude();
	
	public Double getLongitude();
}
