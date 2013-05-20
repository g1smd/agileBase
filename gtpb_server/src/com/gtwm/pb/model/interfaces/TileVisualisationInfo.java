package com.gtwm.pb.model.interfaces;

import com.gtwm.pb.model.manageSchema.tiles.VisualisationTile.VisualisationType;

public interface TileVisualisationInfo extends TileInfo {

	public ChartInfo getChart();

	public void setChart(ChartInfo chart);

	public VisualisationType getVisualisationType();

	public void setVisualisationType(VisualisationType visualisationType);

	public BaseReportInfo getReport();

	public void setReport(BaseReportInfo report);

}
