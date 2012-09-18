package com.gtwm.pb.servlets;

import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.view.VelocityViewServlet;
import org.codehaus.jackson.JsonGenerationException;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.DataManagementInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.SessionDataInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.ViewToolsInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.BaseValue;
import com.gtwm.pb.model.interfaces.fields.TextValue;
import com.gtwm.pb.model.manageData.InputRecordException;
import com.gtwm.pb.model.manageData.SessionData;
import com.gtwm.pb.model.manageData.ViewTools;
import com.gtwm.pb.util.AgileBaseException;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.Enumerations.PublicAction;
import com.gtwm.pb.util.Helpers;
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
					"Error starting Public servlet. No databaseDefn object in the servlet context");
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
		List<FileItem> multipartItems = ServletUtilMethods.getMultipartItems(request);
		// For company specific customisations
		String customFolder = ServletUtilMethods.getParameter(request, "custom", multipartItems);
		AppUserInfo publicUser;
		try {
			publicUser = ServletUtilMethods.getPublicUserForRequest(request, this.databaseDefn
					.getAuthManager().getAuthenticator());
		} catch (AgileBaseException abex) {
			ServletUtilMethods.logException(abex, request, "Error getting public user for request");
			return this.getUserInterfaceTemplate(request, response, "gui/public/error", context,
					abex);
		}
		CompanyInfo company = publicUser.getCompany();
		String templatePath;
		if (customFolder != null) {
			String cleanCompanyName = company.getCompanyName().replaceAll("\\W", "").toLowerCase();
			customFolder = customFolder.replaceAll("\\W", "").toLowerCase();
			templatePath = "gui/customisations/" + cleanCompanyName + "/public/" + customFolder
					+ "/";
		} else {
			templatePath = "gui/public/";
		}
		String templateName = ServletUtilMethods.getParameter(request, "return", multipartItems);
		if (templateName != null) {
			// var is from public input, clean
			templateName = templateName.replaceAll("\\W", "");
		}
		DataManagementInfo dataManagement = this.databaseDefn.getDataManagement();
		for (PublicAction publicAction : publicActions) {
			String publicActionValue = request.getParameter(publicAction.toString().toLowerCase());
			if (publicActionValue != null) {
				TableInfo table = null;
				try {
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
				case GET_REPORT_RSS:
					Long cacheSeconds = null;
					String cacheSecondsString = request.getParameter("cache_seconds");
					if (cacheSecondsString != null) {
						cacheSeconds = Long.valueOf(cacheSecondsString);
					}
					if (publicAction.equals(PublicAction.GET_REPORT_JSON)) {
						String jsonFormat = request.getParameter("json_format");
						if (jsonFormat != null) {
							if (jsonFormat.equals("json")) {
								templateName = templatePath + "report_json_bare";
							} else {
								templateName = templatePath + "report_json";
							}
						} else {
							templateName = templatePath + "report_json";
						}
						if (cacheSeconds == null) {
							cacheSeconds = Long.valueOf(30 * 60); // default to
																	// 30 mins
																	// cache
						}
					} else {
						templateName = templatePath + "report_rss";
						if (cacheSeconds == null) {
							cacheSeconds = Long.valueOf(2 * 60); // default to 2
																	// mins
																	// cache
						}
					}
					String internalReportName = request.getParameter("r");
					try {
						if (internalReportName == null) {
							throw new MissingParametersException(
									"r (internal report ID) parameter is necessary to export report JSON/RSS");
						}
						BaseReportInfo report = table.getReport(internalReportName);
						if (!report.getCalendarSyncable()) {
							throw new CantDoThatException("The report " + report
									+ " has not been set as publicly exportable");
						}
						Map<BaseField, String> filters = getFilters(report, request);
						boolean exactFilters = Helpers.valueRepresentsBooleanTrue(request
								.getParameter("exact_filters"));
						if (publicAction.equals(PublicAction.GET_REPORT_JSON)) {
							String reportJSON = dataManagement.getReportJSON(publicUser, report,
									filters, exactFilters, cacheSeconds);
							context.put("gtwmReportJSON", reportJSON);
						} else {
							String reportRSS = dataManagement.getReportRSS(publicUser, report,
									filters, exactFilters, cacheSeconds);
							context.put("gtwmReportRSS", reportRSS);
							response.setContentType(ResponseReturnType.XML.getResponseType());
						}
					} catch (AgileBaseException abex) {
						ServletUtilMethods.logException(abex, request,
								"General error getting report JSON/RSS");
						return this.getUserInterfaceTemplate(request, response, templateName,
								context, abex);
					} catch (SQLException sqlex) {
						ServletUtilMethods.logException(sqlex, request,
								"General error getting report JSON/RSS");
						return this.getUserInterfaceTemplate(request, response, templateName,
								context, sqlex);
					} catch (XMLStreamException xmlex) {
						ServletUtilMethods.logException(xmlex, request,
								"General error getting report JSON/RSS");
						return this.getUserInterfaceTemplate(request, response, templateName,
								context, xmlex);
					} catch (JsonGenerationException jgex) {
						ServletUtilMethods.logException(jgex, request,
								"General error getting report JSON/RSS");
						return this.getUserInterfaceTemplate(request, response, templateName,
								context, jgex);
					}
					break;
				case SHOW_FORM:
					if (!table.getTableFormPublic()) {
						CantDoThatException cdtex = new CantDoThatException("The table " + table
								+ " has not been set for use as a public form");
						ServletUtilMethods.logException(cdtex, request,
								"General error performing save from public");
						return this.getUserInterfaceTemplate(request, response, templateName,
								context, cdtex);
					}
					context.put("gtpbPublicTable", table);
					context.put("gtpbCompany", company);
					if (templateName == null) {
						templateName = "form";
					}
					templateName = templatePath + templateName;
					String gtpbCss = request.getParameter("css");
					if (gtpbCss != null) {
						// Allow only local CSS. Arbitrary CSS injection is a
						// vulnerability which could lead to Javascript
						// injection, phishing etc.
						gtpbCss = gtpbCss.replaceAll("^.*:", "").replace("//", "")
								.replaceAll("[^\\w\\.\\/]", "");
						context.put("gtpbCss", gtpbCss);
					}
					break;
				case SAVE_NEW_RECORD:
				case UPDATE_RECORD:
					int rowId = -1;
					boolean newRecord = true;
					if (templateName == null) {
						templateName = "posted";
					}
					templateName = templatePath + templateName;
					SessionDataInfo sessionData = new SessionData();
					try {
						if (publicAction.equals(PublicAction.UPDATE_RECORD)) {
							String rowIdString = ServletUtilMethods.getParameter(request, "row_id",
									multipartItems);
							if (rowIdString == null) {
								throw new CantDoThatException(
										"row_id must be provided to update a record");
							}
							rowId = Integer.valueOf(rowIdString);
							newRecord = false;
						}
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
						dataManagement.saveRecord(request, table, fieldInputValues, newRecord,
								rowId, sessionData, multipartItems);
						if (newRecord) {
							sendEmail(company, table, fieldInputValues);
						}
						String tableDataRowJson = dataManagement.getTableDataRowJson(table,
								sessionData.getRowId(table));
						context.put("tableDataRowJson", tableDataRowJson);
					} catch (AgileBaseException abex) {
						ServletUtilMethods.logException(abex, request,
								"General error performing save from public");
						templateName = templatePath + "form";
						return this.getUserInterfaceTemplate(request, response, templateName,
								context, abex);
					} catch (SQLException sqlex) {
						ServletUtilMethods.logException(sqlex, request,
								"SQL error performing save from public");
						templateName = templatePath + "form";
						return this.getUserInterfaceTemplate(request, response, templateName,
								context, sqlex);
					} catch (FileUploadException fuex) {
						ServletUtilMethods.logException(fuex, request,
								"General error doing file upload from public");
						templateName = templatePath + "form";
						return this.getUserInterfaceTemplate(request, response, templateName,
								context, fuex);
					} catch (MessagingException mex) {
						ServletUtilMethods.logException(mex, request,
								"Emailing error performing save from public");
						templateName = templatePath + "form";
						return this.getUserInterfaceTemplate(request, response, templateName,
								context, mex);
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
	 * Create an instance of ViewTools to provide the UI with the necessary
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

	private static Map<BaseField, String> getFilters(BaseReportInfo report,
			HttpServletRequest request) {
		Map<BaseField, String> filters = new HashMap<BaseField, String>();
		for (BaseField field : report.getReportBaseFields()) {
			String internalFieldName = field.getInternalFieldName();
			String fieldValue = request.getParameter(internalFieldName);
			if (fieldValue != null) {
				filters.put(field, fieldValue);
			}
		}
		return filters;
	}

	private static void sendEmail(CompanyInfo company, TableInfo table,
			Map<BaseField, BaseValue> fieldInputValues) throws MessagingException {
		String emailTo = table.getEmail();
		if (emailTo == null) {
			return;
		}
		if (!emailTo.contains("@")) {
			return;
		}
		String subject = "New " + table.getSingularName();
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", "localhost");
		Session mailSession = Session.getDefaultInstance(props, null);
		MimeMessage message = new MimeMessage(mailSession);
		message.setSubject(subject);
		Address toAddress = new InternetAddress(emailTo);
		message.addRecipient(Message.RecipientType.TO, toAddress);
		String emailContent = table.getEmailResponse();
		if (emailContent == null) {
			emailContent = "";
		} else {
			emailContent = emailContent.trim();
		}
		if (emailContent.equals("")) {
			emailContent = "This message has been automatically generated by your submission to "
					+ company.getCompanyName() + " " + table.getSimpleName() + "\n\n";
			emailContent += "Thank you for your request. Your information has been received as below.\n\n---------------\n\n";
			for (Map.Entry<BaseField, BaseValue> entry : fieldInputValues.entrySet()) {
				BaseField field = entry.getKey();
				if (!field.getHidden()) {
					BaseValue value = entry.getValue();
					emailContent += field + ": " + value + "\n";
				}
			}
		}
		for (Map.Entry<BaseField, BaseValue> entry : fieldInputValues.entrySet()) {
			// Detect any email addresses entered and CC the form to
			// them
			BaseField field = entry.getKey();
			if (!field.getHidden()) {
				BaseValue value = entry.getValue();
				if (value instanceof TextValue) {
					if (((TextValue) value).isEmailAddress()) {
						message.addRecipient(Message.RecipientType.CC,
								new InternetAddress(value.toString()));
					}
				}
			}
		}
		message.setText(emailContent);
		message.setFrom(new InternetAddress("notifications@agilebase.co.uk"));
		Transport.send(message);
	}

	private DatabaseInfo databaseDefn = null;

	private String webAppRoot;

	private static final SimpleLogger logger = new SimpleLogger(Public.class);
}
