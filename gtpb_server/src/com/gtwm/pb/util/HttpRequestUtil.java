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
package com.gtwm.pb.util;

import javax.servlet.http.HttpServletRequest;
import org.grlea.log.SimpleLogger;

public class HttpRequestUtil {

	public static Boolean getBooleanValue(HttpServletRequest request, String param) {
		String booleanValueString = request.getParameter(param);
		return Helpers.valueRepresentsBooleanTrue(booleanValueString);
	}
	
	public static Integer getIntegerValue(HttpServletRequest request, String param, Integer defaultValue) {
		Integer value = defaultValue;
		String valueString = request.getParameter(param);
		if (valueString != null) {
			if (! valueString.equals("")) {
				value = Integer.valueOf(valueString);
			}
		}
		return value;
	}
	
	public static Integer getIntegerValueStrict(HttpServletRequest request, String param, Integer defaultValue, String exceptionMessage)
	throws CantDoThatException {
		try {
			return HttpRequestUtil.getIntegerValue(request, param, defaultValue);
		} catch (NumberFormatException nfex) {
			throw new CantDoThatException(exceptionMessage);
		}
	}
	
	public static Double getDoubleValue(HttpServletRequest request, String param, Double defaultValue) {
		Double value =  defaultValue;
		String valueString = request.getParameter(param);
		if (valueString != null) {
			if (! valueString.equals("")) {
				value = Double.parseDouble(valueString);				
			}
		}
		return value;
	}
	
	public static Double getDoubleValueStrict(HttpServletRequest request, String param, Double defaultValue, String exceptionMessage)
	throws CantDoThatException {
		try {
			return HttpRequestUtil.getDoubleValue(request, param, defaultValue);
		} catch (NumberFormatException nfex) {
			throw new CantDoThatException(exceptionMessage);
		}
	}
	
	public static String getStringValue(HttpServletRequest request, String param) {
		String textValue = request.getParameter(param);
		if (textValue.equals("")) {
			textValue = null;
		}
		return textValue;
	}
	
	private static final SimpleLogger logger = new SimpleLogger(HttpRequestUtil.class);
}
