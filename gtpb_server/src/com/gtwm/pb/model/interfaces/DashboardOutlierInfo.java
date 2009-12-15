package com.gtwm.pb.model.interfaces;

import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.DateValue;

/**
 * A single value exception, i.e. outlier value
 */
public interface DashboardOutlierInfo {

	/**
	 * Return the report that the outlier occurs in
	 */
	public BaseReportInfo getReport();
	
	public int getRowID();
	
	public BaseField getField();
	
	/**
	 * Return the value itself
	 */
	public DataRowFieldInfo getDataRowField();
		
	/**
	 * Return the date the row was last edited
	 */
	public DateValue getModificationDate();
}
