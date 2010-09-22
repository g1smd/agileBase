/*
 *  Copyright 2010 GT webMarque Ltd
 * 
 *  This file is part of agileBase.
 *
 *  agileBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  agileBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with agileBase.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gtwm.pb.model.manageUsage;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.UsageLoggerInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.util.Enumerations.AppAction;

public class UsageLogger implements UsageLoggerInfo, Runnable {

	private UsageLogger() {
		this.relationalDataSource = null;
	}

	/**
	 * @param relationalDataSource
	 *            A connection to the database which info will be logged to
	 */
	public UsageLogger(DataSource relationalDataSource) {
		this.relationalDataSource = relationalDataSource;
	}

	/*
	 * Actually do the logging to the database.
	 * 
	 * Note: Thread.yield() statements are between potentially long running
	 * operations
	 */
	public void run() {
		// Delay execution to give the original action we're logging time to
		// complete, or get off to a good start at least. We don't want to make
		// a slow action even slower by adding in a log statement in
		// the middle.
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			logger.error("UsageLogger interrupted while sleeping");
		}
		String companyName = this.user.getCompany().getCompanyName();
		String userName = this.user.getUserName();
		String appActionName = "";
		if (this.appAction != null) {
			appActionName = this.appAction.toString().toLowerCase().replace('_', ' ');
		}
		String SQLCode = "INSERT INTO dbint_log_" + this.logType.toString().toLowerCase();
		Connection conn = null;
		try {
			conn = relationalDataSource.getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = null;
			Thread.yield();
			switch (this.logType) {
			case REPORT_VIEW:
				SQLCode += "(company, app_user, report, details, app_timestamp) VALUES (?,?,?,?,?)";
				statement = conn.prepareStatement(SQLCode);
				statement.setString(1, companyName);
				statement.setString(2, userName);
				statement.setString(3, this.report.getInternalReportName());
				statement.setString(4, this.details);
				statement.setTimestamp(5, this.timestamp);
				break;
			case TABLE_SCHEMA_CHANGE:
				SQLCode += "(company, app_user, app_table, app_action, details, app_timestamp) VALUES (?,?,?,?,?,?)";
				statement = conn.prepareStatement(SQLCode);
				statement.setString(1, companyName);
				statement.setString(2, userName);
				statement.setString(3, this.table.getInternalTableName());
				statement.setString(4, appActionName);
				statement.setString(5, this.details);
				statement.setTimestamp(6, this.timestamp);
				break;
			case REPORT_SCHEMA_CHANGE:
				SQLCode += "(company, app_user, report, app_action, details, app_timestamp) VALUES(?,?,?,?,?,?)";
				statement = conn.prepareStatement(SQLCode);
				statement.setString(1, companyName);
				statement.setString(2, userName);
				statement.setString(3, this.report.getInternalReportName());
				statement.setString(4, appActionName);
				statement.setString(5, this.details);
				statement.setTimestamp(6, this.timestamp);
				break;
			case DATA_CHANGE:
				SQLCode += "(company, app_user, app_table, app_action, row_id, saved_data, app_timestamp) VALUES (?,?,?,?,?,?,?)";
				statement = conn.prepareStatement(SQLCode);
				statement.setString(1, companyName);
				statement.setString(2, userName);
				statement.setString(3, this.table.getInternalTableName());
				statement.setString(4, appActionName);
				statement.setInt(5, this.rowId);
				statement.setString(6, this.details);
				statement.setTimestamp(7, this.timestamp);
				break;
			case LOGIN:
				SQLCode += "(company, app_user, ip_address, app_timestamp) VALUES (?,?,?,?)";
				statement = conn.prepareStatement(SQLCode);
				statement.setString(1, companyName);
				statement.setString(2, userName);
				statement.setString(3, this.ipAddress);
				statement.setTimestamp(4, this.timestamp);
				break;
			}
			Thread.yield();
			statement.executeUpdate();
			statement.close();
			conn.commit();
		} catch (SQLException sqlex) {
			// deal with exceptions here, don't rethrow, we're only logging
			logger.error("Error logging action to database. Logger SQL = " + SQLCode);
			logger.error("Exception details: " + sqlex);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqlex) {
					logger.error("Error closing SQL connection. " + sqlex);
				}
			}
		}
	}

	public void logReportView(AppUserInfo user, BaseReportInfo report,
			Map<BaseField, String> reportFilterValues, int rowLimit) {
		this.logType = LogType.REPORT_VIEW;
		this.user = user;
		this.report = report;
		if (reportFilterValues != null) {
			if (reportFilterValues.size() > 0) {
				this.details = "Session filters = " + reportFilterValues;
			}
		}
		if (rowLimit != 100) {
			if (!this.details.equals("")) {
				this.details += ", ";
			}
			this.details += "row limit = " + rowLimit;
		}
		this.timestamp.setTime(System.currentTimeMillis());
	}

	public void logTableSchemaChange(AppUserInfo user, TableInfo table, AppAction appAction,
			String details) {
		this.logType = LogType.TABLE_SCHEMA_CHANGE;
		this.user = user;
		this.table = table;
		this.appAction = appAction;
		this.details = details;
		this.timestamp.setTime(System.currentTimeMillis());
	}

	public void logReportSchemaChange(AppUserInfo user, BaseReportInfo report, AppAction appAction,
			String details) {
		this.logType = LogType.REPORT_SCHEMA_CHANGE;
		this.user = user;
		this.report = report;
		this.appAction = appAction;
		this.details = details;
		this.timestamp.setTime(System.currentTimeMillis());
	}

	public void logDataChange(AppUserInfo user, TableInfo table, AppAction appAction, int rowId,
			String details) {
		this.logType = LogType.DATA_CHANGE;
		this.user = user;
		this.table = table;
		this.appAction = appAction;
		this.rowId = rowId;
		this.details = details;
		this.timestamp.setTime(System.currentTimeMillis());
	}

	public void logLogin(AppUserInfo user, String ipAddress) {
		this.logType = LogType.LOGIN;
		this.user = user;
		this.ipAddress = ipAddress;
		this.timestamp.setTime(System.currentTimeMillis());
	}

	public static void startLoggingThread(UsageLogger usageLogger) {
		Thread usageLoggerThread = new Thread(usageLogger);
		usageLoggerThread.setPriority(Thread.NORM_PRIORITY - 1);
		usageLoggerThread.start();
	}

	private static final SimpleLogger logger = new SimpleLogger(UsageLogger.class);

	private AppUserInfo user;

	private BaseReportInfo report;

	private TableInfo table;

	private AppAction appAction;

	private String details = "";

	private String ipAddress = "";

	private int rowId = -1;

	private final DataSource relationalDataSource;

	private Timestamp timestamp = new Timestamp(0);

	public enum LogType {
		REPORT_VIEW, TABLE_SCHEMA_CHANGE, REPORT_SCHEMA_CHANGE, DATA_CHANGE, LOGIN;
	}

	private LogType logType;

}
