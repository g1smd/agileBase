package com.gtwm.pb.model.manageSchema.apps;

import javax.persistence.Transient;
import com.gtwm.pb.model.interfaces.AppDataLinkInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.util.RandomString;

public class DataLinkApp extends AbstractApp implements AppDataLinkInfo {

	public DataLinkApp(String colour, BaseReportInfo report) {
		super.setColour(colour);
		super.setInternalAppName(RandomString.generate());
		this.setReport(report);
	}

	@Transient
	public String getAppName() {
		return this.getReport().getParentTable().getSimpleName();
	}

	public BaseReportInfo getReport() {
		return this.report;
	}

	public void setReport(BaseReportInfo report) {
		this.report = report;		
	}
	
	public String toString() {
		return this.getAppName();
	}

	private BaseReportInfo report = null;
}
