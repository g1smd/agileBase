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
package com.gtwm.pb.model.manageData;

import com.gtwm.pb.model.interfaces.TagInfo;

public class Tag implements TagInfo, Comparable<TagInfo> {

	private Tag() {
	}

	public Tag(String name, int weight) {
		this.name = name;
		this.weight = weight;
	}

	public String getName() {
		return this.name;
	}

	public int getWeight() {
		return this.weight;
	}

	/**
	 * Tag equality based on name
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		TagInfo otherTag = (TagInfo) obj;
		return (this.getName().equals(otherTag.getName()));
	}

	public int hashCode() {
		return this.name.hashCode();
	}

	/**
	 * Compare in a non-obvious but consistent way - not alphabetical because we
	 * don't want to attach meaning to the ordering
	 */
	public int compareTo(TagInfo otherTag) {
		int hashCode = this.hashCode();
		int otherHashCode = otherTag.getName().hashCode();
		if (hashCode == otherHashCode) {
			return this.getName().compareTo(otherTag.getName());
		}
		if (hashCode > otherHashCode) {
			return 1;
		} else {
			return -1;
		}
	}

	public String toString() {
		return this.getName();
	}

	private String name = "";

	private int weight = 0;
}
