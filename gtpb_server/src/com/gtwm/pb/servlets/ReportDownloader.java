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
package com.gtwm.pb.servlets;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Map;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.DataRowInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryAggregateInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryDataRowInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryGroupingInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryDataInfo;
import com.gtwm.pb.model.interfaces.SessionDataInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.DataManagementInfo;
import com.gtwm.pb.model.interfaces.DataRowFieldInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.TextField;
import com.gtwm.pb.model.interfaces.fields.RelationField;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.AgileBaseException;
import com.gtwm.pb.util.ObjectNotFoundException;

public final class ReportDownloader extends HttpServlet {

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		this.databaseDefn = (DatabaseInfo) servletContext
				.getAttribute("com.gtwm.pb.servlets.databaseDefn");
		if (this.databaseDefn == null) {
			throw new ServletException(
					"Error starting ReportDownloader servlet. No databaseDefn object in the servlet context");
		}
	}

	public void destroy() {
		super.destroy();
		// release memory for good measure
		this.databaseDefn = null;
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException {
		HttpSession session = request.getSession();
		SessionDataInfo sessionData = (SessionDataInfo) session
				.getAttribute("com.gtwm.pb.servlets.sessionData");
		if (sessionData == null) {
			throw new ServletException("No session found");
		}
		ByteArrayOutputStream spreadsheetOutputStream = null;
		try {
			CompanyInfo company = this.databaseDefn.getAuthManager().getCompanyForLoggedInUser(
					request);
			AppUserInfo user = this.databaseDefn.getAuthManager().getUserByUserName(request,
					request.getRemoteUser());
			spreadsheetOutputStream = this.getSessionReportAsExcel(company, user, sessionData);
			response.setHeader("Cache-Control", "no-cache");
			response.setContentType("application/vnd.ms-excel");
			String filename = "";
			BaseReportInfo report = sessionData.getReport();
			if (report.equals(report.getParentTable().getDefaultReport())) {
				filename = report.getParentTable().getTableName();
			} else {
				filename = sessionData.getReport().getReportName();
			}
			filename = filename.replaceAll("\\W+", "_");
			filename = filename + ".xls";
			response.setHeader("Content-disposition", "attachment; filename=" + filename);
			response.setContentLength(spreadsheetOutputStream.size());
			ServletOutputStream sos = response.getOutputStream();
			spreadsheetOutputStream.writeTo(sos);
			sos.flush();
		} catch (IOException ioex) {
			throw new ServletException("IO exception generating spreadsheet: " + ioex);
		} catch (AgileBaseException pbex) {
			throw new ServletException("Problem generating spreadsheet: " + pbex);
		} catch (SQLException sqlex) {
			throw new ServletException("Database exception generating spreadsheet: " + sqlex);
		} finally {
			if (spreadsheetOutputStream != null) {
				spreadsheetOutputStream.reset();
			}
		}
	}

	/**
	 * Return the session report as an Excel file
	 * 
	 * @param sessionData
	 * @return
	 */
	private ByteArrayOutputStream getSessionReportAsExcel(CompanyInfo company, AppUserInfo user,
			SessionDataInfo sessionData) throws AgileBaseException, IOException, SQLException {
		BaseReportInfo report = sessionData.getReport();
		if (report == null) {
			throw new ObjectNotFoundException("No report found in the session");
		}
		// create Excel spreadsheet
		HSSFWorkbook workbook = new HSSFWorkbook();
		// the pane 2 report
		String reportName = report.getReportName();
		// Replace any invalid characters : \ / ? * [ or ]
		// http://support.microsoft.com/kb/215205
		reportName = reportName.replaceAll("[\\/\\:\\\\\\?\\*\\[\\]]", "-");
		HSSFSheet reportSheet = workbook.createSheet(reportName);
		int rowNum = 0;
		HSSFRow row;
		HSSFCell cell;
		// header
		HSSFCellStyle boldCellStyle = workbook.createCellStyle();
		HSSFFont font = workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		boldCellStyle.setFont(font);
		row = reportSheet.createRow(rowNum);
		int columnNum = 0;
		Set<ReportFieldInfo> reportFields = report.getReportFields();
		for (ReportFieldInfo reportField : reportFields) {
			cell = row.createCell(columnNum);
			cell.setCellValue(new HSSFRichTextString(reportField.getFieldName()));
			cell.setCellStyle(boldCellStyle);
			columnNum++;
		}
		// data
		rowNum++;
		DataManagementInfo dataManagement = this.databaseDefn.getDataManagement();
		List<DataRowInfo> reportDataRows = dataManagement.getReportDataRows(company, report,
				sessionData.getReportFilterValues(), false, sessionData.getReportSorts(), -1);
		String fieldValue = "";
		for (DataRowInfo dataRow : reportDataRows) {
			Map<BaseField, DataRowFieldInfo> dataRowFieldMap = dataRow.getDataRowFields();
			row = reportSheet.createRow(rowNum);
			columnNum = 0;
			for (ReportFieldInfo reportField : reportFields) {
				BaseField field = reportField.getBaseField();
				if (field instanceof TextField) {
					fieldValue = dataRowFieldMap.get(field).getKeyValue();
				} else {
					fieldValue = dataRowFieldMap.get(field).getDisplayValue();
				}
				int cellType = HSSFCell.CELL_TYPE_STRING;
				if (field.getDbType().equals(DatabaseFieldType.FLOAT)
						|| field.getDbType().equals(DatabaseFieldType.INTEGER)
						|| field.getDbType().equals(DatabaseFieldType.SERIAL)) {
					cellType = HSSFCell.CELL_TYPE_NUMERIC;
				}
				row.createCell(columnNum, cellType)
						.setCellValue(new HSSFRichTextString(fieldValue));
				columnNum++;
			}
			rowNum++;
		}
		// Export info worksheet
		addReportMetaDataWorksheet(company, user, sessionData, report, workbook);
		// one worksheet for each of the report summaries
		for (ReportSummaryInfo savedReportSummary : report.getSavedReportSummaries()) {
			this.addSummaryWorksheet(company, sessionData, savedReportSummary, workbook);
		}
		// the default summary
		ReportSummaryInfo reportSummary = report.getReportSummary();
		Set<ReportSummaryAggregateInfo> aggregateFunctions = reportSummary.getAggregateFunctions();
		Set<ReportSummaryGroupingInfo> groupings = reportSummary.getGroupings();
		if ((groupings.size() > 0) || (aggregateFunctions.size() > 0)) {
			this.addSummaryWorksheet(company, sessionData, reportSummary, workbook);
		}
		// write to output
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		return outputStream;
	}

	/**
	 * Add a sheet with export information to the workbook
	 */
	private static void addReportMetaDataWorksheet(CompanyInfo company, AppUserInfo user,
			SessionDataInfo sessionData, BaseReportInfo report, HSSFWorkbook workbook) {
		String title = "Export information";
		HSSFSheet infoSheet;
		try {
			infoSheet = workbook.createSheet(title);
		} catch (IllegalArgumentException iaex) {
			// Just in case there happens to be a report called 'Export
			// information'.
			// The sheet name must be unique
			infoSheet = workbook.createSheet(title + " " + report.getInternalReportName());
		}
		HSSFRow row = infoSheet.createRow(0);
		HSSFCell cell = row.createCell(1);
		cell.setCellValue(new HSSFRichTextString("Export from www.agilebase.co.uk"));
		row = infoSheet.createRow(2);
		cell = row.createCell(0);
		cell.setCellValue(new HSSFRichTextString("Company"));
		cell = row.createCell(1);
		cell.setCellValue(new HSSFRichTextString(company.getCompanyName()));
		row = infoSheet.createRow(3);
		cell = row.createCell(0);
		cell.setCellValue(new HSSFRichTextString("Module"));
		cell = row.createCell(1);
		ModuleInfo module = report.getModule();
		if (module != null) {
			cell.setCellValue(new HSSFRichTextString(report.getModule().getModuleName()));
		} else {
			cell.setCellValue(new HSSFRichTextString(""));
		}
		row = infoSheet.createRow(4);
		cell = row.createCell(0);
		cell.setCellValue(new HSSFRichTextString("Report"));
		cell = row.createCell(1);
		cell.setCellValue(new HSSFRichTextString(report.getReportName()));
		row = infoSheet.createRow(5);
		cell = row.createCell(0);
		cell.setCellValue(new HSSFRichTextString("Exported by"));
		cell = row.createCell(1);
		cell.setCellValue(new HSSFRichTextString(user.getForename() + " " + user.getSurname()));
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		String now = dateFormat.format(date);
		row = infoSheet.createRow(6);
		cell = row.createCell(0);
		cell.setCellValue(new HSSFRichTextString("Export time"));
		cell = row.createCell(1);
		cell.setCellValue(new HSSFRichTextString(now));
	}

	/**
	 * Add a worksheet to the report for the specified workbook
	 */
	private void addSummaryWorksheet(CompanyInfo company, SessionDataInfo sessionData,
			ReportSummaryInfo reportSummary, HSSFWorkbook workbook) throws SQLException,
			CantDoThatException {
		ReportSummaryDataInfo reportSummaryData = this.databaseDefn.getDataManagement()
				.getReportSummaryData(company, reportSummary, sessionData.getReportFilterValues(),
						false);
		if (reportSummaryData == null) {
			return;
		}
		int rowNum;
		HSSFRow row;
		HSSFCell cell;
		int columnNum;
		String fieldValue;
		HSSFSheet summarySheet;
		String summaryTitle = reportSummary.getTitle();
		if (summaryTitle == null) {
			summaryTitle = "Summary";
		} else if (summaryTitle.equals("")) {
			summaryTitle = "Summary";
		}
		// Replace any invalid characters : \ / ? * [ or ]
		// http://support.microsoft.com/kb/215205
		summaryTitle = summaryTitle.replaceAll("[\\/\\:\\\\\\?\\*\\[\\]]", "-");
		try {
			summarySheet = workbook.createSheet(summaryTitle);
		} catch (IllegalArgumentException iaex) {
			// sheet name must be unique
			summarySheet = workbook.createSheet(summaryTitle + " " + reportSummary.getId());
		}
		// header
		rowNum = 0;
		row = summarySheet.createRow(rowNum);
		columnNum = 0;
		HSSFCellStyle boldCellStyle = workbook.createCellStyle();
		HSSFFont font = workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		boldCellStyle.setFont(font);
		Set<ReportSummaryAggregateInfo> aggregateFunctions = reportSummary.getAggregateFunctions();
		Set<ReportSummaryGroupingInfo> groupings = reportSummary.getGroupings();
		for (ReportSummaryGroupingInfo grouping : groupings) {
			BaseField groupingBaseField = grouping.getGroupingReportField().getBaseField();
			if (groupingBaseField instanceof RelationField) {
				fieldValue = groupingBaseField.getTableContainingField() + ": "
						+ ((RelationField) groupingBaseField).getDisplayField();
			} else {
				fieldValue = groupingBaseField.getFieldName();
			}
			cell = row.createCell(columnNum);
			cell.setCellValue(new HSSFRichTextString(fieldValue));
			cell.setCellStyle(boldCellStyle);
			columnNum++;
		}
		for (ReportSummaryAggregateInfo aggregateFunction : aggregateFunctions) {
			fieldValue = aggregateFunction.toString();
			cell = row.createCell(columnNum);
			cell.setCellValue(new HSSFRichTextString(fieldValue));
			cell.setCellStyle(boldCellStyle);
			columnNum++;
		}
		List<ReportSummaryDataRowInfo> reportSummaryDataRows = reportSummaryData
				.getReportSummaryDataRows();
		rowNum++;
		for (ReportSummaryDataRowInfo summaryDataRow : reportSummaryDataRows) {
			row = summarySheet.createRow(rowNum);
			columnNum = 0;
			for (ReportSummaryGroupingInfo grouping : groupings) {
				fieldValue = summaryDataRow.getGroupingValue(grouping);
				row.createCell(columnNum).setCellValue(new HSSFRichTextString(fieldValue));
				columnNum++;
			}
			for (ReportSummaryAggregateInfo aggregateFunction : aggregateFunctions) {
				fieldValue = summaryDataRow.getAggregateValue(aggregateFunction).toString();
				row.createCell(columnNum, HSSFCell.CELL_TYPE_NUMERIC).setCellValue(
						new HSSFRichTextString(fieldValue));
				columnNum++;
			}
			rowNum++;
		}
	}

	private DatabaseInfo databaseDefn = null;
}
