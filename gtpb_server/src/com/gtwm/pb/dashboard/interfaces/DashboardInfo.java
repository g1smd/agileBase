package com.gtwm.pb.dashboard.interfaces;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.json.JSONException;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryDataInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryInfo;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.ObjectNotFoundException;

/**
 * Represents the dashboard for a company
 */
public interface DashboardInfo {
	
	/**
	 * Return list of exceptions found by looking through all reports in the company
	 */
	public SortedSet<DashboardOutlierInfo> getDashboardOutliers();
	
	public Set<DashboardTrendOutlierInfo> getDashboardTrendOutliers();
	
	/**
	 * Get report summary charts that agileBase automatically selects
	 */
	public SortedSet<DashboardReportSummaryInfo> getSuggestedReportSummaries();
	
	/**
	 * Return a grid object that can be used to lay out charts in the dashboard
	 * @param containerWidth
	 *            The pixel width of the space available for this grid
	 * @param widthUnit
	 *            The pixel width of individual blocks to go into the grid
	 * @param heightUnit
	 *            The pixel height of individual blocks to go into the grid
	 */
	public DashboardGridInfo getLayoutGrid(int containerWidth, int widthUnit, int heightUnit);
}
