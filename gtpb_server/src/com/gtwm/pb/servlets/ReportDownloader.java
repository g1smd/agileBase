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
import java.util.List;
import java.util.Set;
import java.util.Map;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.DataRowInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryAggregateInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryDataRowInfo;
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
import com.gtwm.pb.util.PortalBaseException;
import com.gtwm.pb.util.ObjectNotFoundException;

public class ReportDownloader extends HttpServlet {

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
			spreadsheetOutputStream = this.getSessionReportAsExcel(company, sessionData);
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
		} catch (PortalBaseException pbex) {
			throw new ServletException("portalBase exception generating spreadsheet: " + pbex);
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
	private ByteArrayOutputStream getSessionReportAsExcel(CompanyInfo company,
			SessionDataInfo sessionData) throws PortalBaseException, IOException, SQLException {
		BaseReportInfo report = sessionData.getReport();
		if (report == null) {
			throw new ObjectNotFoundException("No report found in the session");
		}
		// create Excel spreadsheet
		HSSFWorkbook workbook = new HSSFWorkbook();
		// the pane 2 report
		HSSFSheet reportSheet = workbook.createSheet(report.getReportName());
		// exported from portalBase message
		int rowNum = 0;
		HSSFRow row = reportSheet.createRow(rowNum);
		HSSFCell cell = row.createCell((short) 1);
		cell.setCellValue(new HSSFRichTextString("Exported from the " + company
				+ " portalBase report '" + report.getModule() + " - " + report
				+ "'. Live data at www.gtportalbase.com/start"));
		rowNum = rowNum + 2;
		// header
		HSSFCellStyle boldCellStyle = workbook.createCellStyle();
		HSSFFont font = workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		boldCellStyle.setFont(font);
		row = reportSheet.createRow(rowNum);
		int columnNum = 0;
		Set<ReportFieldInfo> reportFields = report.getReportFields();
		for (ReportFieldInfo reportField : reportFields) {
			cell = row.createCell((short) columnNum);
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
				row.createCell((short) columnNum, cellType).setCellValue(
						new HSSFRichTextString(fieldValue));
				columnNum++;
			}
			rowNum++;
		}
		// the summary pane
		ReportSummaryInfo reportSummary = report.getReportSummary();
		Set<ReportSummaryAggregateInfo> aggregateFunctions = reportSummary.getAggregateFunctions();
		List<ReportFieldInfo> groupingReportFields = reportSummary.getGroupingReportFields();
		boolean needSummary = (groupingReportFields.size() > 0) || (aggregateFunctions.size() > 0);
		if (needSummary) {
			HSSFSheet summarySheet = workbook.createSheet("Summary");
			// header
			rowNum = 0;
			row = summarySheet.createRow(rowNum);
			columnNum = 0;
			for (ReportFieldInfo groupingReportField : groupingReportFields) {
				BaseField groupingBaseField = groupingReportField.getBaseField();
				if (groupingBaseField instanceof RelationField) {
					fieldValue = groupingBaseField.getTableContainingField() + ": "
							+ ((RelationField) groupingBaseField).getDisplayField();
				} else {
					fieldValue = groupingBaseField.getFieldName();
				}
				cell = row.createCell((short) columnNum);
				cell.setCellValue(new HSSFRichTextString(fieldValue));
				cell.setCellStyle(boldCellStyle);
				columnNum++;
			}
			for (ReportSummaryAggregateInfo aggregateFunction : aggregateFunctions) {
				fieldValue = aggregateFunction.toString();
				cell = row.createCell((short) columnNum);
				cell.setCellValue(new HSSFRichTextString(fieldValue));
				cell.setCellStyle(boldCellStyle);
				columnNum++;
			}
			// summary data
			ReportSummaryDataInfo reportSummaryData = this.databaseDefn.getDataManagement()
					.getReportSummaryData(company, report, sessionData.getReportFilterValues());
			List<ReportSummaryDataRowInfo> reportSummaryDataRows = reportSummaryData
					.getReportSummaryDataRows();
			rowNum++;
			for (ReportSummaryDataRowInfo summaryDataRow : reportSummaryDataRows) {
				row = summarySheet.createRow(rowNum);
				columnNum = 0;
				for (ReportFieldInfo groupingReportField : groupingReportFields) {
					fieldValue = summaryDataRow.getGroupingValue(groupingReportField);
					row.createCell((short) columnNum).setCellValue(
							new HSSFRichTextString(fieldValue));
					columnNum++;
				}
				for (ReportSummaryAggregateInfo aggregateFunction : aggregateFunctions) {
					fieldValue = summaryDataRow.getAggregateValue(aggregateFunction).toString();
					row.createCell((short) columnNum, HSSFCell.CELL_TYPE_NUMERIC).setCellValue(
							new HSSFRichTextString(fieldValue));
					columnNum++;
				}
				rowNum++;
			}
		}
		// write to output
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		return outputStream;
	}

	private DatabaseInfo databaseDefn = null;
}
