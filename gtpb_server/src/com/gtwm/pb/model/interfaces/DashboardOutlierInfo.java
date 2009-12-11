package com.gtwm.pb.model.interfaces;

import java.util.Calendar;

/**
 * A single value exception, i.e. outlier value
 */
public interface DashboardOutlierInfo {

	/**
	 * Return the report that the outlier occurs in
	 */
	public BaseReportInfo getReport();
	
	public int getRowID();
	
	public ReportFieldInfo getReportField();
	
	public double getValue();
	
	/**
	 * Return standard deviation of the values in the field the outlier is in, for comparison
	 */
	public double getStdDev();
	
	/**
	 * Return mean of the values in the field the outlier is in, for comparison
	 */
	public double getMean();
	
	/**
	 * Return the date the row was last edited
	 */
	public Calendar getModificationDate();
}
