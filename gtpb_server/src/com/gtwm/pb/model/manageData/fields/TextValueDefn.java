/*
 *  Copyright 2012 GT webMarque Ltd
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
package com.gtwm.pb.model.manageData.fields;

import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.fields.TextValue;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.Naming;

public class TextValueDefn implements TextValue {

	private TextValueDefn() {
		this.textValue = null;
	}

	public TextValueDefn(String textValue) {
		this.textValue = textValue;
	}

	public String toString() {
		if (this.textValue == null) {
			return "";
		} else {
			return this.textValue;
		}
	}

	public String toXmlString() {
		if (this.textValue == null) {
			return "";
		} else {
			return Naming.makeValidXML(this.textValue);
		}
	}

	public boolean isNull() {
		return (this.textValue == null);
	}

	public boolean isEmailAddress() {
		if (this.isNull()) {
			return false;
		}
		// regex from http://www.regexlib.com/
		if (this.textValue.trim().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
			return true;
		}
		return false;
	}

	public boolean isURL() {
		if (this.isNull()) {
			return false;
		}
		// regex from http://www.regexlib.com/
		// with an addition to allow @ signs in URLs, e.g. for
		// http://www.flickr.com/photos/14516334@N00/345009210/
		if (this.textValue
				.trim()
				.matches(
						"^(((ht|f)tp(s?))\\://)?(www.|[a-zA-Z].)[a-zA-Z0-9\\-\\.]+\\.(com|edu|gov|mil|net|org|biz|info|name|museum|us|ca|uk)(\\:[0-9]+)*(/($|[a-zA-Z0-9\\.\\,\\;\\?\\'\\@\\\\\\+&%\\$#\\=~_\\-]+))*$")) {
			return true;
		}
		return false;
	}

	public boolean isImage() {
		return Helpers.isImage(this.textValue);
	}

	public boolean isTwitterName() {
		if (this.isNull()) {
			return false;
		}
		if (this.textValue.trim().matches("^@([A-Za-z0-9_]+)$")) {
			return true;
		}
		return false;
	}

	public boolean isTwitterHashTag() {
		if (this.isNull()) {
			return false;
		}
		if (this.textValue.trim().matches("^#([A-Za-z0-9_]+)$")) {
			return true;
		}
		return false;
	}

	public boolean isPhoneNumberGB() {
		if (this.isNull()) {
			return false;
		}
		int length = this.textValue.trim().length();
		if ((length > 9) && (length < 20)) {
			// regex from http://www.regexlib.com/
			// alteration by g1smd
			// "^((\\(?0\\d{5}\\)?\\s?\\d{4,5})|(\\(?0\\d{4}\\)?\\s?(\\d{3}\\s?\\d{3}|\\d{5}))|(\\(?0\\d{3}\\)?\\s?(\\d{3}\\s?\\d{4}|\\d{6}))|(\\(?0\\d{2}\\)?\\s?\\d{4}\\s?\\d{4}))(\\s?\\#\\d{3,4})?$"
			String regex = "^";
			regex += "\\(?0"; // leading optional "(" and leading "0"
			regex += "(";
			regex += "(\\d{5}\\)?\\s?\\d{4,5})|"; // [5+4]/[5+5]
			regex += "(\\d{4}\\)?\\s?(\\d{5}|\\d{3}\\s?\\d{3}))|"; // [4+5]/[4+6]
			regex += "(\\d{3}\\)?\\s?(\\d{6}|\\d{3}\\s?\\d{4}))|"; // [3+6]/[3+7]
			regex += "(\\d{2}\\)?\\s?\\d{4}\\s?\\d{4})"; // [2+8]
			regex += ")";
			regex += "(\\s?\\#\\d{3,4})?"; // optional "#" and extension
			regex += "$";
			if (this.textValue.trim().matches(regex)) {
				return true;
			}
		}
		return false;
	}

	public String getFormattedURL() {
		if (!this.isURL()) {
			return null;
		}
		if (this.textValue.contains("://")) {
			return this.textValue.trim();
		} else {
			return "http://" + this.textValue.trim();
		}
	}

	public boolean isPostcode() {
		if (this.isNull()) {
			return false;
		}
		// regex from http://www.regxlib.com/RETester.aspx?regexp_id=260 doesn't
		// work for newer postcodes
		// used instead
		// http://www.govtalk.gov.uk/gdsc/schemaHtml/bs7666-v2-0-xsd-PostCodeType.htm
		if (this.textValue.trim().toUpperCase()
				.matches("^[A-Z]{1,2}[0-9R][0-9A-Z]?\\s[0-9][A-Z-[CIKMOV]]{2}$")) {
			return true;
		}
		return false;
	}

	private final String textValue;

	private static final SimpleLogger logger = new SimpleLogger(TextValueDefn.class);
}