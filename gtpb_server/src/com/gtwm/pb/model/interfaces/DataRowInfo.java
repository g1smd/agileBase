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
import com.gtwm.pb.model.manageData.DataRowField;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.model.interfaces.DatabaseInfo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Craig McDonnell
 *
 */
public interface DataRowInfo {

    /**
     * 
     * @param fieldSchema
     * @param field
     */
	public void addDataRowField (BaseField fieldSchema, DataRowField field);
	
	/**
     * @return A read-only copy of the collection of fields this report data row contains
     */
    public Map<BaseField, DataRowFieldInfo> getDataRowFields();
    
    /**
     * 
     * @param rowid
     */
    public void setRowId( int rowid );
    
    public int getRowId();
    
    /**
     * @return A List of related records for each RelationField in this row of data
     *         The lists are stored in a Map via the RelationField object
     * 
     * @param conn
     * 
     * @throws SQLException
     * @throws ObjectNotFoundException
     */
    public Map<RelationField, List<DataRow>> getChildDataRows( DatabaseInfo databaseDefn, Connection conn) throws SQLException, ObjectNotFoundException, CodingErrorException;
}
