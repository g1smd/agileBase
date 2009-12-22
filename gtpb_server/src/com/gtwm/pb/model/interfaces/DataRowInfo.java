/*
 *  Copyright 2009 GT webMarque Ltd
 * 
 *  This file is part of GT portalBase.
 *
 *  GT portalBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  GT portalBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with GT portalBase.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gtwm.pb.model.interfaces;

import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.RelationField;
import com.gtwm.pb.model.manageData.DataRow;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Represents one row in a set of results data for a report, i.e. basically a
 * set of field=value pairs
 */
public interface DataRowInfo {

	/**
	 * Return the primary key for this row
	 */
	public int getRowId();

	/**
	 * Return the value of a particular field in the row
	 */
	public DataRowFieldInfo getValue(BaseField field);

	/**
	 * Return the value of a particular field, identified by internal field ID
	 * or field name
	 * 
	 * @throws ObjectNotFoundException
	 *             if the given fieldID doesn't match any field in the report by
	 *             ID or name
	 */
	public DataRowFieldInfo getValue(String fieldID) throws ObjectNotFoundException;

	/**
	 * Return the fields and values this report data row contains
	 */
	public Map<BaseField, DataRowFieldInfo> getDataRowFields();

	/**
	 * Return a list of related records for each RelationField in this row of
	 * data The lists are stored in a Map via the RelationField object
	 */
	public Map<RelationField, List<DataRow>> getChildDataRows(DatabaseInfo databaseDefn,
			Connection conn, HttpServletRequest request) throws SQLException, ObjectNotFoundException, CodingErrorException;
}
