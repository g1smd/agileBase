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
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.DashboardInfo;
import com.gtwm.pb.model.interfaces.DashboardOutlierInfo;
import com.gtwm.pb.model.interfaces.DataManagementInfo;
import com.gtwm.pb.model.interfaces.DataRowFieldInfo;
import com.gtwm.pb.model.interfaces.DataRowInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.BaseValue;
import com.gtwm.pb.model.interfaces.fields.DateValue;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
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
		logger.info("Starting dashboard populations");
		long startTime = System.currentTimeMillis();
		Authenticator authenticator = (Authenticator) this.databaseDefn.getAuthManager()
		.getAuthenticator();
		try {
			Map<CompanyInfo, SortedSet<DashboardOutlierInfo>> dashboardOutliers = findDashboardOutliers();
			for (CompanyInfo company : authenticator.getCompanies()) {
				SortedSet<DashboardOutlierInfo> companyOutliers = dashboardOutliers.get(company);
				DashboardInfo dashboard = new Dashboard(companyOutliers);
				company.setDashboard(dashboard);
			}
		} catch (InterruptedException iex) {
			logger.info("Dashboard population not completed due to interruption");
		}
		int duration = (int) ((System.currentTimeMillis() - startTime) / (1000 * 60));
		logger.info("Dashboards populated in " + duration + " minutes");
	}

	private Map<CompanyInfo, SortedSet<DashboardOutlierInfo>> findDashboardOutliers()
			throws InterruptedException {
		Map<CompanyInfo, SortedSet<DashboardOutlierInfo>> companyOutliers = new HashMap<CompanyInfo, SortedSet<DashboardOutlierInfo>>();
		Authenticator authenticator = (Authenticator) this.databaseDefn.getAuthManager()
				.getAuthenticator();
		DataManagementInfo dataManagement = this.databaseDefn.getDataManagement();
		Set<CompanyInfo> companies = authenticator.getCompanies();
		Set<TableInfo> allTables = this.databaseDefn.getTables();
		Map<TableInfo, Set<Integer>> rowAlterations = new HashMap<TableInfo, Set<Integer>>();
		// Preparation: from the log, find which rows have been recently altered
		// in each table
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
			while(dashboardOutliers.size() > 100) {
				for (Iterator<DashboardOutlierInfo> iterator = dashboardOutliers.iterator(); iterator.hasNext();) {
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

	/**
	 * Return any reports that have the given table's primary key as a field
	 * 
	 * @param tables
	 *            The set of tables whose reports to look through
	 * @param table
	 *            The table whose primary key to look for
	 */
	private Set<BaseReportInfo> getReportsIncludingTablePKey(Set<TableInfo> allTables,
			TableInfo table) {
		Set<BaseReportInfo> reports = this.reportsCache.get(table);
		if (reports != null) {
			return reports;
		}
		reports = new HashSet<BaseReportInfo>();
		BaseField primaryKey = table.getPrimaryKey();
		for (TableInfo reportsTable : allTables) {
			for (BaseReportInfo tableReport : reportsTable.getReports()) {
				if (!tableReport.equals(reportsTable.getDefaultReport())) {
					if (tableReport.getReportBaseFields().contains(primaryKey)) {
						reports.add(tableReport);
					}
				}
			}
		}
		this.reportsCache.put(table, reports);
		return reports;
	}

	private DatabaseInfo databaseDefn = null;

	/**
	 * Lookup of internal table name to table
	 */
	private Map<String, TableInfo> tableCache = new HashMap<String, TableInfo>();

	private Map<TableInfo, Set<BaseReportInfo>> reportsCache = new HashMap<TableInfo, Set<BaseReportInfo>>();

	private static final SimpleLogger logger = new SimpleLogger(DashboardPopulator.class);

}
