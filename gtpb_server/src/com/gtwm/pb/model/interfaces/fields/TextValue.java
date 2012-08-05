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
package com.gtwm.pb.model.interfaces.fields;

/**
 * Stores a text value and contains methods to test whether the value is of a particular type, e.g. URL, email
 * address or postcode
 */
public interface TextValue extends BaseValue {

	public String toXmlString();

	public boolean isEmailAddress();

	public boolean isURL();

	public boolean isImage();

	public boolean isTwitterName();

	public boolean isTwitterHashTag();

	public boolean isPhoneNumber();

	public boolean isPhoneNumberGB();

	public boolean isPhoneNumberInternational();

 /**
  * If the value is a URL that hasn't got http:// in front of it, then put http:// in front of it to ensure
  * web browsers load it properly from a link
  */
	public String getFormattedURL();

	public boolean isPostcode();
}