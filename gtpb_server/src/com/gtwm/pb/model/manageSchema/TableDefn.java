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

import java.util.Iterator;
import java.util.Locale;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.SimpleReportInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.RelationField;
import com.gtwm.pb.model.manageSchema.fields.IntegerFieldDefn;
import com.gtwm.pb.model.interfaces.fields.SequenceField;
import com.gtwm.pb.model.manageSchema.fields.AbstractField;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.CantDoThatException;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import org.grlea.log.SimpleLogger;

@Entity
public class TableDefn implements TableInfo {

	protected TableDefn() {
	}

	public TableDefn(String internalTableName, String tableName, String tableDesc) {
		this.setTableName(tableName);
		// store db friendly version of the table name
		if (internalTableName == null) {
			this.setInternalTableName((new RandomString()).toString());
		} else {
			this.setInternalTableName(internalTableName);
		}
		// don't store a null description, the Velocity templating language
		// doesn't like nulls
		if (tableDesc == null) {
			this.setTableDescription("");
		} else {
			this.setTableDescription(tableDesc);
		}
	}

	@Id
	// Hibernate note: don't use @GeneratedValue, see section 4.3 of the manual
	// wrt equals and hashCode
	public String getInternalTableName() {
		return this.internalTableName;
	}

	/**
	 * Hibernate use only
	 */
	private void setInternalTableName(String internalTableName) {
		this.internalTableName = internalTableName;
	}

	public synchronized void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public synchronized void setTableDescription(String tableDesc) {
		this.tableDesc = tableDesc;
	}

	public synchronized String getTableName() {
		return this.tableName;
	}

	public synchronized String getTableDescription() {
		return this.tableDesc;
	}

	public synchronized void addField(BaseField fieldToAdd) throws CantDoThatException {
		if (!this.getFieldsCollection().contains(fieldToAdd)) {
			fieldToAdd.setFieldIndex(this.getFieldsCollection().size());
			this.getFieldsCollection().add(fieldToAdd);
			// shuffle hidden fields to end
			this.correctFieldIndexes();
		}
	}

	public synchronized void setFieldIndex(int index, BaseField fieldToOrder)
			throws ObjectNotFoundException, CantDoThatException {
		Set<BaseField> fieldsCollection = this.getFieldsCollection();
		// range check index
		Integer validIndex = index;
		if (validIndex < 0) {
			validIndex = 0;
		} else if (validIndex > (fieldsCollection.size() - 1)) {
			validIndex = fieldsCollection.size() - 1;
		}
		Integer originalIndex = fieldToOrder.getFieldIndex();
		Iterator<BaseField> iterator = fieldsCollection.iterator();
		if (validIndex > originalIndex) { // moving towards the end
			while (iterator.hasNext()) {
				BaseField field = iterator.next();
				int fieldIndex = field.getFieldIndex();
				if ((fieldIndex > originalIndex) && (fieldIndex <= validIndex)) {
					field.setFieldIndex(fieldIndex - 1);
				}
			}
		} else { // moving towards the beginning
			while (iterator.hasNext()) {
				BaseField field = iterator.next();
				int fieldIndex = field.getFieldIndex();
				if ((fieldIndex >= validIndex) && (fieldIndex < originalIndex)) {
					field.setFieldIndex(fieldIndex + 1);
				}
			}
		}
		fieldToOrder.setFieldIndex(validIndex);
		// just ensure everything's right - in old tables, field indexes may be corrupt
		this.correctFieldIndexes();
	}

	/**
	 * Ensure indexes are consistent, e.g. place all hidden fields at the end
	 */
	private void correctFieldIndexes() throws CantDoThatException {
		Map<BaseField, Integer> correctIndexes = new HashMap<BaseField, Integer>();
		Integer correctIndex = 0;
		for (BaseField field : this.getFields()) {
			if (!field.getHidden()) {
				correctIndexes.put(field, correctIndex);
				correctIndex++;
			}
		}
		// put the hidden fields at the end
		for (BaseField field : this.getFields()) {
			if (field.getHidden()) {
				correctIndexes.put(field, correctIndex);
				correctIndex++;
			}
		}
		Iterator<BaseField> iterator = this.getFieldsCollection().iterator();
		while (iterator.hasNext()) {
			BaseField field = iterator.next();
			correctIndex = correctIndexes.get(field);
			if (!field.getFieldIndex().equals(correctIndex)) {
				// logger.warn("Field index " + field.getFieldIndex() +
				// " incorrect for field "
				// + field + ", correcting to " + correctIndex);
				field.setFieldIndex(correctIndex);
			}
		}
		// Copy updated field ordering to default report
		((SimpleReportDefn) this.getDefaultReport()).copyBaseFieldIndexes();
	}

