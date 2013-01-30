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
package com.gtwm.pb.util;

import org.grlea.log.SimpleLogger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.HibernateException;
import com.gtwm.pb.auth.Authenticator;
import com.gtwm.pb.auth.Company;
import com.gtwm.pb.auth.AppRole;
import com.gtwm.pb.auth.AppUser;
import com.gtwm.pb.auth.UserGeneralPrivilege;
import com.gtwm.pb.auth.RoleGeneralPrivilege;
import com.gtwm.pb.auth.UserTablePrivilege;
import com.gtwm.pb.auth.RoleTablePrivilege;
import com.gtwm.pb.model.manageSchema.FormTab;
import com.gtwm.pb.model.manageSchema.Module;
import com.gtwm.pb.model.manageSchema.ReportMap;
import com.gtwm.pb.model.manageSchema.TableDefn;
import com.gtwm.pb.model.manageSchema.BaseReportDefn;
import com.gtwm.pb.model.manageSchema.SimpleReportDefn;
import com.gtwm.pb.model.manageSchema.AbstractReportField;
import com.gtwm.pb.model.manageSchema.ReportFieldDefn;
import com.gtwm.pb.model.manageSchema.ReportCalcFieldDefn;
import com.gtwm.pb.model.manageSchema.JoinClause;
import com.gtwm.pb.model.manageSchema.ReportFilterDefn;
import com.gtwm.pb.model.manageSchema.ChartDefn;
import com.gtwm.pb.model.manageSchema.ChartAggregateDefn;
import com.gtwm.pb.model.manageSchema.ReportSort;
import com.gtwm.pb.model.manageSchema.ChartGrouping;
import com.gtwm.pb.model.manageSchema.fields.AbstractField;
import com.gtwm.pb.model.manageSchema.fields.BigTextFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.CheckboxFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.CommentFeedFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.DateFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.DecimalFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.FileFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.IntegerFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.ReferencedReportDataFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.RelationFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.SequenceFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.TextFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.SeparatorFieldDefn;


public final class HibernateUtil {

