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
package com.gtwm.pb.util;

import org.grlea.log.SimpleLogger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
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
import com.gtwm.pb.model.manageSchema.Module;
import com.gtwm.pb.model.manageSchema.TableDefn;
import com.gtwm.pb.model.manageSchema.BaseReportDefn;
import com.gtwm.pb.model.manageSchema.SimpleReportDefn;
import com.gtwm.pb.model.manageSchema.AbstractReportField;
import com.gtwm.pb.model.manageSchema.ReportFieldDefn;
import com.gtwm.pb.model.manageSchema.ReportCalcFieldDefn;
import com.gtwm.pb.model.manageSchema.JoinClause;
import com.gtwm.pb.model.manageSchema.ReportFilterDefn;
import com.gtwm.pb.model.manageSchema.ReportSummaryDefn;
import com.gtwm.pb.model.manageSchema.ReportSummaryAggregateDefn;
import com.gtwm.pb.model.manageSchema.ReportSort;
import com.gtwm.pb.model.manageSchema.ReportSummaryGrouping;
import com.gtwm.pb.model.manageSchema.fields.AbstractField;
import com.gtwm.pb.model.manageSchema.fields.BigTextFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.CheckboxFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.DateFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.DecimalFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.FileFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.IntegerFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.RelationFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.SequenceFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.TextFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.SeparatorFieldDefn;

public final class HibernateUtil {

	private static final SessionFactory sessionFactory;

	private static final SimpleLogger logger = new SimpleLogger(HibernateUtil.class);

	static {
		try {
			// Create the SessionFactory
			// specifying which classes should be persisted
		    Configuration cfg = new Configuration();
			cfg.addPackage("com.gtwm.pb.auth");
			cfg.addAnnotatedClass(Authenticator.class);
			cfg.addAnnotatedClass(Company.class);
			cfg.addAnnotatedClass(AppRole.class);
			cfg.addAnnotatedClass(AppUser.class);
			cfg.addAnnotatedClass(UserGeneralPrivilege.class);
			cfg.addAnnotatedClass(RoleGeneralPrivilege.class);
			cfg.addAnnotatedClass(RoleTablePrivilege.class);
			cfg.addAnnotatedClass(UserTablePrivilege.class);
			cfg.addPackage("com.gtwm.pb.model.manageSchema");
			cfg.addAnnotatedClass(Module.class);
			cfg.addAnnotatedClass(TableDefn.class);
			cfg.addAnnotatedClass(BaseReportDefn.class);
			cfg.addAnnotatedClass(AbstractReportField.class);
			cfg.addAnnotatedClass(ReportFieldDefn.class);
			cfg.addAnnotatedClass(ReportCalcFieldDefn.class);
			cfg.addAnnotatedClass(ReportFilterDefn.class);
			cfg.addAnnotatedClass(ReportSummaryAggregateDefn.class);
			cfg.addAnnotatedClass(ReportSummaryDefn.class);
			cfg.addAnnotatedClass(SimpleReportDefn.class);
			cfg.addAnnotatedClass(ReportSort.class);
			cfg.addAnnotatedClass(ReportSummaryGrouping.class);
			cfg.addAnnotatedClass(JoinClause.class);
			cfg.addPackage("com.gtwm.pb.model.manageSchema.fields");
			cfg.addAnnotatedClass(AbstractField.class);
			cfg.addAnnotatedClass(BigTextFieldDefn.class);
			cfg.addAnnotatedClass(CheckboxFieldDefn.class);
			cfg.addAnnotatedClass(DateFieldDefn.class);
			cfg.addAnnotatedClass(DecimalFieldDefn.class);
			cfg.addAnnotatedClass(FileFieldDefn.class);
			cfg.addAnnotatedClass(IntegerFieldDefn.class);
			cfg.addAnnotatedClass(RelationFieldDefn.class);
			cfg.addAnnotatedClass(SequenceFieldDefn.class);
			cfg.addAnnotatedClass(TextFieldDefn.class);
			cfg.addAnnotatedClass(SeparatorFieldDefn.class);
			sessionFactory = cfg.buildSessionFactory();
		} catch (Throwable ex) {
			logger.error("Initial SessionFactory creation failed: " + ex);
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
}