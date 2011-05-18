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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import javax.sql.DataSource;
import org.grlea.log.SimpleLogger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.TextField;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.FieldTypeDescriptorInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.ReportDataInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor;
import com.gtwm.pb.model.manageSchema.BooleanFieldDescriptorOption.PossibleBooleanOptions;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.FieldPrintoutSetting;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.PossibleListOptions;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.TextContentSizes;
import com.gtwm.pb.model.manageSchema.TextFieldDescriptorOption.PossibleTextOptions;
import com.gtwm.pb.model.manageData.ReportData;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Enumerations.TextCase;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.util.AppProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A basic text field in a database table
 */
@Entity
public class TextFieldDefn extends AbstractField implements TextField {

	protected TextFieldDefn() {
	}

	public TextFieldDefn(DataSource dataSource, TableInfo tableContainingField,
			String internalFieldName, String fieldName, String fieldDesc, boolean unique,
			boolean notNull, String defaultValue, boolean notApplicable,
			String notApplicableDescription, String notApplicableValue, boolean usesLookup,
			boolean hidden) throws CantDoThatException {
		checkOptionsConsistency(this.contentSize, usesLookup, unique);
		this.setDataSource(dataSource);
		super.setTableContainingField(tableContainingField);
		if (internalFieldName == null) {
			super.setInternalFieldName((new RandomString()).toString());
		} else {
			super.setInternalFieldName(internalFieldName);
		}
		super.setFieldName(fieldName);
		super.setFieldDescription(fieldDesc);
		super.setUnique(unique);
		this.setDefault(defaultValue);
		super.setNotNull(notNull);
		this.setNotApplicable(notApplicable);
		if (notApplicable) {
			this.setNotApplicableDescriptionDirect(notApplicableDescription);
			this.setNotApplicableValueDirect(notApplicableValue);
		}
		this.setUsesLookup(usesLookup);
		super.setHidden(hidden);
	}

	public boolean allowNotApplicable() {
		return this.getNotApplicable();
	}

	private Boolean getNotApplicable() {
		return this.notApplicable;
	}

	private void setNotApplicable(Boolean notApplicable) {
		this.notApplicable = notApplicable;
	}

	@Transient
	public String getNotApplicableDescription() throws CantDoThatException {
		if (!this.getNotApplicable()) {
			throw new CantDoThatException("The not applicable property is not active for field "
					+ this.getFieldName());
		}
		return this.getNotApplicableDescriptionDirect();
	}

	private void setNotApplicableDescriptionDirect(String notApplicableDescription) {
		this.notApplicableDescription = notApplicableDescription;
	}

	private String getNotApplicableDescriptionDirect() {
		return this.notApplicableDescription;
	}

	private void setNotApplicableValueDirect(String notApplicableValue) {
		this.notApplicableValue = notApplicableValue;
	}

	@Transient
	public String getNotApplicableValue() throws CantDoThatException {
		if (!this.getNotApplicable()) {
			throw new CantDoThatException("The not applicable property is not active for field "
					+ this.getFieldName());
		}
		return this.getNotApplicableValueDirect();
	}

	private String getNotApplicableValueDirect() {
		return this.notApplicableValue;
	}

	@Transient
	public DatabaseFieldType getDbType() {
		return DatabaseFieldType.VARCHAR;
	}

	@Transient
	public FieldCategory getFieldCategory() {
		return FieldCategory.TEXT;
	}

