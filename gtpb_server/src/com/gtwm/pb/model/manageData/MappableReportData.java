/*
 *  Copyright 2009 GT webMarque Ltd
 * 
 *  This file is part of GT portalBase.
 *
 *  GT portalBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  GT portalBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with GT portalBase.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gtwm.pb.model.manageData;

import com.gtwm.pb.model.interfaces.MappableReportDataInfo;
import edu.umd.cs.treemap.Rect;

public class MappableReportData implements MappableReportDataInfo {

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getDepth() {
		return depth;
	}

	public MappableReportData(String caption, String hexColour) {
		this(1, 0, caption, hexColour);
	}

	public MappableReportData(double size, int order, String caption, String hexColour) {
		this.size = size;
		this.order = order;
		this.caption = caption;
		this.hexColour = hexColour;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public Rect getBounds() {
		return bounds;
	}
	
	public int getWidth() {
		return (int) this.bounds.w;
	}
	
	public int getHeight() {
		return (int) this.bounds.h;
	}
	
	public int getLeft() {
		return (int) this.bounds.x;
	}
	
	public int getTop() {
		return (int) this.bounds.y;
	}

	public void setBounds(Rect bounds) {
		this.bounds = bounds;
	}

	public void setBounds(double x, double y, double w, double h) {
		bounds.setRect(x, y, w, h);
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getHexColour() {
		return this.hexColour;
	}
	
	public String getCaption() {
		return this.caption;
	}
	
	public String toString() {
		return this.caption;
	}
	
	private double size;

	private Rect bounds;

	private int order = 0;

	private int depth;
	
	private String hexColour = "";
	
	private String caption = "";
}
