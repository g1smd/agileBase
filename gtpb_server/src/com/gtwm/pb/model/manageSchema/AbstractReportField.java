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
package com.gtwm.pb.model.manageSchema;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.util.CantDoThatException;

/**
 * Used to provide common code to ReportFieldDefn and ReportCalcFieldDefn
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractReportField {

    @Id
    @GeneratedValue
    /**
     * Hibernate needs an ID for a persistent class - this isn't actually used by the app otherwise
     */
    protected long getId() {
        return this.id;
    }
    
    private void setId(long id) {
        this.id = id;
    }

    //Hibernate doesn't like abstract methods without an annotation
    //public abstract BaseField getBaseField();

    @Transient
    public BaseReportInfo getReportFieldIsFrom() throws CantDoThatException {
        if (this.getReportFieldIsFromDirect() == null) {
            throw new CantDoThatException("Field is from a table, not a report");
        }
        return this.getReportFieldIsFromDirect();
    }

    @ManyToOne(targetEntity = BaseReportDefn.class)
    // Uni-directional many to one
    private BaseReportInfo getReportFieldIsFromDirect() {
        return this.reportFieldIsFrom;
    }

    protected void setReportFieldIsFromDirect(BaseReportInfo reportFieldIsFrom) {
        this.reportFieldIsFrom = reportFieldIsFrom;
    }

    @ManyToOne(targetEntity = BaseReportDefn.class)
    // Other side of report.getReportFields(). Note: Should entity be SimpleReportDefn.class?
    public BaseReportInfo getParentReport() {
        return this.parentReport;
    }

    protected void setParentReport(BaseReportInfo parentReport) {
        this.parentReport = parentReport;
    }

    @Transient
    public boolean isFieldFromReport() {
        if (this.getReportFieldIsFromDirect() == null) {
            return false;
        }
        return true;
    }

    public void setFieldIndex(Integer fieldIndex) {
        this.fieldIndex = fieldIndex;
    }

    public Integer getFieldIndex() {
        return this.fieldIndex;
    }

    private BaseReportInfo reportFieldIsFrom = null;

    private BaseReportInfo parentReport = null;
    
    private Integer fieldIndex = 0;
    
    private long id;
}
