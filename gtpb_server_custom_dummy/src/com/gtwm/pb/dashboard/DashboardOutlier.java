package com.gtwm.pb.dashboard;

import com.gtwm.pb.dashboard.interfaces.DashboardOutlierInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.DataRowFieldInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.DateValue;

public class DashboardOutlier implements DashboardOutlierInfo {

	public DataRowFieldInfo getDataRowField() {
		// TODO Auto-generated method stub
		return null;
	}

	public BaseField getField() {
		// TODO Auto-generated method stub
		return null;
	}

	public DateValue getModificationDate() {
		// TODO Auto-generated method stub
		return null;
	}

	public BaseReportInfo getReport() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getRowID() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int getOutlierCount() {
		return 0;
	}

	public OutlierType getOutlierType() {
		// TODO Auto-generated method stub
		return null;
	}

}
