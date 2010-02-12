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

import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import java.util.Set;

/**
 * A representation of an SQL UNION VIEW
 */
public interface UnionReportInfo extends BaseReportInfo {
    /**
     * Add a report into the UNION set
     * 
     * @return Whether the report was already in the set
     */
    public boolean addNewReport(BaseReportInfo report);

    /**
     * Remove a report from the UNION set
     * 
     * @return Whether this report contained the report to remove
     */
    public boolean removeReport(BaseReportInfo report);

    /**
     * @return A read-only copy of all the reports that make up the UNION
     */
    public Set<BaseReportInfo> getReports();

    // public void addSort (BaseField field, boolean ascending);
    // public void removeSort (BaseField field);

    // public void addLimit (int limit);
    // public void removeLimit ();
    
    /**
     * Return a set of all reports having the same fields, in the same order.
     * If no report has yet been selected no matching reports will be returned.<br>
     * The method could later be improved to:
     * <ol>
     * <li>to allow any report with the same data types in the same order
     *    (rather than restricting to exactly matching fields)</li>
     * <li>to allow any report from any table to be considered</li>
     * </ol>
     */
    public Set<BaseReportInfo> getCandidateReports() throws CodingErrorException;
}