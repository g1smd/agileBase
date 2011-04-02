package com.gtwm.pb.model.manageData;

import java.util.Comparator;

import com.gtwm.pb.model.interfaces.WordInfo;

/**
 * Compares different words in a word cloud by weight
 */
public class WordWeightComparator implements Comparator<WordInfo> {

	public int compare(WordInfo word1, WordInfo word2) {
		// Although this is a weight comparator,
		// If the words are the same, they must still be equal
		if (word1.equals(word2)) {
			return 0;
		}
		int weightComparison = Integer.valueOf(word1.getWeight()).compareTo(
				Integer.valueOf(word2.getWeight()));
		if (weightComparison != 0) {
			return weightComparison;
		}
		return word1.getName().compareTo(word2.getName());
	}

}
