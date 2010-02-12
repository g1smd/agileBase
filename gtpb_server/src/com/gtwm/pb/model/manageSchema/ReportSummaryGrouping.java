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
package com.gtwm.pb.model.manageSchema;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import com.gtwm.pb.model.interfaces.ReportSummaryGroupingInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.manageSchema.AbstractReportField;
import com.gtwm.pb.util.Enumerations.SummaryGroupingModifier;

@Entity
public class ReportSummaryGrouping implements ReportSummaryGroupingInfo {

    protected ReportSummaryGrouping() {
    }
    
    public ReportSummaryGrouping(ReportFieldInfo groupingReportField, SummaryGroupingModifier groupingModifier) {
        this.setGroupingReportField(groupingReportField);
        this.setCreationTimeDirect(new Date());
        this.setGroupingModifier(groupingModifier);
    }
    
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
    
    @ManyToOne(targetEntity=AbstractReportField.class)
    public ReportFieldInfo getGroupingReportField() {
        return this.groupingField;
    }
    
    private void setGroupingReportField(ReportFieldInfo groupingField) {
        this.groupingField = groupingField;
    }

    private void setGroupingModifier(SummaryGroupingModifier groupingModifier) {
    	this.groupingModifier = groupingModifier;
    }
    
    @Enumerated(EnumType.STRING)
    public SummaryGroupingModifier getGroupingModifier() {
    	return this.groupingModifier;
    }
    
	@Transient
    public Date getCreationTime() {
    	return new Date(this.getCreationTimeDirect().getTime());
    }
    
    @Temporal(value=TemporalType.TIMESTAMP)
    private Date getCreationTimeDirect() {
        return this.creationTime;
    }
    
    private void setCreationTimeDirect(Date creationTime) {
        this.creationTime = creationTime;
    }

    public int compareTo(ReportSummaryGroupingInfo otherGrouping) {
        int creationTimeCompare = this.getCreationTime().compareTo(otherGrouping.getCreationTime());
        if (creationTimeCompare != 0) {
        	return creationTimeCompare;
        }
        return Long.valueOf(this.getId()).compareTo(Long.valueOf(((ReportSummaryGrouping) otherGrouping).getId())); 
    }
    
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }
        return (this.getId() == ((ReportSummaryGrouping) obj).getId());
    }
    
    public int hashCode() {
    	return Long.valueOf(this.getId()).hashCode();
    }
    
    public String toString() {
    	return this.getGroupingReportField().toString();
    }
    
    private ReportFieldInfo groupingField = null;
    
    private SummaryGroupingModifier groupingModifier = null;
    
    private Date creationTime = null;
    
    private long id;
}
