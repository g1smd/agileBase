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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import org.junit.Test;
import com.gtwm.pb.model.interfaces.TagCloudInfo;
import com.gtwm.pb.model.interfaces.TagInfo;

public class TagCloudTest {

	/**
	 * Check there are no single letter words in output
	 */
	@Test
	public void testNoSingleLetters() {
		Set<String> stopWords = new HashSet<String>();
		TagCloudInfo tagCloud = new TagCloud("a b c d e f g h i j k l m n o p 1,2,3,4.5,6,7;8;9;0",
				1, 10, 50, stopWords);
		SortedSet<TagInfo> tags = tagCloud.getTags();
		assertEquals(tags.size(), 0);
	}

	@Test
	public void testWeights() {
		Set<String> stopWords = new HashSet<String>();
		int minWeight = 10;
		int maxWeight = 20;
		// Check that equal frequency words have the same weight
		TagCloudInfo tagCloud = new TagCloud("Two Words", minWeight, maxWeight, 30, stopWords);
		SortedSet<TagInfo> tags = tagCloud.getTags();
		assertEquals(tags.size(), 2);
		TagInfo firstTag = tags.first();
		TagInfo lastTag = tags.last();
		assertEquals(firstTag.getWeight(), lastTag.getWeight());
		// Check weights are within boundaries
		assertTrue(firstTag.getWeight() >= minWeight && firstTag.getWeight() <= maxWeight);
		assertTrue(lastTag.getWeight() >= minWeight && lastTag.getWeight() <= maxWeight);
	}

	@Test
	public void testLoremIpsum() {
		Set<String> stopWords = new HashSet<String>();
		// Check the largest frequency tag is correct
		TagCloudInfo tagCloud = new TagCloud(
				"Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
				10, 30, 50, stopWords);
		SortedSet<TagInfo> tags = tagCloud.getTags();
		// TODO: check that 'est' is the largest frequency tag
	}

	@Test
	public void testStopWords() {

	}

	/**
	 * A real world example from a client project
	 */
	@Test
	public void testRealWorldExample() {
		String inputString = "Agree mutual expectations and working arrangements Agree objectives and standards of performance Allocate work Assess teams and individuals Build teams Coach individuals Counsel individuals Define the vision and mission Develop individuals Develop teams Develop your management team Dismiss individuals Encourage diversity Encourage diversity and fair working practices Ethical stance Evaluate and improve learning and development Evaluate potential partners Evaluate success and failure Focus on results Give feedback Implement systems for managing information and communication Meet customer specifications Mentor individuals Negotiate and agree the introduction of change Plan human resource requirements Plan work Provide advice and support Review performance Strategic awareness Support the performance of teams and individuals Train people";
		Set<String> stopWords = new HashSet<String>();
		stopWords.add("agree");
		stopWords.add("review");
		stopWords.add("work");
		stopWords.add("obtain");
		stopWords.add("select");
		stopWords.add("establish");
		stopWords.add("provide");
		stopWords.add("individuals");
		TagCloudInfo tagCloud = new TagCloud(inputString, 10,30,40, stopWords);
		SortedSet<TagInfo> tags = tagCloud.getTags();
		for (TagInfo tag : tags) {
			System.out.println(tag.getName() + " : " + tag.getWeight());
		}
		//TODO: tests that at least one tag has maxWeight, at least one has minWeight and there are some inbetween
	}
}
