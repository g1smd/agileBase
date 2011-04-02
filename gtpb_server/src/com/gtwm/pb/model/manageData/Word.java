/*
 *  Copyright 2011 GT webMarque Ltd
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

import java.util.Set;
import com.gtwm.pb.model.interfaces.WordInfo;

public class Word implements WordInfo, Comparable<WordInfo> {

	private Word() {
		this.weight = 0;
		this.name = null;
		this.synonyms = null;
	}

	public Word(String name, int weight) {
		this.name = name;
		this.weight = weight;
		this.synonyms = null;
	}
	
	public Word(String name, int weight, Set<String> synonyms) {
		this.name = name;
		this.weight = weight;
		this.synonyms = synonyms;
		this.synonyms.remove(name);
	}

	public String getName() {
		return this.name;
	}

	public int getWeight() {
		return this.weight;
	}
	
	public Set<String> getSynonyms() {
		return this.synonyms;
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
		WordInfo otherWord = (WordInfo) obj;
		return (this.getName().equals(otherWord.getName()));
	}

	public int hashCode() {
		return this.name.hashCode();
	}

	/**
	 * Compare in a non-obvious but consistent way - not alphabetical because we
	 * don't want to attach meaning to the ordering
	 */
	public int compareTo(WordInfo otherTag) {
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

	private final String name;

	private final int weight;
	
	private final Set<String> synonyms;
}
