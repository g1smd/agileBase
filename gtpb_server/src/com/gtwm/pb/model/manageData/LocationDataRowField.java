package com.gtwm.pb.model.manageData;

import com.gtwm.pb.model.interfaces.LocationDataRowFieldInfo;

public class LocationDataRowField implements LocationDataRowFieldInfo {

	public LocationDataRowField(String postCode, Double latitude, Double longitude) {
		this.keyValue = postCode;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public String getKeyValue() {
		return this.keyValue;
	}

	public String getDisplayValue() {
		return this.keyValue;
	}

	public String getStandardDevHexColour() {
		return DataRowField.NULL_COLOR;
	}

	public double getNumberOfStdDevsFromMean() {
		return 0d;
	}

	public Double getLatitude() {
		return this.latitude;
	}

	public Double getLongitude() {
		return this.longitude;
	}
	
	private final String keyValue;

	private final Double latitude;
	
	private final Double longitude;
}
