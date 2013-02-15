package com.gtwm.pb.model.manageSchema.tiles;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import com.gtwm.pb.model.interfaces.TileVisualisationInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.ChartInfo;
import com.gtwm.pb.model.manageSchema.ChartDefn;
import com.gtwm.pb.model.manageSchema.SimpleReportDefn;
import com.gtwm.pb.util.RandomString;

@Entity
public class VisualisationTile extends AbstractTile implements TileVisualisationInfo {

	private VisualisationTile() {
	}
	
	public VisualisationTile(String colour, VisualisationType visualisationType) {
		super.setColour(colour);
		super.setInternalTileName(RandomString.generate());
		this.setVisualisationType(visualisationType);
	}
	
	@Transient
	public String getTileName() {
		return this.getChart().getTitle();
	}

	@ManyToOne(targetEntity = ChartDefn.class)
	public ChartInfo getChart() {
		return this.chart;
	}

	public void setChart(ChartInfo chart) {
		this.chart = chart;
	}
	
	@Enumerated(EnumType.STRING)
	public VisualisationType getVisualisationType() {
		return visualisationType;
	}

	public void setVisualisationType(VisualisationType visualisationType) {
		this.visualisationType = visualisationType;
	}

	@ManyToOne(targetEntity = SimpleReportDefn.class)
	public BaseReportInfo getReport() {
		return report;
	}

	public void setReport(BaseReportInfo report) {
		this.report = report;
	}

	public String toString() {
		return this.getTileName();
	}
	
	public enum VisualisationType {
		CHART, WORD_CLOUD, MAP
	}
	
	private ChartInfo chart = null;
	
	private VisualisationType visualisationType = null;
	
	private BaseReportInfo report = null;

}
