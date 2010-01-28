/*
 *  Copyright 2009 GT webMarque Ltd
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.BaseValue;

public interface TableDataInfo {
	/**
	 * Return the values in one record so it can be viewed or edited. The table
	 * is specified in the constructor. If there is no record matching the given
	 * rowId, then return an empty map.
	 * 
	 * If the rowId is negative, return an unspecified record
	 * 
	 * @param rowId
	 *            Row identifier
	 * @return A map of field => field value
	 * @throws SQLException
	 *             There shouln't be an SQLException but there may be
	 * @throws CantDoThatException
	 *             If there was an internal error because of out-of-date code in
	 *             the method - specifically, one of the table's fields is of a
	 *             type the method doesn't recognise
	 */
	public Map<BaseField, BaseValue> getTableDataRow(Connection conn, int rowId)
			throws CantDoThatException, SQLException, ObjectNotFoundException, CodingErrorException;

	/**
	 * Checks whether the record identified by rowId is locked, taking into
	 * account whether the lock has been overridden for the logged in user
	 */
	public boolean isRecordLocked(Connection conn, SessionDataInfo sessionData, int rowId)
			throws ObjectNotFoundException, SQLException;
}
