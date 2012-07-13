package com.gtwm.pb.dashboard.interfaces;

import java.util.List;

import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.util.Enumerations.Period;

/**
 * Extension of the DashboardException idea. Represents a chart over time that
 * contains a recent outlier value
 */
public interface DashboardTrendOutlierInfo {

	public BaseReportInfo getReport();

	public ReportFieldInfo getXAxisField();

	public ReportFieldInfo getYAxisField();

	/**
	 * Return what the x-axis is measured in, e.g. days, weeks, months. In
	 * actual fact the axis represents age, e.g. days ago, weeks
	 * ago, months ago or years ago
	 */
	public Period getXAxisPeriod();
	
	/**
	 * Return the data - one value per x-axis period from oldest to newest
	 */
	public List<Double> getData();

}
