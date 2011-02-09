package com.gtwm.pb.servlets;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.view.VelocityViewServlet;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.auth.PublicUser;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.ViewMethodsInfo;
import com.gtwm.pb.model.interfaces.ViewToolsInfo;
import com.gtwm.pb.model.manageData.ViewMethods;
import com.gtwm.pb.model.manageData.ViewTools;
import com.gtwm.pb.util.AgileBaseException;
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
		TableInfo table = null;
		try {
			table = this.getPublicTable(request);
		} catch (AgileBaseException abex) {
			AppController.logException(abex, request, "Error preparing public data");
		}
		String templateName = "gui/public/form";
		context.put("gtpbPublicTable", table);
		return this.getUserInterfaceTemplate(request, response, templateName, context);
	}

	private TableInfo getPublicTable(HttpServletRequest request) throws AgileBaseException {
		String internalCompanyName = request.getParameter("c");
		if (internalCompanyName == null) {
			throw new MissingParametersException("c (internal company ID) parameter is necessary");
		}
		String internalTableName = request.getParameter("t");
		if (internalTableName == null) {
			throw new MissingParametersException("t (internal table ID) parameter is necessary");
		}
		String userName = "publicform";
		String forename = "Public";
		String surname = "Form";
		try {
			AppUserInfo publicUser = new PublicUser(this.databaseDefn.getAuthManager()
					.getAuthenticator(), internalCompanyName, userName, surname, forename);
			CompanyInfo company = publicUser.getCompany();
			TableInfo table = null;
			TABLE_LOOP: for (TableInfo testTable : company.getTables()) {
				if (testTable.getInternalTableName().equals(internalTableName)) {
					return testTable;
				}
			}
			throw new ObjectNotFoundException("Table with ID " + internalTableName
					+ " not found in company " + company);
		} catch (AgileBaseException abex) {
			logger.error("Error getting table: " + abex);
			throw abex;
		}
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
			HttpServletResponse response, String templateName, Context context) {
		ViewToolsInfo viewTools = new ViewTools(request, response, this.webAppRoot);
		logger.debug("Putting viewTools in context: " + viewTools);
		context.put("viewTools", viewTools);
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
