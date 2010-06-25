package com.gtwm.pb.dashboard;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.json.JSONException;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.dashboard.interfaces.DashboardGridInfo;
import com.gtwm.pb.dashboard.interfaces.DashboardInfo;
import com.gtwm.pb.dashboard.interfaces.DashboardOutlierInfo;
import com.gtwm.pb.dashboard.interfaces.DashboardReportSummaryInfo;
import com.gtwm.pb.dashboard.interfaces.DashboardTrendOutlierInfo;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryDataInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryInfo;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.Enumerations.UserType;

public class Dashboard implements DashboardInfo {

	public Map<ReportSummaryInfo, ReportSummaryDataInfo> getCompanyReportSummaries() {
		// TODO Auto-generated method stub
		return null;
	}

	public SortedSet<DashboardOutlierInfo> getDashboardOutliers() {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<DashboardTrendOutlierInfo> getDashboardTrendOutliers() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Integer> getDataChangesPerWeek() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getDataChangesPercentageChange() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<Integer> getLoginsPerWeek() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getLoginsPercentageChange() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Map<UserType, Integer> getRecentLoginCounts() {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getRecentReportModificationDescriptions() {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getRecentTableModificationDescriptions() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Integer> getReportViewsPerWeek() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getReportViewsPercentageChange() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getTreeMapJSON() throws ObjectNotFoundException, DisallowedException,
			SQLException, JSONException, CodingErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<ReportSummaryInfo, ReportSummaryDataInfo> getUserReportSummaries(AppUserInfo user) {
		// TODO Auto-generated method stub
		return null;
	}

	public SortedSet<DashboardReportSummaryInfo> getSuggestedReportSummaries() {
		// TODO Auto-generated method stub
		return null;
	}

	public DashboardGridInfo getLayoutGrid(int containerWidth, int widthUnit, int heightUnit) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getHeadlineNotices() {
		// TODO Auto-generated method stub
		return null;
	}

}