	@Transient
	public FieldTypeDescriptorInfo getFieldDescriptor() throws CantDoThatException,
			CodingErrorException {
		FieldTypeDescriptorInfo fieldDescriptor = new FieldTypeDescriptor(FieldCategory.TEXT);
		try {
			fieldDescriptor.setListOptionSelectedItem(PossibleListOptions.TEXTCONTENTSIZE,
					String.valueOf(this.getContentSize()));
			fieldDescriptor.setBooleanOptionState(PossibleBooleanOptions.UNIQUE, super.getUnique());
			fieldDescriptor.setBooleanOptionState(PossibleBooleanOptions.MANDATORY,
					super.getNotNull());
			fieldDescriptor.setBooleanOptionState(PossibleBooleanOptions.USELOOKUP,
					this.usesLookup());
			if (this.hasDefault()) {
				if (this.usesLookup()) {
					fieldDescriptor.setTextOptionValue(PossibleTextOptions.DEFAULTVALUE,
							this.getDefaultCSV());
				} else {
					fieldDescriptor.setTextOptionValue(PossibleTextOptions.DEFAULTVALUE,
							this.getDefault());
				}
			}
			TextCase textCase = this.getTextCase();
			if (textCase == null) {
				textCase = TextCase.ANY;
			}
			fieldDescriptor.setListOptionSelectedItem(PossibleListOptions.TEXTCASE,
					textCase.toString());
			FieldPrintoutSetting printoutSetting = this.getPrintoutSetting();
			fieldDescriptor.setListOptionSelectedItem(PossibleListOptions.PRINTFORMAT, printoutSetting.name());
		} catch (ObjectNotFoundException onfex) {
			throw new CantDoThatException("Internal error setting up " + this.getClass()
					+ " field descriptor", onfex);
		}
		return fieldDescriptor;
	}

	public synchronized void setDefault(String defaultValue) throws CantDoThatException {
		super.setDefaultDefined((defaultValue != null));
		this.setDefaultDirect(defaultValue);
	}

	@Transient
	public synchronized String getDefaultCSV() throws CantDoThatException {
		if (!this.usesLookup()) {
			throw new CantDoThatException("Can't get default CSV - this field isn't a lookup");
		}
		String defaultCSV = this.getDefaultDirect();
		TextCase textCase = this.getTextCase();
		if (textCase != null) {
			defaultCSV = textCase.transform(defaultCSV);
		}
		return defaultCSV;
	}

	@Transient
	public synchronized String getDefault() {
		String defaultText = this.getDefaultDirect();
		if (defaultText == null) {
			return defaultText;
		}
		TextCase textCase = this.getTextCase();
		if (textCase != null) {
			defaultText = textCase.transform(defaultText);
		}
		// if a lookup with a CSV, return the first value
		// User can use a leading comma to return an empty value
		if (this.usesLookup() && defaultText.contains(",")) {
			List<String> items = Arrays.asList(defaultText.split(","));
			// The default text may be *only* commas
			if (items.size() > 0) {
				return items.get(0);
			} else {
				return null;
			}
		}
		return defaultText;
	}

	public synchronized void clearDefault() {
		super.setDefaultDefined(false);
		this.setDefaultDirect(null);
	}

