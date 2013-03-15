package com.gtwm.pb.model.manageSchema.tiles;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import com.gtwm.pb.model.interfaces.TileDataLinkInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.manageSchema.BaseReportDefn;
import com.gtwm.pb.util.Enumerations.TileType;
import com.gtwm.pb.util.RandomString;

@Entity
public class DataLinkTile extends AbstractTile implements TileDataLinkInfo {

	private DataLinkTile() {
	}
	
	public DataLinkTile(String colour, BaseReportInfo report, String icon) {
		super.setColour(colour);
		super.setInternalTileName(RandomString.generate());
		super.setTileType(TileType.DATA_LINK);
		if (icon == null) {
			icon = report.getModule().getIconPath();
		}
		super.setIcon(icon);
		this.setReport(report);
	}

	@Transient
	@Override
	public String getTileName() {
		return this.getReport().getParentTable().getSimpleName();
	}

	@ManyToOne(targetEntity = BaseReportDefn.class)
	public BaseReportInfo getReport() {
		return this.report;
	}

	public void setReport(BaseReportInfo report) {
		this.report = report;		
	}
	
	private BaseReportInfo report = null;
}
