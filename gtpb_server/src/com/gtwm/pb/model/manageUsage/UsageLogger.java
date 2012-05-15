/*
 *  Copyright 2012 GT webMarque Ltd
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.DataLogEntryInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.UsageLoggerInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.util.CodingErrorException;
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
		// If logging an update for a single field value in a record
		if (this.appAction != null) {
			if (this.appAction.equals(AppAction.UPDATE_RECORD) && (this.field != null)) {
				// In the case of data updates, use a queue system to avoid
				// logging
				// every change.
				// First, add the current update to the queue
				DataLogEntryInfo newEntry = new DataLogEntry(this.user, this.field, this.rowId,
						this.details, this.appAction);
				BlockingQueue<DataLogEntryInfo> userQueue = userQueues.get(this.user);
				if (userQueue == null) {
					userQueue = new LinkedBlockingQueue<DataLogEntryInfo>();
					userQueues.put(this.user, userQueue);
				}
				userQueue.add(newEntry);
				// Then compare with old value
				if (userQueue.size() > 1) {
					try {
						DataLogEntryInfo oldEntry = userQueue.take();
						// If old entry is of a different field, log it
						// otherwise
						// forget it, the new one will be used instead
						if ((!oldEntry.getField().equals(this.field))
								|| (oldEntry.getRowId() != this.rowId)) {
							this.table = oldEntry.getField().getTableContainingField();
							this.rowId = oldEntry.getRowId();
							this.timestamp.setTime(oldEntry.getTime());
							this.details = oldEntry.getValue();
						} else {
							return;
						}
					} catch (InterruptedException iex) {
						logger.error("Logging of update interrupted: " + iex + ". Current queue = "
								+ userQueue);
						Thread.currentThread().interrupt();
					}
				} else {
					// We've only got one thing in the queue, wait until another
					// comes along before deciding whether to log it
					return;
				}
			}
		}
		// Delay execution to give the original action we're logging time to
		// complete, or get off to a good start at least. We don't want to make
		// a slow action even slower by adding in a log statement in
		// the middle.
		if (this.logType.equals(LogType.REPORT_VIEW)) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.error("UsageLogger interrupted while sleeping");
			}
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
				SQLCode += "(company, app_user, ip_address, app_timestamp, details) VALUES (?,?,?,?,?)";
				statement = conn.prepareStatement(SQLCode);
				statement.setString(1, companyName);
				statement.setString(2, userName);
				statement.setString(3, this.ipAddress);
				statement.setTimestamp(4, this.timestamp);
				statement.setString(5, this.details);
				break;
			}
			Thread.yield();
			statement.executeUpdate();
			statement.close();
			if (this.logType.equals(LogType.DATA_CHANGE)) {
				logDataOlderThan(conn, 30);
			}
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

	/**
	 * Clear out the queue of old entries (or all entries if seconds=0 is
	 * specified)
	 */
	public static void logDataOlderThan(Connection conn, int seconds) {
		long someTimeAgo = System.currentTimeMillis() - (seconds * 1000);
		Set<DataLogEntryInfo> oldEntries = new HashSet<DataLogEntryInfo>();
		for (Map.Entry<AppUserInfo, BlockingQueue<DataLogEntryInfo>> entry : userQueues.entrySet()) {
			AppUserInfo user = entry.getKey();
			BlockingQueue<DataLogEntryInfo> queue = entry.getValue();
			boolean oldiesAllDone = false;
			while ((queue.size() > 0) && (!oldiesAllDone)) {
				DataLogEntryInfo logEntry = queue.peek();
				if (logEntry.getTime() < someTimeAgo) {
					oldEntries.add(logEntry);
					queue.remove(logEntry);
				} else {
					oldiesAllDone = true;
				}
			}
			if (queue.size() == 0) {
				userQueues.remove(user);
			}
		}
		if (oldEntries.size() > 0) {
			String SQLCode = "INSERT INTO dbint_log_"
					+ LogType.DATA_CHANGE.toString().toLowerCase();
			SQLCode += "(company, app_user, app_table, app_action, row_id, saved_data, app_timestamp) VALUES (?,?,?,?,?,?,?)";
			try {
				PreparedStatement statement = conn.prepareStatement(SQLCode);
				for (DataLogEntryInfo oldEntry : oldEntries) {
					AppUserInfo user = oldEntry.getUser();
					statement.setString(1, user.getCompany().getCompanyName());
					statement.setString(2, user.getUserName());
					statement.setString(3, oldEntry.getField().getTableContainingField()
							.getInternalTableName());
					statement.setString(4, oldEntry.getAppAction().toString().toLowerCase()
							.replace('_', ' '));
					statement.setInt(5, oldEntry.getRowId());
					statement.setString(6, oldEntry.getValue());
					statement.setTimestamp(7, new Timestamp(oldEntry.getTime()));
					int rowsInserted = statement.executeUpdate();
					if (rowsInserted != 1) {
						logger.error("Logging 1 data change but inserted " + rowsInserted
								+ " rows with " + statement);
					}
					Thread.yield();
				}
				statement.close();
			} catch (SQLException sqlex) {
				// TODO Auto-generated catch block
				logger.error("Error logging old data changes to database. Logger SQL = " + SQLCode);
				logger.error("Exception details: " + sqlex);
			}
		}
	}

	/**
	 * Send a string to a localhost HTTP server for broadcasting with a websocket
	 * message
	 */
	public static void sendNotification(AppUserInfo user, TableInfo table, int rowId, String notification) throws CodingErrorException {
		// Generate the JSON message
		JsonFactory jsonFactory = new JsonFactory();
		StringWriter stringWriter = new StringWriter(512);
		JsonGenerator jg;
		try {
			jg = jsonFactory.createJsonGenerator(stringWriter);
			jg.writeStartObject();
			jg.writeStringField("forename", user.getForename());
			jg.writeStringField("surname", user.getSurname());
			jg.writeStringField("internaltablename", table.getInternalTableName());
			jg.writeNumberField("rowid", rowId);
			jg.writeStringField("notification", notification);
			jg.writeEndObject();
			jg.flush();
			jg.close();
		} catch (IOException ioex) {
			throw new CodingErrorException("JSON generation threw an IO exception: " + ioex);
		}
		// Send the message
		URL localhost = null;
		try {
			localhost = new URL("http://localhost:8181");
		} catch (MalformedURLException muex) {
			throw new CodingErrorException("URL in sendNotification not valid: " + muex);
		}
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) localhost.openConnection();
			connection.setDoOutput(true);
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write(stringWriter.toString());
			out.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String decodedString;
			while ((decodedString = in.readLine()) != null) {
				logger.debug(decodedString);
			}
			in.close();
		} catch (IOException ioex) {
			logger.error("Error sending HTTP notification: " + ioex);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	public void logReportView(AppUserInfo user, BaseReportInfo report,
			Map<BaseField, String> reportFilterValues, int rowLimit, String extraDetails) {
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
		if (extraDetails != null) {
			this.details += ", extra details = " + extraDetails;
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

	public void logDataChange(AppUserInfo user, TableInfo table, BaseField field,
			AppAction appAction, int rowId, String details) {
		this.logType = LogType.DATA_CHANGE;
		this.user = user;
		this.table = table;
		this.field = field;
		this.appAction = appAction;
		this.rowId = rowId;
		this.details = details;
		this.timestamp.setTime(System.currentTimeMillis());
	}

	public void logLogin(AppUserInfo user, String ipAddress, String browser) {
		this.logType = LogType.LOGIN;
		this.user = user;
		this.ipAddress = ipAddress;
		this.details = browser;
		this.timestamp.setTime(System.currentTimeMillis());
	}

	public static void startLoggingThread(UsageLogger usageLogger) {
		Thread usageLoggerThread = new Thread(usageLogger);
		usageLoggerThread.setPriority(Thread.NORM_PRIORITY - 1);
		usageLoggerThread.start();
	}

	private static final SimpleLogger logger = new SimpleLogger(UsageLogger.class);

	private AppUserInfo user = null;

	private BaseReportInfo report = null;

	private TableInfo table = null;

	private BaseField field = null;

	private AppAction appAction = null;

	private String details = "";

	private String ipAddress = "";

	private int rowId = -1;

	// A shared (static) data change queue for each user
	private static final Map<AppUserInfo, BlockingQueue<DataLogEntryInfo>> userQueues = new ConcurrentHashMap<AppUserInfo, BlockingQueue<DataLogEntryInfo>>();

	private final DataSource relationalDataSource;

	private Timestamp timestamp = new Timestamp(0);

	public enum LogType {
		REPORT_VIEW, TABLE_SCHEMA_CHANGE, REPORT_SCHEMA_CHANGE, DATA_CHANGE, LOGIN;
	}

	private LogType logType;

}