	private void setDefaultDirect(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	private String getDefaultDirect() {
		return this.defaultValue;
	}

	public synchronized void setContentSize(Integer maxChars) throws CantDoThatException {
		checkOptionsConsistency(maxChars, this.getUsesLookupDirect(), this.getUnique());
		this.setContentSizeDirect(maxChars);
	}

	private void setContentSizeDirect(Integer maxChars) {
		this.contentSize = maxChars;
	}

	@Transient
	public synchronized Integer getContentSize() {
		return this.getContentSizeDirect();
	}

	private Integer getContentSizeDirect() {
		return this.contentSize;
	}

	public void setUsesLookup(Boolean usesLookup) throws CantDoThatException {
		checkOptionsConsistency(this.getContentSize(), usesLookup, this.getUnique());
		this.setUsesLookupDirect(usesLookup);
	}

	private void setUsesLookupDirect(Boolean usesLookup) {
		this.usesLookup = usesLookup;
	}

	private Boolean getUsesLookupDirect() {
		return this.usesLookup;
	}

	@Transient
	public boolean usesLookup() {
		return this.getUsesLookupDirect();
	}

	public void setUnique(Boolean fieldUnique) throws CantDoThatException {
		checkOptionsConsistency(this.getContentSize(), this.getUsesLookupDirect(), fieldUnique);
		super.setUnique(fieldUnique);
	}

	private void logAllItemsCacheStats() {
		int allItemsCacheViews = this.allItemsCacheHits + this.allItemsCacheMisses;
		if (allItemsCacheViews % 1000 == 0) {
			logger.info(this.toString() + " lookup items cache hits = " + this.allItemsCacheHits
					+ ", misses = " + this.allItemsCacheMisses);
			this.allItemsCacheHits = 0;
			this.allItemsCacheMisses = 0;
		}
	}

	private void logFilteredItemsCacheStats() {
		int filteredItemsCacheViews = this.filteredItemsCacheHits + this.filteredItemsCacheMisses;
		if (filteredItemsCacheViews % 500 == 0) {
			logger.info(this.toString() + " lookup filtered items cache hits = "
					+ this.filteredItemsCacheHits + ", misses = " + this.filteredItemsCacheMisses);
			this.filteredItemsCacheHits = 0;
			this.filteredItemsCacheMisses = 0;
			// clear memory once in a while as well
			this.filteredItemsCache = new HashMap<String, SortedSet<String>>();
		}
	}

	@Transient
	// synchronized because it uses a cache
	public synchronized SortedSet<String> getItems() throws SQLException, CantDoThatException,
			CodingErrorException {
		if ((System.currentTimeMillis() - this.allItemsLastCacheTime) < AppProperties.lookupCacheTime) {
			if (this.allItemsCache.size() > 0) {
				this.allItemsCacheHits += 1;
				this.logAllItemsCacheStats();
				return this.allItemsCache;
			}
		}
		SortedSet<String> items = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		TextCase textCase = this.getTextCase();
		if (textCase == null) {
			textCase = TextCase.ANY;
		}
		// Add CSV from the default items, if there is one
		String defaultText = this.getDefaultDirect();
		if (defaultText != null) {
			if (defaultText.contains(",")) {
				defaultText = textCase.transform(defaultText);
				List<String> defaultItems = Arrays.asList(defaultText.split(","));
				for (String defaultItem : defaultItems) {
					if (!defaultItem.trim().equals("")) {
						items.add(defaultItem.trim());
					}
				}
			}
		}
		String sqlRepresentation = textCase.getSqlRepresentation();
		String SQLCode = "SELECT DISTINCT " + sqlRepresentation + "(" + this.getInternalFieldName()
				+ ") FROM " + this.getTableContainingField().getInternalTableName();
		Connection conn = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			ResultSet results = statement.executeQuery();
			while (results.next()) {
				String item = results.getString(1);
				if (item != null) {
					items.add(item.trim());
				}
			}
			results.close();
			statement.close();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		this.allItemsCache = items;
		this.allItemsLastCacheTime = System.currentTimeMillis();
		this.allItemsCacheMisses += 1;
		this.logAllItemsCacheStats();
		return items;
	}

	public SortedSet<String> getItems(BaseReportInfo report, Map<BaseField, String> filterValues)
			throws SQLException, CantDoThatException, CodingErrorException {
		// filterId used for caching items for this particular filter
		String filterId = report.getInternalReportName();
		for (Map.Entry<BaseField, String> filterValueEntry : filterValues.entrySet()) {
			filterId += filterValueEntry.getKey().getInternalFieldName();
			filterId += filterValueEntry.getValue();
		}
		if ((System.currentTimeMillis() - this.filteredItemsLastCacheTime) < AppProperties.lookupCacheTime) {
			SortedSet<String> filteredItems = this.filteredItemsCache.get(filterId);
			if (filteredItems != null) {
				this.filteredItemsCacheHits += 1;
				this.logFilteredItemsCacheStats();
				return filteredItems;
			}
		}
		Connection conn = null;
		SortedSet<String> items = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		// check that this field is in the report
		boolean fieldFound = false;
		SortedSet<ReportFieldInfo> reportFields = report.getReportFields();
		for (ReportFieldInfo reportField : reportFields) {
			if (reportField.getBaseField().equals(this)) {
				fieldFound = true;
				break;
			}
		}
		if (!fieldFound) {
			return items;
		}
		try {
			TextCase textCase = this.getTextCase();
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			ReportDataInfo reportData = new ReportData(conn, report, false, false);
			// Generates a SELECT DISTINCT on this field including filterValues
			// in the WHERE clause
			Map<BaseField, Boolean> emptySorts = new HashMap<BaseField, Boolean>();
			PreparedStatement statement = reportData.getReportSqlPreparedStatement(conn,
					filterValues, false, emptySorts, -1, this);
			ResultSet results = statement.executeQuery();
			while (results.next()) {
				String item = results.getString(1);
				if (item != null) {
					if (textCase == null) {
						items.add(item);
					} else {
						items.add(textCase.transform(item));
					}
				}
			}
			results.close();
			statement.close();
		} catch (SQLException sqlex) {
			// catch exception where field is not included
			// within report and simply return an empty tree
			// TODO: specifically check for this before opening a connection,
			// rather than catching all SQL errors
			logger.warn(sqlex.toString() + ". Probably occurred because field " + this
					+ " isn't in report " + report + ", in which case it's nothing to worry about");
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		this.filteredItemsCache.put(filterId, items);
		this.filteredItemsCacheMisses += 1;
		this.filteredItemsLastCacheTime = System.currentTimeMillis();
		this.logFilteredItemsCacheStats();
		return items;
	}

	/**
	 * Don't use in code. Only to be used from the DatabaseDefn constructor,
	 * hence not in interface
	 */
	public void setDataSource(DataSource dataSource) throws CantDoThatException {
		if (dataSource == null) {
			throw new CantDoThatException(
					"Can't set the data source to null, that's not very useful");
		}
		this.dataSource = dataSource;
	}

	/**
	 * Check that field options make sense, e.g. a unique field can't use a
	 * lookup
	 * 
	 * @throws CantDoThatException
	 *             if options aren't consistent with each other
	 */
	private static void checkOptionsConsistency(int contentSize, boolean usesLookup, boolean unique)
			throws CantDoThatException {
		if (contentSize >= TextContentSizes.FEW_PARAS.getNumChars()) {
			if (usesLookup) {
				throw new CantDoThatException("A large text field can't use a lookup");
			}
			if (unique) {
				throw new CantDoThatException("A large text field can't unique");
			}
		} else if (usesLookup && unique) {
			throw new CantDoThatException("A unique field can't use a lookup");
		}
	}

	@Enumerated(EnumType.STRING)
	public TextCase getTextCase() {
		return this.textCase;
	}

	public void setTextCase(TextCase textCase) {
		this.textCase = textCase;
	}

	private TextCase textCase = TextCase.ANY;

	private String defaultValue = null;

	private Integer contentSize = TextContentSizes.FEW_WORDS.getNumChars();

	private Boolean notApplicable = false;

	private String notApplicableDescription = "Not applicable";

	private String notApplicableValue = "NOT APPLICABLE";

	private Boolean usesLookup = false;

	private SortedSet<String> allItemsCache = new TreeSet<String>();

	private Map<String, SortedSet<String>> filteredItemsCache = new HashMap<String, SortedSet<String>>();

	long allItemsLastCacheTime = 0;

	long filteredItemsLastCacheTime = 0;

	int allItemsCacheHits = 0;

	int allItemsCacheMisses = 0;

	int filteredItemsCacheHits = 0;

	int filteredItemsCacheMisses = 0;

	private transient DataSource dataSource = null;

	private static final SimpleLogger logger = new SimpleLogger(TextFieldDefn.class);

	/**
	 * Can pass this constant as an argument when constructing this object. Just
	 * means arguments are more readable than (true, false, false, true, ...)
	 */
	public static final Boolean NOT_APPLICABLE_TRUE = true;

	public static final Boolean NOT_APPLICABLE_FALSE = false;

	public static final Boolean USES_LOOKUP_TRUE = true;

	public static final Boolean USES_LOOKUP_FALSE = false;
}