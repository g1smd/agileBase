/*
 *  Copyright 2011 GT webMarque Ltd
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
package com.gtwm.pb.model.interfaces;

import java.util.Map;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.util.Enumerations.AppAction;

/**
 * Handles logging of usage information to a database, which can then be
 * reported on to see which tables/reports are being used the most, what schema
 * changes have taken place etc.
 */
public interface UsageLoggerInfo {

	public void logReportView(AppUserInfo user, BaseReportInfo report,
			Map<BaseField, String> reportFilterValues, int rowLimit, String extraDetails);

	public void logTableSchemaChange(AppUserInfo user, TableInfo table, AppAction appAction,
			String details);

	public void logReportSchemaChange(AppUserInfo user, BaseReportInfo report, AppAction appAction,
			String details);

	public void logDataChange(AppUserInfo user, TableInfo table, AppAction appAction, int rowId, String details);

	public void logLogin(AppUserInfo user, String ipAddress, String browser);
}
