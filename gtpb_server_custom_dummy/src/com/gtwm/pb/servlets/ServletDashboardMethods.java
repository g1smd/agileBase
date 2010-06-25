package com.gtwm.pb.servlets;

import javax.servlet.http.HttpServletRequest;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.SessionDataInfo;

public class ServletDashboardMethods {

	public synchronized static void setDashboardSummaryState(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) {
		logger.info("Notice: dashboard action unable to run");
		logger.info("Dashboard components are not distributed in the agileBase open source version");
		logger.info("Please contact GT webMarque to obtain the dashboard module");
		logger.info("Email: support@agilebase.co.uk");		
	}
	
	private static final SimpleLogger logger = new SimpleLogger(ServletDashboardMethods.class);
}
