/*
 *  Copyright 2009 GT webMarque Ltd
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
package com.gtwm.pb.model.interfaces;

import java.util.List;
import com.gtwm.pb.util.CantDoThatException;
import java.sql.SQLException;

/**
 * Contains methods to aid integration of agileBase with a wiki, for a particular company
 */
public interface WikiManagementInfo {

	/**
	 * Returns true if a wiki is running on this computer and integrated into
	 * agileBase via the servlet container's server.xml
	 * 
	 * Returns false if no wiki functionality will be integrated into agileBase
	 */
	public boolean isWikiIntegrated();
	
	/**
	 * Get the company this wiki is for
	 */
	public CompanyInfo getCompany();

	/**
	 * Given a wiki page name, return the first part of the text of that page,
	 * formatted as HTML for display, or an empty string if no page exists for
	 * the record
	 * 
	 * The results of this method can therefore also be used to check if a wiki
	 * page exists
	 * 
	 * @throws CantDoThatException
	 *             If no wiki is integrated
	 * @throws SQLException
	 *             if there was an internal error getting the content
	 */
	public String getWikiPageSnippet(String wikiPageName, int numChars)
			throws CantDoThatException, SQLException;

	/**
	 * Return the URL for visiting a wiki page
	 * 
	 * @param edit
	 *            If true, return a URL for editing the page, if false, for
	 *            viewing
	 */
	public String getWikiUrl(String wikiPageName, boolean edit);

	/**
	 * Return a list of pages from the wiki. The page name and snippet of
	 * content is returned. Results are filtered according to the parameters
	 * 
	 * @param pageNameFilter
	 *            Restricts results to the pages starting with the given string
	 * @param pageContentFilter
	 *            Restricts results to pages containing the given string
	 */
	public List<WikiRecordDataRowInfo> getWikiRecordDataRows(
			String pageNameFilter, String pageContentFilter) throws CantDoThatException,
			SQLException;
}
