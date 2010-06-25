package com.gtwm.pb.auth;

import org.grlea.log.SimpleLogger;

import com.gtwm.pb.model.interfaces.DatabaseInfo;

public class DashboardPopulator implements Runnable {

	public void run() {
		logger.info("Notice: dashboard generation unable to run");
		logger.info("Dashboard components are not distributed in the agileBase open source version");
		logger.info("Please contact GT webMarque to obtain the dashboard module");
		logger.info("Email: support@agilebase.co.uk");		
	}

	public DashboardPopulator(DatabaseInfo databaseDefn) {
	}

	private static final SimpleLogger logger = new SimpleLogger(DashboardPopulator.class);

}
