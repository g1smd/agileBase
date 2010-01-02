package com.gtwm.pb.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.dashboard.Dashboard;
import com.gtwm.pb.dashboard.DashboardOutlier;
import com.gtwm.pb.dashboard.DashboardReportSummary;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.DashboardInfo;
import com.gtwm.pb.model.interfaces.DashboardOutlierInfo;
import com.gtwm.pb.model.interfaces.DashboardReportSummaryInfo;
import com.gtwm.pb.model.interfaces.DataManagementInfo;
import com.gtwm.pb.model.interfaces.DataRowFieldInfo;
import com.gtwm.pb.model.interfaces.DataRowInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryAggregateInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryDataInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryGroupingInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryInfo;
import com.gtwm.pb.model.interfaces.SimpleReportInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.BaseValue;
import com.gtwm.pb.model.interfaces.fields.DateValue;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.PortalBaseException;
import com.gtwm.pb.util.Enumerations.HiddenFields;

/**
 * Populates all company dashboards with data. In the auth package as we need
 * protected level access to the Authenticator object to do this. Ideally we
 * would be in the dashboard package
 */
public class DashboardPopulator implements Runnable {

	private DashboardPopulator() {
	}

	public DashboardPopulator(DatabaseInfo databaseDefn) {
		this.databaseDefn = databaseDefn;
	}

