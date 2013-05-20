package com.gtwm.pb.dashboard.interfaces;

import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.DataRowFieldInfo;
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

	/**
	 * Identifer of the row containing the outlying value
	 */
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

	public OutlierType getOutlierType();

	/**
	 * If this outlier represents multiple outliers from the same table & field, return the number, otherwise return 1
	 */
	public int getOutlierCount();

	/**
	 * An outlier can be a case of either
	 *
	 * - a field is marked as mandatory but no value is entered
	 *
	 * - a field value is significantly outside the norm, based on std. dev.
	 */
	public enum OutlierType {
		MANDATED_VIOLATION, DISTRIBUTION_OUTLIER
	}
}
