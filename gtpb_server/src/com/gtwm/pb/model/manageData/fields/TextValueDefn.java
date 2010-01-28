/*
 *  Copyright 2009 GT webMarque Ltd
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
import com.gtwm.pb.util.Naming;

public class TextValueDefn implements TextValue {

   private TextValueDefn() {
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
        if (this.textValue.trim().matches("^(((ht|f)tp(s?))\\://)?(www.|[a-zA-Z].)[a-zA-Z0-9\\-\\.]+\\.(com|edu|gov|mil|net|org|biz|info|name|museum|us|ca|uk)(\\:[0-9]+)*(/($|[a-zA-Z0-9\\.\\,\\;\\?\\'\\@\\\\\\+&%\\$#\\=~_\\-]+))*$")) {
            return true;
        }
        return false;
    }
    
    public boolean isPhoneNumber() {
    	if (this.isNull()) {
    		return false;
    	}
        // regex from http://www.regexlib.com/
    	if (this.textValue.trim().matches("^((\\(?0\\d{4}\\)?\\s?\\d{3}\\s?\\d{3})|(\\(?0\\d{3}\\)?\\s?\\d{3}\\s?\\d{4})|(\\(?0\\d{2}\\)?\\s?\\d{4}\\s?\\d{4}))(\\s?\\#(\\d{4}|\\d{3}))?$")) {
    		return true;
    	}
    	return false;
    }
    
    public String getFormattedURL() {
        if (! this.isURL()) {
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
        // regex from http://www.regxlib.com/RETester.aspx?regexp_id=260 doesn't work for newer postcodes
        // used instead http://www.govtalk.gov.uk/gdsc/schemaHtml/bs7666-v2-0-xsd-PostCodeType.htm
        if (this.textValue.toUpperCase().matches("^[A-Z]{1,2}[0-9R][0-9A-Z]?\\s[0-9][A-Z-[CIKMOV]]{2}$")) {
            return true;
        }
        return false;
    }
    
    private String textValue = null;
    
	private static final SimpleLogger logger = new SimpleLogger(TextValueDefn.class);
}
