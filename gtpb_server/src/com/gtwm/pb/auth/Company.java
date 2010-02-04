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
package com.gtwm.pb.auth;

import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.DashboardInfo;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.manageSchema.Module;
import com.gtwm.pb.model.manageUsage.UsageLogger.LogType;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Collections;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.AppRoleInfo;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.ObjectNotFoundException;

@Entity
public class Company implements CompanyInfo, Comparable<CompanyInfo> {

	protected Company() {
	}

	public Company(String companyName) {
		this.setCompanyName(companyName);
		this.setInternalCompanyName((new RandomString()).toString());
	}

	@Id
	public String getInternalCompanyName() {
		return this.internalCompanyName;
	}

	private void setInternalCompanyName(String internalCompanyName) {
		this.internalCompanyName = internalCompanyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getCompanyName() {
		return this.companyName;
	}

	@Transient
	public synchronized SortedSet<TableInfo> getTables() {
		return Collections.unmodifiableSortedSet(new TreeSet<TableInfo>(this.tables));
	}

	public synchronized void addTable(TableInfo table) {
		this.tables.add(table);
	}

	public synchronized void removeTable(TableInfo table) {
		this.tables.remove(table);
	}

	@Transient
	public SortedSet<AppUserInfo> getUsers() {
		return Collections
				.unmodifiableSortedSet(new TreeSet<AppUserInfo>(this.getUsersCollection()));
	}

	@OneToMany(mappedBy = "company", targetEntity = AppUser.class, cascade = CascadeType.ALL)
	// Bi-directional OneToMany
	protected synchronized Set<AppUserInfo> getUsersCollection() {
		return this.usersCollection;
	}

	private synchronized void setUsersCollection(Set<AppUserInfo> usersCollection) {
		this.usersCollection = usersCollection;
	}

	@Transient
	public SortedSet<AppRoleInfo> getRoles() {
		return Collections
				.unmodifiableSortedSet(new TreeSet<AppRoleInfo>(this.getRolesCollection()));
	}

	@OneToMany(mappedBy = "company", targetEntity = AppRole.class, cascade = CascadeType.ALL)
	// Bi-directional OneToMany
	protected synchronized Set<AppRoleInfo> getRolesCollection() {
		return this.roles;
	}

	private synchronized void setRolesCollection(Set<AppRoleInfo> rolesCollection) {
		this.roles = rolesCollection;
	}

	@Transient
	public SortedSet<ModuleInfo> getModules() {
		return Collections.unmodifiableSortedSet(new TreeSet<ModuleInfo>(this
				.getModulesCollection()));
	}

	@Transient
	public ModuleInfo getModuleByInternalName(String internalModuleName)
			throws ObjectNotFoundException {
		for (ModuleInfo module : this.getModulesCollection()) {
			if (module.getInternalModuleName().equals(internalModuleName)) {
				return module;
			}
		}
		throw new ObjectNotFoundException("Can't find module with internal name "
				+ internalModuleName + " in company " + this);
	}

	/*
	 * Hibernate defaults to unidirectional one to many
	 */
	@OneToMany(targetEntity = Module.class, cascade = CascadeType.ALL)
	private synchronized Set<ModuleInfo> getModulesCollection() {
		return this.modules;
	}

	private synchronized void setModulesCollection(Set<ModuleInfo> modulesCollection) {
		this.modules = modulesCollection;
	}

	@Transient
	public Set<String> getTabAddresses() {
		return Collections.unmodifiableSet(new LinkedHashSet<String>(this
				.getTabAddressesCollection()));
	}

	@CollectionOfElements
	private synchronized Set<String> getTabAddressesCollection() {
		return this.tabAddresses;
	}

	private synchronized void setTabAddressesCollection(Set<String> tabAddressesCollection) {
		this.tabAddresses = tabAddressesCollection;
	}

	public synchronized void addUser(AppUserInfo user) {
		this.getUsersCollection().add(user);
	}

	public synchronized void addRole(AppRoleInfo role) {
		this.getRolesCollection().add(role);
	}

	public synchronized void addModule(ModuleInfo module) {
		this.getModulesCollection().add(module);
	}

	public synchronized void addTabAddress(String tabAddress) {
		this.getTabAddressesCollection().add(tabAddress);
	}

	public synchronized void removeUser(AppUserInfo user) {
		this.getUsersCollection().remove(user);
	}

	public synchronized void removeRole(AppRoleInfo role) {
		this.getRolesCollection().remove(role);
	}

	public synchronized void removeModule(ModuleInfo module) {
		this.getModulesCollection().remove(module);
	}

	public synchronized void removeTabAddress(String tabAddress) {
		this.getTabAddressesCollection().remove(tabAddress);
	}

	public synchronized void setCachedSparkline(LogType logType, int options,
			List<Integer> sparklineData) {
		String key = logType.name() + options;
		this.cachedSparkLines.put(key, sparklineData);
		this.sparkLineCacheTime = System.currentTimeMillis();
	}

	public synchronized List<Integer> getCachedSparkline(LogType logType, int options) {
		long cacheAge = System.currentTimeMillis() - this.sparkLineCacheTime;
		// Cache sparklines for two days
		if (cacheAge > 1000 * 60 * 60 * 48) {
			return new LinkedList<Integer>();
		}
		String key = logType.name() + options;
		List<Integer> cachedSparkLine = this.cachedSparkLines.get(key);
		if (cachedSparkLine == null) {
			return new LinkedList<Integer>();
		}
		return cachedSparkLine;
	}

	@Transient
	public DashboardInfo getDashboard() {
		return this.dashboard;
	}

	public void setDashboard(DashboardInfo dashboard) {
		this.dashboard = dashboard;
	}

	public void addSummaryIdForDashboard(long id) {
		this.getSummaryIdsForDashboardDirect().add(id);
	}

	public void addSummaryIdNotForDashboard(long id) {
		this.getSummaryIdsNotForDashboardDirect().add(id);
	}

	@Transient
	public SortedSet<Long> getSummaryIdsForDashboard() {
		return Collections.unmodifiableSortedSet(this.getSummaryIdsForDashboardDirect());
	}

	@CollectionOfElements(fetch = FetchType.EAGER)
	@Sort(type = SortType.NATURAL)
	private SortedSet<Long> getSummaryIdsForDashboardDirect() {
		return this.summaryIdsForDashboard;
	}
	
	/* Only used by Hibernate */
	private void setSummaryIdsForDashboardDirect(SortedSet<Long> sids) {
		this.summaryIdsForDashboard = sids;
	}

	@Transient
	public SortedSet<Long> getSummaryIdsNotForDashboard() {
		return Collections.unmodifiableSortedSet(this.getSummaryIdsNotForDashboardDirect());
	}

	@CollectionOfElements(fetch = FetchType.EAGER)
	@Sort(type = SortType.NATURAL)
	private SortedSet<Long> getSummaryIdsNotForDashboardDirect() {
		return this.summaryIdsNotForDashboard;
	}

	/* Only used by Hibernate */
	private void setSummaryIdsNotForDashboardDirect(SortedSet<Long> sids) {
		this.summaryIdsNotForDashboard = sids;
	}

	public void removeSummaryIdForDashboard(long id) {
		this.getSummaryIdsForDashboardDirect().remove(id);
	}

	public void removeSummaryIdNotForDashboard(long id) {
		this.getSummaryIdsNotForDashboardDirect().remove(id);
	}

	public int compareTo(CompanyInfo otherCompany) {
		if (this == otherCompany) {
			return 0;
		}
		String compareString = this.getCompanyName().toLowerCase() + this.getInternalCompanyName();
		String otherCompareString = otherCompany.getCompanyName().toLowerCase()
				+ otherCompany.getInternalCompanyName();
		return compareString.compareTo(otherCompareString);
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		return this.getInternalCompanyName().equals(((CompanyInfo) obj).getInternalCompanyName());
	}

	public int hashCode() {
		return this.getInternalCompanyName().hashCode();
	}

	public String toString() {
		return this.getCompanyName();
	}

	private SortedSet<Long> summaryIdsForDashboard = new TreeSet<Long>();

	private SortedSet<Long> summaryIdsNotForDashboard = new TreeSet<Long>();

	private Set<AppUserInfo> usersCollection = new HashSet<AppUserInfo>();

	private Set<AppRoleInfo> roles = new HashSet<AppRoleInfo>();

	private Set<TableInfo> tables = new HashSet<TableInfo>();

	private Set<ModuleInfo> modules = new HashSet<ModuleInfo>();

	private Set<String> tabAddresses = new LinkedHashSet<String>();

	private String companyName = "";

	private String internalCompanyName = null;

	/**
	 * Keep one variable to record when the sparkLines were cached (in epoch
	 * ms). Assumes that all sparkLines will be cached at approx. the same time
	 */
	private long sparkLineCacheTime = 0;

	private Map<String, List<Integer>> cachedSparkLines = new HashMap<String, List<Integer>>();

	private DashboardInfo dashboard = null;

}
