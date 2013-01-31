package com.gtwm.pb.model.interfaces;

import com.gtwm.pb.model.manageSchema.apps.VisualisationApp.VisualisationType;

public interface AppVisualisationInfo extends AppInfo {
	
	public ChartInfo getChart();
	
	public void setChart(ChartInfo chart);
	
	public VisualisationType getVisualisationType();
	
	public void setVisualisationType(VisualisationType visualisationType);
	
	public BaseReportInfo getReport();
	
	public void setReport(BaseReportInfo report);

}
