package com.gtwm.pb.model.interfaces;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.json.JSONException;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.Enumerations.UserType;

/**
 * Represents the dashboard for a company
 */
public interface DashboardInfo {
	
	/**
	 * Return how much login counts have changed recently, e.g. a 25% increase. Negative numbers mean a decrease
	 * @return last week's total against the past four weeks'
	 */
	public int getLoginsPercentageChange();
	
	public int getReportViewsPercentageChange();
	
	public int getDataChangesPercentageChange();
	
	/**
	 * @return e.g. "Oliver Kohll modified the human resources -> staff details summary"
	 */
	public Set<String> getRecentReportModificationDescriptions();
	
	/**
	 * @return e.g. "Oliver Kohll added fields to the CRM -> organisations table"
	 */
	public Set<String> getRecentTableModificationDescriptions();
	
	/**
	 * Get no. logins in the past week for each user type
	 */
	public Map<UserType, Integer> getRecentLoginCounts();
	
	/**
	 * Return counts of the number of logins per week, from 52 weeks ago to the present
	 */
	public List<Integer> getLoginsPerWeek();
	
	public List<Integer> getReportViewsPerWeek();
	
	public List<Integer> getDataChangesPerWeek();
	
	public SortedSet<DashboardOutlierInfo> getDashboardOutliers();
	
	public Set<DashboardTrendOutlierInfo> getDashboardTrendOutliers();
	
	public Map<ReportSummaryInfo, ReportSummaryDataInfo> getCompanyReportSummaries();
	
	public Map<ReportSummaryInfo, ReportSummaryDataInfo> getUserReportSummaries(AppUserInfo user);
	
	/**
	 * Return data that can be used to build a treemap of report view
	 * statistics. Reports are grouped by module
	 * 
	 * @return JSON formatted data suitable for use with the Infoviz toolkit, OR
	 *         a plain String error message
	 */
	public String getTreeMapJSON() throws ObjectNotFoundException, DisallowedException,
			SQLException, JSONException, CodingErrorException;
}
