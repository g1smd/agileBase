package com.gtwm.pb.model.manageData;

import java.util.Comparator;

import com.gtwm.pb.model.interfaces.WordInfo;

/**
 * Compares words in a word cloud by weight
 */
public class WordWeightComparator implements Comparator<WordInfo> {

	public int compare(WordInfo word1, WordInfo word2) {
		int weightComparison = Integer.valueOf(word1.getWeight()).compareTo(
				Integer.valueOf(word2.getWeight()));
		if (weightComparison != 0) {
			return weightComparison;
		}
		return word1.getName().compareTo(word2.getName());
	}

}
