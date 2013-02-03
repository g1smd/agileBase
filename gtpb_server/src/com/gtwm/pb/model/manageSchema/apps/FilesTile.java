package com.gtwm.pb.model.manageSchema.apps;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import com.gtwm.pb.model.interfaces.TileFilesInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.manageSchema.BaseReportDefn;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.TileType;

@Entity
public class FilesTile extends AbstractTile implements TileFilesInfo {

	public FilesTile(String colour, BaseReportInfo report) {
		super.setColour(colour);
		super.setInternalTileName(RandomString.generate());
		super.setAppType(TileType.FILES);
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
