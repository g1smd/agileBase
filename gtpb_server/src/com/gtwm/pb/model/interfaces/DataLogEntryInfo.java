package com.gtwm.pb.model.interfaces;

import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.util.Enumerations.AppAction;

/**
 * A log entry representing a data change (edit, insert etc.)
 * @author oliver
 *
 */
public interface DataLogEntryInfo {

	/**
	 * Time at which this entry was created
	 */
	public long getTime();
	
	public AppUserInfo getUser();
	
	public BaseField getField();
	
	public int getRowId();
	
	public String getValue();
	
	public AppAction getAppAction();
}
