package com.gtwm.pb.model.manageSchema.fields.options;

public class DateFieldOptions extends BasicFieldOptions {
	
	public boolean isDefaultToNow() {
		return defaultToNow;
	}

	public void setDefaultToNow(boolean defaultToNow) {
		this.defaultToNow = defaultToNow;
	}

	public Integer getMaxAgeYears() {
		return maxAgeYears;
	}

	public void setMaxAgeYears(Integer maxAgeYears) {
		this.maxAgeYears = maxAgeYears;
	}

	public Integer getMinAgeYears() {
		return minAgeYears;
	}

	public void setMinAgeYears(Integer minAgeYears) {
		this.minAgeYears = minAgeYears;
	}

	public Integer getDateResolution() {
		return dateResolution;
	}

	public void setDateResolution(Integer dateResolution) {
		this.dateResolution = dateResolution;
	}

	private boolean defaultToNow = false;
	
	private Integer maxAgeYears = null;
	
	private Integer minAgeYears = null;
	
	private Integer dateResolution = null;
}