	public synchronized void removeField(BaseField fieldToRemove) {
		this.getFieldsCollection().remove(fieldToRemove);
		int removedFieldIndex = fieldToRemove.getFieldIndex();
		// shuffle other fields down
		Iterator<BaseField> iterator = this.getFieldsCollection().iterator();
		while (iterator.hasNext()) {
			BaseField field = iterator.next();
			if (field.getFieldIndex() > removedFieldIndex) {
				field.setFieldIndex(field.getFieldIndex() - 1);
			}
		}
	}

	@Transient
	public synchronized SortedSet<BaseField> getFields() {
		SortedSet<BaseField> fieldsToReturn = new TreeSet<BaseField>(this.getFieldsCollection());
		return Collections.unmodifiableSortedSet(fieldsToReturn);
	}

	/**
	 * For use by Hibernate only
	 */
	@OneToMany(mappedBy = "tableContainingField", targetEntity = AbstractField.class, cascade = CascadeType.ALL)
	protected synchronized Set<BaseField> getFieldsCollection() {
		return this.fields;
	}

	/**
	 * Hibernate only
	 */
	private synchronized void setFieldsCollection(Set<BaseField> fields) {
		this.fields = fields;
	}

	@Transient
	public synchronized BaseField getField(String fieldID)
			throws ObjectNotFoundException {
		// everyone's a suspect!
		Set<BaseField> fields = this.getFieldsCollection();
		for (BaseField suspectField : fields) {
			if (suspectField.getInternalFieldName().equals(fieldID)) {
				return suspectField;
			}
		}
		// not found by internal name, try by public name
		for (BaseField suspectField : this.getFieldsCollection()) {
			if (suspectField.getFieldName().equalsIgnoreCase(fieldID)) {
				return suspectField;
			}
		}
		// if we've got to here the field hasn't been found
		throw new ObjectNotFoundException("The field with id '" + fieldID
				+ "' doesn't exist in table " + this.getTableName());
	}

	public synchronized void addReport(BaseReportInfo reportToAdd, boolean isDefaultReport) {
		this.getReportsCollection().add(reportToAdd);
		if (isDefaultReport) {
			if (reportToAdd instanceof SimpleReportDefn) {
				this.setDefaultReport((SimpleReportInfo) reportToAdd);
			} else {
				throw new UnsupportedOperationException(
						"Reports of type "
								+ reportToAdd.getClass().getSimpleName()
								+ "cannot be used as a table's default report; only simple reports may be used");
			}
		}
	}

	public synchronized void removeReport(BaseReportInfo reportToRemove) {
		this.getReportsCollection().remove(reportToRemove);
	}

	@Transient
	public synchronized SortedSet<BaseReportInfo> getReports() {
		return Collections.unmodifiableSortedSet(new TreeSet<BaseReportInfo>(this
				.getReportsCollection()));
	}

	@OneToMany(mappedBy = "parentTable", targetEntity = BaseReportDefn.class, cascade = CascadeType.ALL)
	protected synchronized Set<BaseReportInfo> getReportsCollection() {
		return this.reports;
	}

	private synchronized void setReportsCollection(Set<BaseReportInfo> reports) {
		this.reports = reports;
	}

	public synchronized void setDefaultReport(SimpleReportInfo report) {
		this.setDefaultReportDirect(report);
	}

	@Transient
	public synchronized SimpleReportInfo getDefaultReport() {
		return (SimpleReportInfo) this.getDefaultReportDirect();
	}