	static {
		try {
			// Create the SessionFactory
			// specifying which classes should be persisted
			Configuration configuration = new Configuration();
			configuration.addPackage("com.gtwm.pb.auth");
			configuration.addAnnotatedClass(Authenticator.class);
			configuration.addAnnotatedClass(Company.class);
			configuration.addAnnotatedClass(AppRole.class);
			configuration.addAnnotatedClass(AppUser.class);
			configuration.addAnnotatedClass(UserGeneralPrivilege.class);
			configuration.addAnnotatedClass(RoleGeneralPrivilege.class);
			configuration.addAnnotatedClass(RoleTablePrivilege.class);
			configuration.addAnnotatedClass(UserTablePrivilege.class);
			configuration.addPackage("com.gtwm.pb.model.manageSchema");
			configuration.addAnnotatedClass(Module.class);
			configuration.addAnnotatedClass(TableDefn.class);
			configuration.addAnnotatedClass(BaseReportDefn.class);
			configuration.addAnnotatedClass(AbstractReportField.class);
			configuration.addAnnotatedClass(ReportFieldDefn.class);
			configuration.addAnnotatedClass(ReportCalcFieldDefn.class);
			configuration.addAnnotatedClass(ReportFilterDefn.class);
			configuration.addAnnotatedClass(ChartAggregateDefn.class);
			configuration.addAnnotatedClass(ChartDefn.class);
			configuration.addAnnotatedClass(ReportMap.class);
			configuration.addAnnotatedClass(SimpleReportDefn.class);
			configuration.addAnnotatedClass(ReportSort.class);
			configuration.addAnnotatedClass(ChartGrouping.class);
			configuration.addAnnotatedClass(JoinClause.class);
			configuration.addAnnotatedClass(FormTab.class);
			configuration.addPackage("com.gtwm.pb.model.manageSchema.fields");
			configuration.addAnnotatedClass(AbstractField.class);
			configuration.addAnnotatedClass(BigTextFieldDefn.class);
			configuration.addAnnotatedClass(CheckboxFieldDefn.class);
			configuration.addAnnotatedClass(DateFieldDefn.class);
			configuration.addAnnotatedClass(DecimalFieldDefn.class);
			configuration.addAnnotatedClass(FileFieldDefn.class);
			configuration.addAnnotatedClass(IntegerFieldDefn.class);
			configuration.addAnnotatedClass(RelationFieldDefn.class);
			configuration.addAnnotatedClass(SequenceFieldDefn.class);
			configuration.addAnnotatedClass(TextFieldDefn.class);
			configuration.addAnnotatedClass(SeparatorFieldDefn.class);
			configuration.addAnnotatedClass(ReferencedReportDataFieldDefn.class);
			configuration.addAnnotatedClass(CommentFeedFieldDefn.class);
			// TODO: not sure if this is necessary or not, check next time we
			// have a schema update
			// NB automatic schema updates don't work for adding non null (e.g.
			// basic types such as integer or boolean) properties, these have to
			// be added manually. Error messages will show the expected names of
			// the fields
			// new SchemaUpdate(cfg).execute(true, true);
			configuration.setProperty("hibernate.query.substitutions", "yes 'Y', no 'N'");
			configuration.setProperty("hibernate.connection.datasource", "java:comp/env/jdbc/agileBaseSchema");
			configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
			configuration.setProperty("hibernate.show_sql", "false");
			configuration.setProperty("hibernate.format_sql", "true");
			configuration.setProperty("hibernate.use_sql_comments", "true");
			configuration.setProperty("hibernate.hbm2ddl.auto", "update");
			configuration.setProperty("hibernate.max_fetch_depth","1");
			configuration.setProperty("hibernate.default_batch_fetch_size", "8");
			configuration.setProperty("hibernate.jdbc.batch_versioned_data", "true");
			configuration.setProperty("hibernate.jdbc.use_streams_for_binary", "true");
			configuration.setProperty("hibernate.cache.region_prefix","hibernate.test");
			configuration.setProperty("hibernate.cache.use_structured_entries","true");
			configuration.setProperty("hibernate.cache.provider_class","org.hibernate.cache.HashtableCacheProvider");
			configuration.configure();
			ServiceRegistry serviceRegistry = (new ServiceRegistryBuilder()).applySettings(configuration.getProperties()).buildServiceRegistry();
			sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		} catch (Throwable ex) {
			// TODO: logger doesn't seem to work in this static block
			//logger.error("Initial SessionFactory creation failed: " + ex);
			ex.printStackTrace();
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static final ThreadLocal session = new ThreadLocal();

	public static Session currentSession() throws HibernateException {
		Session s = (Session) session.get();
		// Open a new Session, if this Thread has none yet
		if (s == null) {
			s = sessionFactory.openSession();
			session.set(s);
		}
		return s;
	}

	public static void closeSession() throws HibernateException {
		Session s = (Session) session.get();
		session.set(null);
		if (s != null)
			s.close();
	}

	public static void startHibernateTransaction() throws HibernateException {
		Session hibSession = currentSession();
		try {
			hibSession.beginTransaction();
		} catch (HibernateException hex) {
			closeSession();
			throw hex;
		}
	}

	public static void finishHibernateTransaction() {
		Session hibSession = currentSession();
		try {
			Transaction hibTransaction = hibSession.getTransaction();
			// Use isActive rather than wasRolledBack as this doesn't seem to do
			// what I'd expect (tell you if the transaction was rolled back or
			// not)
			if (hibTransaction.isActive()) {
				hibSession.getTransaction().commit();
			}
		} finally {
			closeSession();
		}
	}

	public static void rollbackHibernateTransaction() {
		logger.info("Rolling back hibernate transaction");
		Session hibSession = currentSession();
		Transaction hibTransaction = hibSession.getTransaction();
		try {
			if (hibTransaction.isActive()) {
				hibTransaction.rollback();
			}
		} catch (Exception ex) {
			logger.error("Unable to rollback");
		}
	}

	/**
	 * Call a hibernate update() method on the passed object to make hibernate
	 * recognise that it's persistent
	 */
	public static void activateObject(Object obj) {
		Session hibernateSession = currentSession();
		// Calling update on an already persistent object causes an exception
		if (!hibernateSession.contains(obj)) {
			// Make object persistent
			try {
				hibernateSession.update(obj);
			} catch (HibernateException ex) {
				logger.error("Error activating object " + obj);
				logger.error("Activation exception: " + ex);
				throw ex;
			}
		}
	}
	
	private static final SessionFactory sessionFactory;
	
	private static final SimpleLogger logger = new SimpleLogger(HibernateUtil.class);

}