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
package com.gtwm.pb.model.manageSchema;

import java.util.Iterator;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import com.gtwm.pb.model.interfaces.FormTabInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.SimpleReportInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.RelationField;
import com.gtwm.pb.model.manageSchema.fields.IntegerFieldDefn;
import com.gtwm.pb.model.interfaces.fields.SequenceField;
import com.gtwm.pb.model.manageSchema.fields.AbstractField;
import com.gtwm.pb.util.Enumerations.FormStyle;
import com.gtwm.pb.util.Naming;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.CantDoThatException;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import org.grlea.log.SimpleLogger;
import javatools.parsers.PlingStemmer;

@Entity
public class TableDefn implements TableInfo {

	protected TableDefn() {
	}

	public TableDefn(String internalTableName, String tableName, String tableDesc) {
		this.setTableName(tableName);
		// store db friendly version of the table name
		if (internalTableName == null) {
			this.setInternalTableName(RandomString.generate());
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
	// w.r.t. equals and hashCode
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
		// reset other forms of name (they'll be regenerated when the relevant
		// getters are called)
		this.simpleName = null;
		this.singularName = null;
	}

	public synchronized void setTableDescription(String tableDesc) {
		this.tableDesc = tableDesc;
	}

	public synchronized String getTableName() {
		return this.tableName;
	}

	@Transient
	public String getSimpleName() {
		if (this.simpleName != null) {
			return this.simpleName;
		}
		this.simpleName = Naming.getSimpleName(this.getTableName());
		return this.simpleName;
	}

	@Transient
	public String getSingularName() {
		if (this.singularName != null) {
			return this.singularName;
		}
		String simpleName = this.getSimpleName();
		if (simpleName.toLowerCase().endsWith("premises") || simpleName.toLowerCase().endsWith("expenses")) {
			// PlingStemmer doesn't return the correct singular form of
			// "premises" which is also "premises"
			// TODO: the next version of PlingStemmer corrects this, replace when released
			this.singularName = simpleName;
		} else if (PlingStemmer.isSingular(simpleName)) {
			this.singularName = simpleName;
		} else {
			this.singularName = PlingStemmer.stem(simpleName);
		}
		return this.singularName;
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
		// just ensure everything's right - in old tables, field indexes may be
		// corrupt
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
	public synchronized BaseField getField(String fieldID) throws ObjectNotFoundException {
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
	public synchronized BaseReportInfo getReport(String reportID) throws ObjectNotFoundException {
		Set<BaseReportInfo> reports = this.getReportsCollection();
		for (BaseReportInfo report : reports) {
			if (report.getInternalReportName().equals(reportID)) {
				return report;
			}
		}
		// Report with that internal name doesn't exist
		// Try treating it as a public name
		String lowerReportID = reportID.toLowerCase();
		for (BaseReportInfo report : reports) {
			logger.debug("Comparing " + report.getReportName().toLowerCase() + " with " + lowerReportID);
			if (report.getReportName().toLowerCase().equals(lowerReportID)) {
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
		if (this.equals(table.getFormTable())) {
			return true;
		}
		return false;
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

	public void setRecordsLockable(Boolean lockable) {
		this.lockable = lockable;
	}

	public Boolean getRecordsLockable() {
		return this.lockable;
	}

	public void setTableFormPublic(boolean tableFormPublic) {
		this.tableFormPublic = tableFormPublic;
	}

	public boolean getTableFormPublic() {
		return this.tableFormPublic;
	}
	
	public String getEmail() {
		return this.email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}

	public void setFormStyle(FormStyle formStyle) {
		this.formStyle = formStyle;
	}
	
	@Enumerated(EnumType.STRING)
	public FormStyle getFormStyle() {
		return this.formStyle;
	}
	
	@Transient
	public SortedSet<FormTabInfo> getFormTabs() {
		return Collections.unmodifiableSortedSet(new TreeSet<FormTabInfo>(this.getFormTabsDirect()));
	}
	
	@OneToMany(mappedBy="parentTable", targetEntity = FormTab.class, cascade = CascadeType.ALL)
	private Set<FormTabInfo> getFormTabsDirect() {
		return this.formTabs;
	}
	
	private void setFormTabsDirect(Set<FormTabInfo> formTabs) {
		this.formTabs = formTabs;
	}
	
	public void addFormTab(FormTabInfo formTab) {
		this.getFormTabsDirect().add(formTab);
	}
	
	public void removeFormTab(FormTabInfo formTab) {
		this.getFormTabsDirect().remove(formTab);
	}
	
	@ManyToOne(targetEntity = TableDefn.class)
	public TableInfo getFormTable() {
		return this.formTable;
	}
	
	public void setFormTable(TableInfo formTable) {
		this.formTable = formTable;
	}
	
	public boolean getAllowAutoDelete() {
		return this.allowAutoDelete;
	}
	
	public void setAllowAutoDelete(boolean allowAutoDelete) {
		this.allowAutoDelete = allowAutoDelete;
	}
	
	/**
	 * Provide a natural sort order by table name case insensitively
	 */
	public int compareTo(TableInfo otherTable) {
		if (this == otherTable) {
			return 0;
		}
		// For performance, compare first on item most likely to differ
		int comparison = this.getTableName().toLowerCase()
				.compareTo(otherTable.getTableName().toLowerCase());
		if (comparison != 0) {
			return comparison;
		}
		return this.getInternalTableName().compareTo(otherTable.getInternalTableName());
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

	private boolean tableFormPublic = false;
	
	private String email = null;
	
	private FormStyle formStyle = FormStyle.SINGLE_COLUMN;
	
	private Set<FormTabInfo> formTabs = new HashSet<FormTabInfo>();
	
	private TableInfo formTable = null;
	
	private boolean allowAutoDelete = false;

	private volatile String simpleName = null;

	private volatile String singularName = null;

	private static final SimpleLogger logger = new SimpleLogger(TableDefn.class);
}