	private void setDefaultReportDirect(BaseReportInfo defaultReportDirect) {
		this.defaultReportDirect = (SimpleReportInfo) defaultReportDirect;
	}

	@OneToOne(targetEntity = BaseReportDefn.class, cascade = CascadeType.ALL)
	private BaseReportInfo getDefaultReportDirect() {
		return this.defaultReportDirect;
	}

	@Transient
	public synchronized BaseReportInfo getReport(String reportID)
			throws ObjectNotFoundException {
		Set<BaseReportInfo> reports = this.getReportsCollection();
		for (BaseReportInfo report : reports) {
			if (report.getInternalReportName().equals(reportID)) {
				return report;
			}
		}
		// Report with that internal name doesn't exist
		// Try treating it as a public name
		for (BaseReportInfo report : reports) {
			if (report.getReportName().equalsIgnoreCase(reportID)) {
				return report;
			}
		}
		throw new ObjectNotFoundException("The report with id '" + reportID
				+ "' doesn't exist in table " + this.tableName + "(" + this.internalTableName + ")");
	}

	@Transient
	public synchronized boolean isDependentOn(TableInfo table) {
		for (BaseField myField : this.getFieldsCollection()) {
			if (myField instanceof RelationField) {
				TableInfo sourceTable = ((RelationField) myField).getRelatedTable();
				if (sourceTable.equals(table)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Provide a natural sort order by table name case insensitively
	 */
	public int compareTo(TableInfo otherTable) {
		if (this == otherTable) {
			return 0;
		}
		String otherTableName = otherTable.getTableName();
		// Include internal table name in comparison as well because equals() is
		// based on internal table name,
		// not table name
		String otherInternalTableName = otherTable.getInternalTableName();
		String lhs = this.getTableName().toLowerCase(Locale.UK) + this.getInternalTableName();
		String rhs = otherTableName.toLowerCase(Locale.UK) + otherInternalTableName;
		return lhs.compareTo(rhs);
	}

	/**
	 * Define equals() based on internal table name
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		TableDefn otherTable = (TableDefn) obj;
		return this.getInternalTableName().equals(otherTable.getInternalTableName());
	}

	/**
	 * Hash code consistent with equals
	 */
	public int hashCode() {
		return this.getInternalTableName().hashCode();
	}

	public synchronized void setPrimaryKey(SequenceField primaryKeyField)
			throws CantDoThatException {
		if (primaryKeyField.getTableContainingField().equals(this)) {
			this.setPrimaryKeyDirect(primaryKeyField);
		} else {
			throw new CantDoThatException(
					"When setting a primary key, the field specified must already be a member of the table");
		}
	}

	public synchronized void setPrimaryKey(IntegerFieldDefn primaryKeyField)
			throws CantDoThatException {
		if (primaryKeyField.getTableContainingField().equals(this)) {
			this.setPrimaryKeyDirect(primaryKeyField);
		} else {
			throw new CantDoThatException(
					"When setting a primary key, the field specified must already be a member of the table");
		}
	}

	@Transient
	public synchronized BaseField getPrimaryKey() {
		return this.getPrimaryKeyDirect();
	}

	private void setPrimaryKeyDirect(BaseField primaryKeyField) {
		this.primaryKeyField = primaryKeyField;
	}

	@OneToOne(targetEntity = AbstractField.class, cascade = CascadeType.ALL)
	// Uni-directional one to one
	private BaseField getPrimaryKeyDirect() {
		return this.primaryKeyField;
	}

	public synchronized void setRecordsLockable(Boolean lockable) {
		this.lockable = lockable;
	}

	public synchronized Boolean getRecordsLockable() {
		return this.lockable;
	}

	public String toString() {
		return this.getTableName();
	}

	private String tableName = "";

	private String tableDesc = "";

	private String internalTableName = "";

	private SimpleReportInfo defaultReportDirect = null;

	private Set<BaseField> fields = new HashSet<BaseField>();

	private Set<BaseReportInfo> reports = new HashSet<BaseReportInfo>();

	private BaseField primaryKeyField = null;

	private boolean lockable = false;

	private static final SimpleLogger logger = new SimpleLogger(TableDefn.class);
}