package com.gtwm.pb.servlets;

import java.sql.SQLException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.view.VelocityViewServlet;
import org.grlea.log.SimpleLogger;
import org.json.JSONException;

import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.SessionDataInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.ViewToolsInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.BaseValue;
import com.gtwm.pb.model.manageData.InputRecordException;
import com.gtwm.pb.model.manageData.SessionData;
import com.gtwm.pb.model.manageData.ViewTools;
import com.gtwm.pb.util.AgileBaseException;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.Enumerations.PublicAction;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.Enumerations.ResponseReturnType;

/**
 * A servlet that serves public data, e.g. for integration of agileBase forms
 * into websites
 */
public class Public extends VelocityViewServlet {

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		this.databaseDefn = (DatabaseInfo) servletContext
				.getAttribute("com.gtwm.pb.servlets.databaseDefn");
		if (this.databaseDefn == null) {
			throw new ServletException(
					"Error starting CalendarPublisher servlet. No databaseDefn object in the servlet context");
		}
		this.webAppRoot = servletContext.getRealPath("/");
	}

	public void destroy() {
		super.destroy();
		// release memory for good measure
		this.databaseDefn = null;
	}

	public Template handleRequest(HttpServletRequest request, HttpServletResponse response,
			Context context) {
		ResponseReturnType responseReturnType = ResponseReturnType.HTML;
		response.setContentType(responseReturnType.getResponseType());
		response.setCharacterEncoding("ISO-8859-1");
		EnumSet<PublicAction> publicActions = EnumSet.allOf(PublicAction.class);
		String templatePath = "gui/public/";
		List<FileItem> multipartItems = ServletUtilMethods.getMultipartItems(request);
		String templateName = ServletUtilMethods.getParameter(request, "return", multipartItems);
		if (templateName != null) {
			// var is from public input, clean
			templateName = templateName.replaceAll("\\W", "");
		}
		AppUserInfo publicUser = null;
		for (PublicAction publicAction : publicActions) {
			String publicActionValue = request.getParameter(publicAction.toString().toLowerCase());
			if (publicActionValue != null) {
				TableInfo table = null;
				CompanyInfo company = null;
				try {
					publicUser = ServletUtilMethods.getPublicUserForRequest(request,
							this.databaseDefn.getAuthManager().getAuthenticator());
					company = publicUser.getCompany();
					String internalTableName = request.getParameter("t");
					if (internalTableName == null) {
						throw new MissingParametersException(
								"t (internal table ID) parameter is necessary");
					}
					table = this.getPublicTable(company, internalTableName);
				} catch (AgileBaseException abex) {
					ServletUtilMethods.logException(abex, request, "Error preparing public data");
					return this.getUserInterfaceTemplate(request, response, "report_error",
							context, abex);
				}
				switch (publicAction) {
				case GET_REPORT_JSON:
					templateName = "report_json";
					String internalReportName = request.getParameter("r");
					try {
						if (internalReportName == null) {
							throw new MissingParametersException(
									"r (internal report ID) parameter is necessary to export report JSON");
						}
						BaseReportInfo report = table.getReport(internalReportName);
						if (!report.getCalendarSyncable()) {
							throw new CantDoThatException("The report " + report
									+ " has not been set as publicly exportable");
						}
						String reportJSON = this.databaseDefn.getDataManagement().getReportJSON(
								publicUser, report);
						context.put("gtwmReportJSON", reportJSON);
						response.setContentType("application/json");
					} catch (AgileBaseException abex) {
						ServletUtilMethods.logException(abex, request,
								"General error performing save from public");
						return this.getUserInterfaceTemplate(request, response, templateName,
								context, abex);
					} catch (JSONException jsonex) {
						ServletUtilMethods.logException(jsonex, request,
								"General error performing save from public");
						return this.getUserInterfaceTemplate(request, response, templateName,
								context, jsonex);
					} catch (SQLException sqlex) {
						ServletUtilMethods.logException(sqlex, request,
								"General error performing save from public");
						return this.getUserInterfaceTemplate(request, response, templateName,
								context, sqlex);
					}
				case SHOW_FORM:
					context.put("gtpbPublicTable", table);
					context.put("gtpbCompany", company);
					if (templateName == null) {
						templateName = "form";
					}
					templateName = templatePath + templateName;
					if (!table.getTableFormPublic()) {
						CantDoThatException cdtex = new CantDoThatException("The table " + table
								+ " has not been set for use as a public form");
						ServletUtilMethods.logException(cdtex, request,
								"General error performing save from public");
						return this.getUserInterfaceTemplate(request, response, templateName,
								context, cdtex);
					}
					break;
				case SAVE_NEW_RECORD:
					if (templateName == null) {
						templateName = "posted";
					}
					templateName = templatePath + templateName;
					SessionDataInfo sessionData = new SessionData();
					try {
						if (!table.getTableFormPublic()) {
							throw new CantDoThatException("The table " + table
									+ " has not been set for use as a public form");
						}
						ServletSessionMethods.setFieldInputValues(sessionData, request, true,
								this.databaseDefn, table, multipartItems);
						// Check that all mandatory fields have been filled in
						LinkedHashMap<BaseField, BaseValue> fieldInputValues = new LinkedHashMap<BaseField, BaseValue>(
								sessionData.getFieldInputValues());
						for (Map.Entry<BaseField, BaseValue> inputEntry : fieldInputValues
								.entrySet()) {
							BaseField inputField = inputEntry.getKey();
							if (inputField.getNotNull()) {
								BaseValue inputValue = inputEntry.getValue();
								if (inputValue.isNull()) {
									// If a mandatory field is empty, return to
									// the form and report the error
									templateName = templatePath + "form";
									context.put("gtpbPublicTable", table);
									context.put("gtpbCompany", company);
									AgileBaseException exceptionCaught = new InputRecordException(
											"This required field has to be filled in", inputField);
									return this.getUserInterfaceTemplate(request, response,
											templateName, context, exceptionCaught);
								}
							}
						}
						this.databaseDefn.getDataManagement().saveRecord(request, table,
								fieldInputValues, true, -1, sessionData, multipartItems);
					} catch (AgileBaseException abex) {
						ServletUtilMethods.logException(abex, request,
								"General error performing save from public");
						return this.getUserInterfaceTemplate(request, response, templateName,
								context, abex);
					} catch (SQLException sqlex) {
						ServletUtilMethods.logException(sqlex, request,
								"SQL error performing save from public");
						return this.getUserInterfaceTemplate(request, response, templateName,
								context, sqlex);
					} catch (FileUploadException fuex) {
						ServletUtilMethods.logException(fuex, request,
								"General error doing file upload from public");
						return this.getUserInterfaceTemplate(request, response, templateName,
								context, fuex);
					}
					break;
				}
			}
		}
		return this.getUserInterfaceTemplate(request, response, templateName, context, null);
	}

	private TableInfo getPublicTable(CompanyInfo company, String internalTableName)
			throws ObjectNotFoundException, MissingParametersException {
		TableInfo table = null;
		for (TableInfo testTable : company.getTables()) {
			if (testTable.getInternalTableName().equals(internalTableName)) {
				return testTable;
			}
		}
		throw new ObjectNotFoundException("Table with ID " + internalTableName
				+ " not found in company " + company);
	}

	/**
	 * Create an instance of ViewMethods to provide the UI with the necessary
	 * functionality, and return the requested template.
	 * 
	 * @param exceptionCaught
	 *            An exception thrown by handleRequest. Pass null if none. This
	 *            will be saved in ViewMethods to allow the UI to find out what
	 *            went wrong
	 * @return The template requested, ready to parse by the UI
	 */
	private Template getUserInterfaceTemplate(HttpServletRequest request,
			HttpServletResponse response, String templateName, Context context,
			Exception exceptionCaught) {
		ViewToolsInfo viewTools = new ViewTools(request, response, this.webAppRoot);
		context.put("viewTools", viewTools);
		context.put("exceptionCaught", exceptionCaught);
		if (templateName == null) {
			logger.error("No template specified.");
		}
		templateName = templateName + ".vm";
		Template template = null;
		try {
			// See note about template locations at top of file
			template = getTemplate(templateName);
		} catch (ResourceNotFoundException rnfe) {
			logger.error("Template not found: " + rnfe);
		} catch (ParseErrorException pee) {
			logger.error("Syntax error in the template: " + pee);
		}
		return template;
	}

	private DatabaseInfo databaseDefn = null;

	private String webAppRoot;

	private static final SimpleLogger logger = new SimpleLogger(Public.class);
}
