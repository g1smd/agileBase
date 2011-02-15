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
package com.gtwm.pb.model.manageSchema.fields;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;
import javax.persistence.ManyToOne;
import javax.persistence.Column;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.manageSchema.TableDefn;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.Naming;

/**
 * An abtract class that provides basic field functionality<br>
 * All concrete field types will extend this class, for example types that
 * represent dropdowns, numbers or text. Subclasses must call various methods in
 * this class at construction time, at least setInternalFieldName() to give the
 * field an internal name
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractField implements BaseField {

	@Transient
	public void setFieldName(String fieldName) {
		this.setFieldNameDirect(fieldName);
	}

	@Transient
	public String getFieldName() {
		return Naming.makeValidXML(this.getFieldNameDirect());
	}

	/**
	 * For hibernate: we want to persist this.fieldName directly, not
	 * Naming.makeValidXML(this.getFieldNameDirect())
	 */
	private String getFieldNameDirect() {
		return this.fieldName;
	}

	private void setFieldNameDirect(String fieldName) {
		this.fieldName = fieldName;
	}

	@Id
	public String getInternalFieldName() {
		return this.internalFieldName;
	}

	/**
	 * For use by subclasses
	 */
	protected void setInternalFieldName(String internalFieldName) {
		this.internalFieldName = internalFieldName;
	}

	public void setFieldDescription(String fieldDesc) {
		// Velocity templating engine doesn't like nulls
		if (fieldDesc == null) {
			this.fieldDesc = "";
		} else {
			this.fieldDesc = fieldDesc;
		}
	}

	public String getFieldDescription() {
		return this.fieldDesc;
	}

	@Transient
	public void setNotNull(boolean fieldNotNull) throws CantDoThatException {
		this.setNotNullDirect(fieldNotNull);
	}

	/**
	 * Allow subclasses to override default behaviour and set a field not null
	 * even if it doesn't have a default value
	 */
	protected void setNotNullDirect(Boolean fieldNotNull) {
		this.fieldNotNull = fieldNotNull;
	}

	@Transient
	public boolean getNotNull() {
		return this.getNotNullDirect();
	}

	private Boolean getNotNullDirect() {
		return this.fieldNotNull;
	}

	/*
	 * Subclasses may throw CantDoThatException (non-Javadoc)
	 * 
	 * @see com.gtwm.pb.model.interfaces.fields.BaseField#setUnique(boolean)
	 */
	public void setUnique(Boolean fieldUnique) throws CantDoThatException {
		this.fieldUnique = fieldUnique;
	}

	// Override default column name 'unique' since this is an SQL reserved word
	// and will cause Hibernate problems
	@Column(name = "fieldUnique")
	public Boolean getUnique() {
		return this.fieldUnique;
	}

	public void setTableContainingField(TableInfo tableContainingField) {
		this.tableContainingField = tableContainingField;
	}

	@ManyToOne(targetEntity = TableDefn.class)
	// Other side of table.getFields()
	// @OneToOne(mappedBy="primaryKey", targetEntity=TableDefn.class) // Other
	// side of table.getPrimaryKey()
	public TableInfo getTableContainingField() {
		return this.tableContainingField;
	}

	/**
	 * Provide a natural sort order by parent table then field index then field
	 * name + internal name
	 */
	public int compareTo(BaseField otherField) {
		if (this == otherField) {
			return 0;
		}
		TableInfo otherTable = otherField.getTableContainingField();
		int tableCompare = this.getTableContainingField().compareTo(otherTable);
		if (tableCompare != 0) {
			return tableCompare;
		}
		Integer otherFieldIndex = otherField.getFieldIndex();
		if (otherFieldIndex != null) {
			// Hidden fields should come at the end
			if (otherField.getHidden()) {
				otherFieldIndex += 10000;
			}
			Integer thisFieldIndex = this.getFieldIndex();
			if (thisFieldIndex != null) {
				if (this.getHidden()) {
					thisFieldIndex += 10000;
				}
				int indexCompare = thisFieldIndex.compareTo(otherFieldIndex);
				if (indexCompare != 0) {
					return indexCompare;
				}
			}
		}
		String otherFieldName = otherField.getFieldName();
		String otherFieldInternalName = otherField.getInternalFieldName();
		return (this.getFieldName() + this.getInternalFieldName())
				.compareToIgnoreCase(otherFieldName + otherFieldInternalName);
	}

	/**
	 * equals is based on parent table internal name and internal field name
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		BaseField otherField = (BaseField) obj;
		if (!otherField.getTableContainingField().equals(this.getTableContainingField())) {
			return false;
		}
		return (this.getInternalFieldName()).equals(otherField.getInternalFieldName());
	}

	/**
	 * Hash code consistent with our equals
	 */
	public int hashCode() {
		if (this.hashCode == 0) {
			int result = 17;
			result = 37 * result + this.getTableContainingField().hashCode();
			result = 37 * result + this.getInternalFieldName().hashCode();
			this.hashCode = result;
		}
		return this.hashCode;
	}

	public Boolean getHidden() {
		return this.hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	public void setFieldIndex(Integer fieldIndex) {
		this.fieldIndex = fieldIndex;
	}

	public Integer getFieldIndex() {
		return this.fieldIndex;
	}

	@Transient
	public boolean hasDefault() {
		return this.getDefaultDefined();
	}

	private Boolean getDefaultDefined() {
		return this.defaultDefined;
	}

	/**
	 * To be used by subclasses
	 */
	protected void setDefaultDefined(Boolean defaultDefined) {
		this.defaultDefined = defaultDefined;
	}

	public String toString() {
		return this.getFieldName();
	}

	private String fieldName = "";

	private String internalFieldName = null;

	private String fieldDesc = "";

	private TableInfo tableContainingField = null;

	private Boolean fieldNotNull = false;

	private Boolean fieldUnique = false;

	private Boolean hidden = false;

	private Boolean defaultDefined = false;

	private Integer fieldIndex = 0;

	private volatile int hashCode = 0;

	private long id;

	private static final SimpleLogger logger = new SimpleLogger(AbstractField.class);
}