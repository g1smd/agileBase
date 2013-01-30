package com.gtwm.pb.model.manageSchema.apps;

import com.gtwm.pb.model.interfaces.AppVisualisationInfo;
import com.gtwm.pb.model.interfaces.ChartInfo;
import com.gtwm.pb.util.RandomString;

public class VisualisationApp extends AbstractApp implements AppVisualisationInfo {

	public VisualisationApp(String colour, ChartInfo chart) {
		super.setColour(colour);
		super.setInternalAppName(RandomString.generate());
		this.setChart(chart);
	}
	
	public String getAppName() {
		return this.chart.getTitle();
	}

	public ChartInfo getChart() {
		return this.chart;
	}

	public void setChart(ChartInfo chart) {
		this.chart = chart;
	}
	
	public String toString() {
		return this.getAppName();
	}
	
	private ChartInfo chart = null;

}
