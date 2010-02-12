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
package com.gtwm.pb.model.interfaces;

import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.manageSchema.JoinType;
import java.util.Date;

/**
 * Represents an SQL join in a report, i.e. 'table1 inner join table2 on table1.field1 = table2.field1' or
 * something like that. Can join between tables, reports and a combination of the two
 */
public interface JoinClauseInfo extends Comparable<JoinClauseInfo> {
    /**
     * @return Whether the left hand side of the join is a table or a report
     */
    public boolean isLeftPartTable();

    /**
     * @return Whether the right hand side of the join is a table or a report
     */
    public boolean isRightPartTable();

    /**
     * Use this to get the left field if the left hand side of the join is on a table
     * 
     * @return The left hand field in the table join
     * @throws CantDoThatException
     *             If the left hand side of the join is a report rather than a table
     */
    public BaseField getLeftTableField() throws CantDoThatException;

    public BaseField getRightTableField() throws CantDoThatException;

    /**
     * Use this to get the left field if the left hand side of the join is on a report
     * 
     * @return ReportFieldInfo object representing the report field, which can be used to get the actual field
     *         object as well as the report it is in
     * @throws CantDoThatException
     */
    public ReportFieldInfo getLeftReportField() throws CantDoThatException;

    public ReportFieldInfo getRightReportField() throws CantDoThatException;

    /**
     * @return Whether the join is an INNER, LEFT OUTER, RIGHT OUTER or FULL OUTER join
     */
    public JoinType getJoinType();
    
    public String getInternalJoinName();
    
    /**
     * Get the time the object was constructed - used for sorting joins
     */
    public Date getCreationTimestamp();
}
