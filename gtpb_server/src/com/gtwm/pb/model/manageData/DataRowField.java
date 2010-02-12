/*
 *  Copyright 2010 GT webMarque Ltd
 * 
 *  This file is part of agileBase.
 *
 *  agileBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  agileBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with agileBase.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gtwm.pb.model.manageData;

import com.gtwm.pb.model.interfaces.DataRowFieldInfo;

public class DataRowField implements DataRowFieldInfo {

	public static final String NULL_COLOR = "";

	private DataRowField() {
	}

	/**
	 * @param numberOfStdDevsFromMean
	 *            Field colour will be calculated from this parameter
	 */
	public DataRowField(String keyValue, String displayValue, double numberOfStdDevsFromMean) {
		this.keyValue = keyValue;
		this.displayValue = displayValue;
		this.numberOfStdDevsFromMean = numberOfStdDevsFromMean;
		this.standardDevHexColor = calcStandardDevHexColour(numberOfStdDevsFromMean);
	}

	/**
	 * @param standardDevHexColor
	 *            Set field colour explicitly
	 */
	public DataRowField(String keyValue, String displayValue, String standardDevHexColor) {
		this.keyValue = keyValue;
		this.displayValue = displayValue;
		this.standardDevHexColor = standardDevHexColor;
	}

	private static String calcStandardDevHexColour(double numberOfStdDevsFromMean) {
		int colourVal = (int) (numberOfStdDevsFromMean * colourScalingFactor);
		int absColourVal = Math.abs(colourVal);
		if (absColourVal > 255) {
			absColourVal = 255;
		}
		String colourRepresentation = Integer.toHexString(255 - absColourVal);
		if (colourRepresentation.length() == 1) {
			colourRepresentation = "0" + colourRepresentation;
		}
		// +ve = green shade, -ve = red
		if (colourVal > 0) {
			colourRepresentation = "#" + colourRepresentation + "ff" + colourRepresentation;
		} else {
			colourRepresentation = "#ff" + colourRepresentation + colourRepresentation;
		}
		return colourRepresentation;
	}

	public DataRowField(String keyValue, String displayValue) {
		this.keyValue = keyValue;
		this.displayValue = displayValue;
		this.standardDevHexColor = DataRowField.NULL_COLOR;
	}

	public String getKeyValue() {
		if (this.keyValue == null) {
			return "";
		} else {
			return this.keyValue;
		}
	}

	public String getDisplayValue() {
		if (this.displayValue == null) {
			return "";
		} else {
			return this.displayValue;
		}
	}

	public double getNumberOfStdDevsFromMean() {
		return this.numberOfStdDevsFromMean;
	}

	public String getStandardDevHexColour() {
		return this.standardDevHexColor;
	}

	public String toString() {
		return this.getDisplayValue();
	}

	private String keyValue = null;

	private String displayValue = null;

	private double numberOfStdDevsFromMean = 0d;

	private String standardDevHexColor = NULL_COLOR;

	/**
	 * Increase this number to make field colours brighter, reduce to make more
	 * pastel. A value of 25 means the brightest green will be about 10 standard
	 * deviations away from the mean and the brightest red about 10 the other
	 * way
	 */
	private static final int colourScalingFactor = 25;

}
