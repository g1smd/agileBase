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
package com.gtwm.pb.servlets;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
// Java 7
//import java.nio.charset.Charset;
//import java.nio.file.Files;
//import java.nio.file.Path;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.grlea.log.SimpleLogger;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.auth.PrivilegeType;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.AuthManagerInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.DataRowInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.ChartAggregateInfo;
import com.gtwm.pb.model.interfaces.ChartDataRowInfo;
import com.gtwm.pb.model.interfaces.ChartGroupingInfo;
import com.gtwm.pb.model.interfaces.ChartInfo;
import com.gtwm.pb.model.interfaces.ChartDataInfo;
import com.gtwm.pb.model.interfaces.SessionDataInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.DataManagementInfo;
import com.gtwm.pb.model.interfaces.DataRowFieldInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.TextField;
import com.gtwm.pb.model.interfaces.fields.RelationField;
import com.gtwm.pb.util.Enumerations.QuickFilterType;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.AgileBaseException;
import com.gtwm.pb.util.Helpers;
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
		BaseReportInfo report = sessionData.getReport();
		String templateName = request.getParameter("template");
		if (templateName != null) {
			this.serveTemplate(request, response, report, templateName);
		} else {
			this.serveSpreadsheet(request, response, sessionData, report);
		}
	}

	private void serveTemplate(HttpServletRequest request, HttpServletResponse response,
			BaseReportInfo report, String templateName) throws ServletException {
		String rinsedTemplateName = templateName.replaceAll("\\..*$", "").replaceAll("\\W", "")
				+ ".vm";
		try {
			AuthManagerInfo authManager = this.databaseDefn.getAuthManager();
			if (!authManager.getAuthenticator().loggedInUserAllowedTo(request, PrivilegeType.MANAGE_TABLE, report.getParentTable())) {
				throw new DisallowedException(authManager.getLoggedInUser(request), PrivilegeType.MANAGE_TABLE, report.getParentTable());
			}
			CompanyInfo company = this.databaseDefn.getAuthManager().getCompanyForLoggedInUser(
					request);
			String pathString = this.databaseDefn.getDataManagement().getWebAppRoot()
					+ "WEB-INF/templates/uploads/" + company.getInternalCompanyName() + "/"
					+ report.getInternalReportName() + "/" + rinsedTemplateName;
			// Java 7
			//Path path = Paths.get(pathString);
			//List<String> lines = Files.readAllLines(path, Charset.defaultCharset());
			// Java 6
			FileReader fr = new FileReader(pathString); 
			BufferedReader br = new BufferedReader(fr); 
			List<String> lines = new LinkedList<String>();
			String s;
			while((s = br.readLine()) != null) {
				lines.add(s);
			}
			fr.close(); 
			response.setHeader("Content-disposition", "attachment; filename=" + rinsedTemplateName);
			response.setHeader("Cache-Control", "no-cache");
			response.setContentType("text/html");
			ServletOutputStream sos = response.getOutputStream();
			for (String line : lines) {
				sos.println(line);
			}
			sos.flush();
		} catch (AgileBaseException abex) {
			logger.error("Problem serving template: " + abex);
			throw new ServletException("Problem serving template: " + abex);
		} catch (IOException ioex) {
			logger.error("Problem serving template: " + ioex);
			throw new ServletException("Problem serving template: " + ioex);
		}
	}

	private void serveSpreadsheet(HttpServletRequest request, HttpServletResponse response,
			SessionDataInfo sessionData, BaseReportInfo report) throws ServletException {
		ByteArrayOutputStream spreadsheetOutputStream = null;
		try {
			TableInfo table = report.getParentTable();
			AuthManagerInfo authManager = this.databaseDefn.getAuthManager();
			if (!report.getAllowExport() && (!authManager.getAuthenticator().loggedInUserAllowedTo(request, PrivilegeType.MANAGE_TABLE, table))) {
				throw new DisallowedException(authManager.getLoggedInUser(request), PrivilegeType.MANAGE_TABLE, table);
			}
			CompanyInfo company = authManager.getCompanyForLoggedInUser(
					request);
			AppUserInfo user = authManager.getUserByUserName(request,
					request.getRemoteUser());
			logger.info("User " + user + " exporting report " + report + " from table " + table);
			spreadsheetOutputStream = this.getSessionReportAsExcel(company, user, sessionData);
			response.setHeader("Cache-Control", "no-cache");
			response.setContentType("application/vnd.ms-excel");
			String filename = "";
			if (report.equals(table.getDefaultReport())) {
				filename = table.getTableName();
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
		Workbook workbook = new SXSSFWorkbook();
		// the pane 2 report
		String reportName = report.getReportName();
		// Replace any invalid characters : \ / ? * [ or ]
		// http://support.microsoft.com/kb/215205
		reportName = reportName.replaceAll("[\\/\\:\\\\\\?\\*\\[\\]]", "-");
		Sheet reportSheet;
		try {
			reportSheet = workbook.createSheet(reportName);
		} catch (IllegalArgumentException iaex) {
			reportSheet = workbook.createSheet(reportName + " " + report.getInternalReportName());
		}
		int rowNum = 0;
		// header
		CellStyle boldCellStyle = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		boldCellStyle.setFont(font);
		Row row = reportSheet.createRow(rowNum);
		int columnNum = 0;
		Set<ReportFieldInfo> reportFields = report.getReportFields();
		for (ReportFieldInfo reportField : reportFields) {
			Cell cell = row.createCell(columnNum);
			cell.setCellValue(reportField.getFieldName());
			cell.setCellStyle(boldCellStyle);
			columnNum++;
		}
		// data
		rowNum++;
		DataManagementInfo dataManagement = this.databaseDefn.getDataManagement();
		List<DataRowInfo> reportDataRows = dataManagement.getReportDataRows(company, report,
				sessionData.getReportFilterValues(), false, sessionData.getReportSorts(), -1,
				QuickFilterType.AND, false);
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
				Cell cell;
				switch (field.getDbType()) {
				case FLOAT:
					cell = row.createCell(columnNum, Cell.CELL_TYPE_NUMERIC);
					try {
						cell.setCellValue(Double.valueOf(fieldValue.replace(",", "")));
					} catch (NumberFormatException nfex) {
						// Fall back to a string representation
						cell.setCellType(Cell.CELL_TYPE_STRING);
						cell.setCellValue(fieldValue);
					}
					break;
				case INTEGER:
				case SERIAL:
					cell = row.createCell(columnNum, Cell.CELL_TYPE_NUMERIC);
					try {
						cell.setCellValue(Integer.valueOf(fieldValue.replace(",", "")));
					} catch (NumberFormatException nfex) {
						logger.debug(nfex.toString() + ": value " + fieldValue.replace(",", ""));
						// Fall back to a string representation
						cell.setCellType(Cell.CELL_TYPE_STRING);
						cell.setCellValue(fieldValue);
					}
					break;
				case VARCHAR:
				default:
					cell = row.createCell(columnNum, Cell.CELL_TYPE_STRING);
					cell.setCellValue(Helpers.unencodeHtml(fieldValue));
					break;
				}
				columnNum++;
			}
			rowNum++;
		}
		// Export info worksheet
		addReportMetaDataWorksheet(company, user, sessionData, report, workbook);
		// one worksheet for each of the report summaries
		for (ChartInfo savedChart : report.getSavedCharts()) {
			this.addSummaryWorksheet(company, sessionData, savedChart, workbook);
		}
		// the default summary
		ChartInfo reportSummary = report.getChart();
		Set<ChartAggregateInfo> aggregateFunctions = reportSummary.getAggregateFunctions();
		Set<ChartGroupingInfo> groupings = reportSummary.getGroupings();
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
			SessionDataInfo sessionData, BaseReportInfo report, Workbook workbook) {
		String title = "Export information";
		Sheet infoSheet;
		try {
			infoSheet = workbook.createSheet(title);
		} catch (IllegalArgumentException iaex) {
			// Just in case there happens to be a report called 'Export
			// information'.
			// The sheet name must be unique
			infoSheet = workbook.createSheet(title + " " + report.getInternalReportName());
		}
		Row row = infoSheet.createRow(0);
		Cell cell = row.createCell(1);
		cell.setCellValue("Export from www.agilebase.co.uk");
		row = infoSheet.createRow(2);
		cell = row.createCell(0);
		cell.setCellValue("Company");
		cell = row.createCell(1);
		cell.setCellValue(company.getCompanyName());
		row = infoSheet.createRow(3);
		cell = row.createCell(0);
		cell.setCellValue("Module");
		cell = row.createCell(1);
		ModuleInfo module = report.getModule();
		if (module != null) {
			cell.setCellValue(report.getModule().getModuleName());
		} else {
			cell.setCellValue("");
		}
		row = infoSheet.createRow(4);
		cell = row.createCell(0);
		cell.setCellValue("Report");
		cell = row.createCell(1);
		cell.setCellValue(report.getReportName());
		row = infoSheet.createRow(5);
		cell = row.createCell(0);
		cell.setCellValue("Exported by");
		cell = row.createCell(1);
		cell.setCellValue(user.getForename() + " " + user.getSurname());
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		String now = dateFormat.format(date);
		row = infoSheet.createRow(6);
		cell = row.createCell(0);
		cell.setCellValue("Export time");
		cell = row.createCell(1);
		cell.setCellValue(now);
	}

	/**
	 * Add a worksheet to the report for the specified workbook
	 */
	private void addSummaryWorksheet(CompanyInfo company, SessionDataInfo sessionData,
			ChartInfo reportSummary, Workbook workbook) throws SQLException,
			CantDoThatException {
		ChartDataInfo reportSummaryData = this.databaseDefn.getDataManagement().getChartData(
				company, reportSummary, sessionData.getReportFilterValues(), false);
		if (reportSummaryData == null) {
			return;
		}
		int rowNum;
		Row row;
		Cell cell;
		int columnNum;
		String fieldValue;
		Sheet summarySheet;
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
		CellStyle boldCellStyle = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		boldCellStyle.setFont(font);
		Set<ChartAggregateInfo> aggregateFunctions = reportSummary.getAggregateFunctions();
		Set<ChartGroupingInfo> groupings = reportSummary.getGroupings();
		for (ChartGroupingInfo grouping : groupings) {
			BaseField groupingBaseField = grouping.getGroupingReportField().getBaseField();
			if (groupingBaseField instanceof RelationField) {
				fieldValue = groupingBaseField.getTableContainingField() + ": "
						+ ((RelationField) groupingBaseField).getDisplayField();
			} else {
				fieldValue = groupingBaseField.getFieldName();
			}
			cell = row.createCell(columnNum);
			cell.setCellValue(fieldValue);
			cell.setCellStyle(boldCellStyle);
			columnNum++;
		}
		for (ChartAggregateInfo aggregateFunction : aggregateFunctions) {
			fieldValue = aggregateFunction.toString();
			cell = row.createCell(columnNum);
			cell.setCellValue(fieldValue);
			cell.setCellStyle(boldCellStyle);
			columnNum++;
		}
		List<ChartDataRowInfo> reportSummaryDataRows = reportSummaryData.getChartDataRows();
		rowNum++;
		for (ChartDataRowInfo summaryDataRow : reportSummaryDataRows) {
			row = summarySheet.createRow(rowNum);
			columnNum = 0;
			for (ChartGroupingInfo grouping : groupings) {
				fieldValue = summaryDataRow.getGroupingValue(grouping);
				row.createCell(columnNum).setCellValue(fieldValue);
				columnNum++;
			}
			for (ChartAggregateInfo aggregateFunction : aggregateFunctions) {
				Double number = summaryDataRow.getAggregateValue(aggregateFunction).doubleValue();
				row.createCell(columnNum, Cell.CELL_TYPE_NUMERIC).setCellValue(number);
				columnNum++;
			}
			rowNum++;
		}
	}

	private DatabaseInfo databaseDefn = null;

	private static final SimpleLogger logger = new SimpleLogger(ReportDownloader.class);
}
