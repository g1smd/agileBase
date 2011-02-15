/*
 *  Copyright 2011 GT webMarque Ltd
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
package com.gtwm.pb.integration;

import com.gtwm.pb.auth.Authenticator;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.WikiManagementInfo;
import com.gtwm.pb.model.interfaces.WikiRecordDataRowInfo;
import com.gtwm.pb.integration.WikiRecordDataRow;
import com.gtwm.pb.util.CantDoThatException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.LinkedList;
import javax.sql.DataSource;
import org.apache.commons.lang.WordUtils;

import org.grlea.log.SimpleLogger;

public class MediaWikiManagement implements WikiManagementInfo {

	private MediaWikiManagement() {
	}
	
	/**
	 * Create an object to manage the MediaWiki data in the datasource provided
	 * 
	 * @param wikiDataSource Should be null if there's no wiki running for the company
	 */
	public MediaWikiManagement(CompanyInfo company, DataSource wikiDataSource) {
		this.wikiDataSource = wikiDataSource;
		this.company = company;
		this.cleanedCompanyName = company.getCompanyName().toLowerCase().replaceAll("\\W", "");
	}

	public boolean isWikiIntegrated() {
		return (this.wikiDataSource != null);
	}
	
	public CompanyInfo getCompany() {
		return this.company;
	}

	public String getWikiUrl(String wikiPageName, boolean edit) {
		String wikiUrl = "http://gtwebmarque.com/wikis/";
		wikiUrl += this.cleanedCompanyName + "/";
		if (edit) {
			wikiUrl += "index.php?title=";
			wikiUrl += this.getWikifiedPageName(wikiPageName);
			wikiUrl += "&action=edit";
		} else {
			wikiUrl += "index.php/" + this.getWikifiedPageName(wikiPageName);
		}

		return wikiUrl;
	}

	public String getWikiPageSnippet(String wikiPageName, int numChars)
			throws CantDoThatException, SQLException {
		if (!this.isWikiIntegrated()) {
			throw new CantDoThatException("No wiki is integrated");
		}
		Connection conn = null;
		String wikiContent = "";
		String wikifiedPageName = this.getWikifiedPageName(wikiPageName);
		try {
			conn = this.wikiDataSource.getConnection();
			conn.setAutoCommit(false);
			String SQLCode = "SELECT pc.old_text ";
			SQLCode += "FROM " + "page p INNER JOIN "
					+ "revision r ON p.page_id = r.rev_page ";
			SQLCode += "  INNER JOIN "
					+ "pagecontent pc ON r.rev_text_id = pc.old_id ";
			SQLCode += "WHERE p.page_title = ?";
			SQLCode += "ORDER BY r.rev_id DESC LIMIT 1";
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			statement.setString(1, wikifiedPageName);
			ResultSet results = statement.executeQuery();
			if (results.next()) {
				wikiContent = results.getString(1);
			}
			results.close();
			statement.close();
		} catch (SQLException sqlex) {
			throw sqlex;
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		if (wikiContent.length() > numChars) {
			wikiContent = wikiContent.substring(0, numChars);
		}
		// strip out common wiki syntax
		wikiContent = wikiContent.replaceAll("[\\=\\'\\[\\]]{2,}?", "");
		wikiContent = wikiContent.replaceAll("\\n", "<p>");
		return wikiContent;
	}

	public List<WikiRecordDataRowInfo> getWikiRecordDataRows(
			String pageNameFilter, String pageContentFilter) throws CantDoThatException,
			SQLException {
		if (!this.isWikiIntegrated()) {
			throw new CantDoThatException("No wiki is integrated");
		}
		if (pageNameFilter == null) {
			pageNameFilter = "";
		}
		if (pageContentFilter == null) {
			pageContentFilter = "";
		}
		String SQLCode = "SELECT p.page_title, pc.old_text ";
		SQLCode += "FROM page p INNER JOIN " 
				+ "revision r ON p.page_id = r.rev_page ";
		SQLCode += "  INNER JOIN " + "pagecontent pc ON r.rev_text_id = pc.old_id ";
		if (pageNameFilter.length() > 0) {
			SQLCode += " WHERE p.page_title ILIKE ?";
			if (pageContentFilter.length() > 0) {
				SQLCode += " AND pc.old_text ILIKE ?";
			}
		} else if (pageContentFilter.length() > 0) {
			SQLCode += " WHERE pc.old_text ILIKE ?";
		}
		if ((pageNameFilter.length() > 0) || (pageContentFilter.length() > 0)) {
			SQLCode += " AND";
		} else {
			SQLCode += " WHERE";
		}
		SQLCode += " r.rev_id = (select max(r1.rev_id) from gtwm.revision r1 where r1.rev_page = r.rev_page)";
		SQLCode += " AND p.page_title NOT IN (SELECT img_name FROM image)";
		SQLCode += " ORDER BY r.rev_id DESC LIMIT 50";
		List<WikiRecordDataRowInfo> wikiRecordDataRows = new LinkedList<WikiRecordDataRowInfo>();
		Connection conn = null;
		try {
			conn = this.wikiDataSource.getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			if (pageNameFilter.length() > 0) {
				statement.setString(1, pageNameFilter + "%");
				if (pageContentFilter.length() > 0) {
					statement.setString(2, "%" + pageContentFilter + "%");
				}
			} else if (pageContentFilter.length() > 0) {
				statement.setString(1, "%" + pageContentFilter + "%");
			}
			ResultSet results = statement.executeQuery();
			while (results.next()) {
				String pageName = results.getString(1);
				String pageContent = results.getString(2);
				if (pageContent.length() > 100) {
					pageContent = pageContent.substring(0, 100);
				}
				// strip out common wiki syntax
				pageContent = pageContent.replaceAll("[\\=\\'\\[\\]]{2,}?", "");
				WikiRecordDataRow wikiRecordDataRow = new WikiRecordDataRow(pageName, pageContent);
				wikiRecordDataRows.add(wikiRecordDataRow);
			}
			results.close();
			statement.close();
		} catch (SQLException sqlex) {
			throw sqlex;
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return wikiRecordDataRows;
	}

	/**
	 * Equality based on company owning the wiki
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		MediaWikiManagement otherMediaWikiManagement = (MediaWikiManagement) obj;
		return (this.company.equals(otherMediaWikiManagement.getCompany()));
	}
	
	public int hashCode() {
		return this.company.hashCode();
	}

	/**
	 * Examples:
	 * 
	 * Page_Name -> Page_Name
	 * 
	 * Page Name -> Page_Name
	 * 
	 * page name -> Page_Name
	 * 
	 * Page_name -> Page_Name
	 */
	private String getWikifiedPageName(String rawWikiPageName) {
		String wikifiedPageName = rawWikiPageName.replace("_", " ");
		wikifiedPageName = WordUtils.capitalizeFully(wikifiedPageName);
		wikifiedPageName = wikifiedPageName.replace(" ", "_");
		return wikifiedPageName;
	}
	
	private DataSource wikiDataSource = null;
	
	private String cleanedCompanyName = null;
	
	private CompanyInfo company = null;

	private static final SimpleLogger logger = new SimpleLogger(Authenticator.class);

}