	public void run() {
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);
		logger.info("Starting dashboards population");
		long startTime = System.currentTimeMillis();
		Authenticator authenticator = (Authenticator) this.databaseDefn.getAuthManager()
				.getAuthenticator();
		try {
			// Generate the dashboard data
			Map<CompanyInfo, SortedSet<DashboardOutlierInfo>> dashboardOutliers = this
					.findDashboardOutliers();
			Map<CompanyInfo, SortedSet<DashboardReportSummaryInfo>> dashboardSummaries = this
					.findSuggestedSummaries();
			// Make dashboards from the data, one per company
			for (CompanyInfo company : authenticator.getCompanies()) {
				SortedSet<DashboardOutlierInfo> companyOutliers = dashboardOutliers.get(company);
				SortedSet<DashboardReportSummaryInfo> companySummaries = dashboardSummaries
						.get(company);
				DashboardInfo dashboard = new Dashboard(companyOutliers, companySummaries);
				company.setDashboard(dashboard);
			}
		} catch (InterruptedException iex) {
			logger.info("Dashboard population not completed due to interruption");
		} catch (PortalBaseException pbex) {
			logger.error("Error populating dashboards: " + pbex);
		}
		int duration = (int) ((System.currentTimeMillis() - startTime) / (1000 * 60));
		logger.info("Dashboards populated in " + duration + " minutes");
	}

	private Map<CompanyInfo, SortedSet<DashboardOutlierInfo>> findDashboardOutliers()
			throws InterruptedException {
		logger.debug("Finding dashboard outliers");
		Map<CompanyInfo, SortedSet<DashboardOutlierInfo>> companyOutliers = new HashMap<CompanyInfo, SortedSet<DashboardOutlierInfo>>();
		Authenticator authenticator = (Authenticator) this.databaseDefn.getAuthManager()
				.getAuthenticator();
		DataManagementInfo dataManagement = this.databaseDefn.getDataManagement();
		logger.debug("About to get companies");
		Set<CompanyInfo> companies = authenticator.getCompanies();
		logger.debug("Got companies " + companies);
		Set<TableInfo> allTables = getAllTables(companies);
		logger.debug("Got all tables " + allTables);
		Map<TableInfo, Set<Integer>> rowAlterations = this.getRecentRowAlterations(allTables);
		// Populate each company dashboard
		for (CompanyInfo company : companies) {
			logger.info("Finding outliers for " + company);
			Set<TableInfo> companyTables = company.getTables();
			SortedSet<DashboardOutlierInfo> dashboardOutliers = new TreeSet<DashboardOutlierInfo>();
			TABLESLOOP: for (TableInfo table : companyTables) {
				Set<Integer> rowIds = rowAlterations.get(table);
				if (rowIds == null) {
					continue TABLESLOOP;
				}
				String rowIdFilterString = "";
				for (int rowId : rowIds) {
					rowIdFilterString += rowId + " OR ";
				}
				rowIdFilterString = rowIdFilterString.substring(0, rowIdFilterString.length() - 4);
				BaseField primaryKey = table.getPrimaryKey();
				try {
					BaseField lastModifiedField = table.getField(HiddenFields.LAST_MODIFIED
							.getFieldName());
					Set<BaseReportInfo> reports = table.getReports();
					REPORTSLOOP: for (BaseReportInfo report : reports) {
						if ((!report.equals(table.getDefaultReport()))
								&& (!report.getReportName().startsWith("dbvcalc_"))
								&& (!report.getReportName().startsWith("dbvcrit_"))) {
							Map<BaseField, String> filterValues = new HashMap<BaseField, String>();
							filterValues.put(primaryKey, rowIdFilterString);
							List<DataRowInfo> reportDataRows = null;
							try {
								reportDataRows = dataManagement.getReportDataRows(company, report,
										filterValues, true, new HashMap<BaseField, Boolean>(), -1);
							} catch (SQLException sqlex) {
								logger.error("Broken report " + company + ": " + report.getModule()
										+ " -> " + report + ". " + sqlex);
								continue REPORTSLOOP;
							}
							for (DataRowInfo reportDataRow : reportDataRows) {
								for (BaseField field : report.getReportBaseFields()) {
									if ((field.getFieldCategory().equals(FieldCategory.NUMBER) || field
											.getFieldCategory().equals(FieldCategory.DATE))
											&& (!field.getHidden())) {
										DataRowFieldInfo dataRowField = reportDataRow
												.getValue(field);
										double stdDevs = dataRowField.getNumberOfStdDevsFromMean();
										if (Math.abs(stdDevs) > 1) {
											// We've found an outlier, get some
											// info about it to report
											int rowId = reportDataRow.getRowId();
											Map<BaseField, BaseValue> tableDataRow = dataManagement
													.getTableDataRow(table, rowId);
											DateValue lastModifiedValue = (DateValue) tableDataRow
													.get(lastModifiedField);
											DashboardOutlierInfo dashboardOutlier = new DashboardOutlier(
													report, rowId, field, dataRowField,
													lastModifiedValue);
											dashboardOutliers.add(dashboardOutlier);
										}
									}
								}
							}
						}
						Thread.yield();
						if (Thread.currentThread().isInterrupted()) {
							throw new InterruptedException();
						}
					}
				} catch (PortalBaseException pbex) {
					logger.error("PortalBaseException populating dashboard for " + company
							+ ", working on table " + table + ". " + pbex);
				} catch (SQLException sqlex) {
					logger.error("SQL exception populating dashboard for " + company
							+ ", working on table " + table + ". " + sqlex);
				}
			}
			int minStdDev = 1;
			while (dashboardOutliers.size() > 100) {
				for (Iterator<DashboardOutlierInfo> iterator = dashboardOutliers.iterator(); iterator
						.hasNext();) {
					DashboardOutlierInfo dashboardOutlier = iterator.next();
					if (Math.abs(dashboardOutlier.getDataRowField().getNumberOfStdDevsFromMean()) < minStdDev) {
						iterator.remove();
					}
				}
				minStdDev++;
			}
			companyOutliers.put(company, dashboardOutliers);
		}
		return companyOutliers;
	}

	/**
	 * Return all tables from all companies passed in
	 */
	private static Set<TableInfo> getAllTables(Set<CompanyInfo> companies) {
		Set<TableInfo> allTables = new HashSet<TableInfo>();
		for (CompanyInfo company : companies) {
			allTables.addAll(company.getTables());
		}
		return allTables;
	}

	/**
	 * From the log, find which rows have been recently altered in each table
	 */
	private synchronized Map<TableInfo, Set<Integer>> getRecentRowAlterations(
			Set<TableInfo> allTables) {
		if (this.rowAlterationsCache.size() > 0) {
			return this.rowAlterationsCache;
		}
		Map<TableInfo, Set<Integer>> rowAlterations = new HashMap<TableInfo, Set<Integer>>();
		String SQLCode = "SELECT DISTINCT app_table, row_id FROM dbint_log_data_change";
		SQLCode += " WHERE app_timestamp > now() - '31 days'::interval";
		Connection conn = null;
		try {
			conn = this.databaseDefn.getDataSource().getConnection();
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			ResultSet results = statement.executeQuery();
			TableInfo table = null;
			RESULTSLOOP: while (results.next()) {
				String internalTableName = results.getString(1);
				int rowId = results.getInt(2);
				try {
					table = this.getTableByInternalName(allTables, internalTableName);
				} catch (ObjectNotFoundException onfex) {
					// table has been deleted since event was logged
					continue RESULTSLOOP;
				}
				Set<Integer> tableRowIds = rowAlterations.get(table);
				if (tableRowIds == null) {
					tableRowIds = new HashSet<Integer>();
				}
				tableRowIds.add(rowId);
				rowAlterations.put(table, tableRowIds);
			}
			results.close();
			statement.close();
		} catch (SQLException sqlex) {
			logger.error(sqlex.toString());
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Further error closing SQL connection");
					e.printStackTrace();
				}
			}
		}
		this.rowAlterationsCache = rowAlterations;
		return rowAlterations;
	}

	private Map<CompanyInfo, SortedSet<DashboardReportSummaryInfo>> findSuggestedSummaries()
			throws CodingErrorException, ObjectNotFoundException, CantDoThatException {
		Map<CompanyInfo, SortedSet<DashboardReportSummaryInfo>> suggestedOutliers = new HashMap<CompanyInfo, SortedSet<DashboardReportSummaryInfo>>();
		Authenticator authenticator = (Authenticator) this.databaseDefn.getAuthManager()
				.getAuthenticator();
		DataManagementInfo dataManagement = this.databaseDefn.getDataManagement();
		Set<CompanyInfo> companies = authenticator.getCompanies();
		Set<TableInfo> allTables = getAllTables(companies);
		Map<TableInfo, Set<Integer>> rowAlterations = this.getRecentRowAlterations(allTables);
		for (CompanyInfo company : companies) {
			SortedSet<DashboardReportSummaryInfo> companySuggestedOutliers = new TreeSet<DashboardReportSummaryInfo>();
			for (TableInfo table : company.getTables()) {
				REPORTSLOOP: for (BaseReportInfo report : table.getReports()) {
					ReportSummaryInfo reportSummary = report.getReportSummary();
					int reportSummaryScore = 0;
					// reports based on recently updated data score highly
					Set<TableInfo> joinReferencedTables = ((SimpleReportInfo) report)
							.getJoinReferencedTables();
					for (TableInfo joinReferencedTable : joinReferencedTables) {
						if (rowAlterations.keySet().contains(joinReferencedTable)) {
							if (joinReferencedTable.equals(report.getParentTable())) {
								reportSummaryScore += 5;
							} else {
								reportSummaryScore += 1;
							}
						}
					}
					// aggregates other than count score highly
					Set<ReportSummaryAggregateInfo> aggregates = reportSummary
							.getAggregateFunctions();
					NONCOUNTCHECK: for (ReportSummaryAggregateInfo aggregate : aggregates) {
						if (!aggregate.isCountFunction()) {
							reportSummaryScore += 5;
							break NONCOUNTCHECK;
						}
					}
					// groupings other than text score highly
					Set<ReportSummaryGroupingInfo> groupings = reportSummary.getGroupings();
					boolean containsTextGroupings = false;
					boolean containsNonTextGroupings = false;
					for (ReportSummaryGroupingInfo grouping : groupings) {
						FieldCategory category = grouping.getGroupingReportField().getBaseField()
								.getFieldCategory();
						if (category.equals(FieldCategory.TEXT)) {
							containsTextGroupings = true;
						} else {
							containsNonTextGroupings = true;
						}
					}
					if (containsNonTextGroupings && !containsTextGroupings) {
						reportSummaryScore += 5;
					}
					try {
						ReportSummaryDataInfo reportSummaryData = dataManagement
								.getReportSummaryData(company, report,
										new HashMap<BaseField, String>());
						// summary reports with text groupings and many rows score low
						if (containsTextGroupings
								&& (reportSummaryData.getReportSummaryDataRows().size() > 40)) {
							reportSummaryScore -= 5;
						}
						DashboardReportSummaryInfo dashboardReportSummary = new DashboardReportSummary(
								reportSummary, reportSummaryData, reportSummaryScore);
						companySuggestedOutliers.add(dashboardReportSummary);
					} catch (SQLException sqlex) {
						logger.error("Broken summary report for '" + report + "': " + sqlex);
						continue REPORTSLOOP;
					}
				}
			}
			suggestedOutliers.put(company, companySuggestedOutliers);
		}
		return suggestedOutliers;
	}

	private TableInfo getTableByInternalName(Set<TableInfo> tables, String internalTableName)
			throws ObjectNotFoundException {
		TableInfo cachedTable = this.tableCache.get(internalTableName);
		if (cachedTable != null) {
			return cachedTable;
		}
		for (TableInfo table : tables) {
			if (table.getInternalTableName().equals(internalTableName)) {
				this.tableCache.put(internalTableName, table);
				return table;
			}
		}
		// if we've got to here the table hasn't been found
		throw new ObjectNotFoundException("The table with id '" + internalTableName
				+ "' doesn't exist");
	}

	private DatabaseInfo databaseDefn = null;

	/**
	 * Lookup of internal table name to table
	 */
	private Map<String, TableInfo> tableCache = new HashMap<String, TableInfo>();

	private Map<TableInfo, Set<Integer>> rowAlterationsCache = new HashMap<TableInfo, Set<Integer>>();

	private static final SimpleLogger logger = new SimpleLogger(DashboardPopulator.class);

}
