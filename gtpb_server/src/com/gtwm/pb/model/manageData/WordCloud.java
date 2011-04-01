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

import com.gtwm.pb.model.interfaces.WordCloudInfo;
import com.gtwm.pb.model.interfaces.WordInfo;
import org.apache.commons.math.stat.Frequency;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.grlea.log.SimpleLogger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;

public class WordCloud implements WordCloudInfo {

	private WordCloud() {
	}

	/**
	 * @param textLowerCase
	 *            Input text, must be lower case
	 * @param minWeight
	 *            Minimum tag weight, e.g. a font size
	 * @param maxWeight
	 *            Max. tag weight
	 * @param maxTags
	 *            Maximum number of tags to return, -1 for all tags
	 * @param additionalStopWords
	 *            Set of words to specifically exclude, in addition to the
	 *            standard set [and, not, after, yes, no, ...]
	 */
	public WordCloud(String textLowerCase, int minWeight, int maxWeight, int maxTags,
			Set<String> additionalStopWords) {
		String[] wordArray = textLowerCase.split("\\W");
		Set<String> stopWords = new HashSet<String>(Arrays.asList(stopWordsArray));
		for (String additionalStopWord : additionalStopWords) {
			stopWords.add(additionalStopWord.toLowerCase().trim());
		}
		Frequency frequencies = new Frequency();
		for (String word : wordArray) {
			if ((!stopWords.contains(word)) && (word.length() >= minWordLength)) {
				frequencies.addValue(word);
			}
		}
		// Compute std. dev of frequencies so we can remove outliers
		DescriptiveStatistics stats = new DescriptiveStatistics();
		Iterator freqIt = frequencies.valuesIterator();
		long wordFreq;
		while (freqIt.hasNext()) {
			wordFreq = frequencies.getCount(freqIt.next());
			stats.addValue(wordFreq);
		}
		double mean = stats.getMean();
		double stdDev = stats.getStandardDeviation();
		long minFreq = Long.MAX_VALUE;
		long maxFreq = 0;
		// Remove outliers
		freqIt = frequencies.valuesIterator();
		int upperLimit = (int) (mean + (stdDev * 4));
		int lowerLimit = (int) (mean - stdDev);
		if (lowerLimit < 2) {
			lowerLimit = 2;
		}
		String word;
		int numWords = 0;
		int numRawWords = wordArray.length;
		boolean removeLowOutliers = (numRawWords > (maxTags * 10));
		while (freqIt.hasNext()) {
			word = (String) freqIt.next();
			wordFreq = frequencies.getCount(word);
			// For a large input set, remove high and low outliers.
			// For a smaller set, just high freq. outliers
			if ((wordFreq > upperLimit) || ((wordFreq < lowerLimit) && removeLowOutliers)) {
				freqIt.remove();
			} else {
				numWords++;
				if (wordFreq > maxFreq) {
					maxFreq = wordFreq;
				} else if (wordFreq < minFreq) {
					minFreq = wordFreq;
				}
			}
		}
		// Cut down to exact required number of tags by removing smallest
		if (lowerLimit < minFreq) {
			lowerLimit = (int) minFreq;
		}
		if (numWords > maxTags) {
			while (numWords > maxTags) {
				freqIt = frequencies.valuesIterator();
				SMALLREMOVAL: while (freqIt.hasNext()) {
					wordFreq = frequencies.getCount(freqIt.next());
					if (wordFreq < lowerLimit) {
						freqIt.remove();
						numWords--;
						if (numWords == maxTags) {
							break SMALLREMOVAL;
						}
					}
				}
				int step = (int) ((mean - lowerLimit) / 3);
				if (step < 1) {
					step = 1;
				}
				lowerLimit += step;
			}
			// The new min. freq. may have changed
			minFreq = Long.MAX_VALUE;
			freqIt = frequencies.valuesIterator();
			while (freqIt.hasNext()) {
				wordFreq = frequencies.getCount(freqIt.next());
				if (wordFreq < minFreq) {
					minFreq = wordFreq;
				}
			}
		}
		// Scale and create tag objects
		double scaleFactor = new Double(maxWeight - minWeight) / new Double(maxFreq - minFreq);
		freqIt = frequencies.valuesIterator();
		int weight;
		while (freqIt.hasNext()) {
			word = (String) freqIt.next();
			if (maxFreq == minFreq) {
				weight = minWeight + ((maxWeight - minWeight) / 4);
			} else {
				wordFreq = frequencies.getCount(word);
				// Might still be some left less than the min. threshold
				if (wordFreq <= minFreq) {
					weight = minWeight;
				} else {
					weight = (int) (Math.ceil(new Double(wordFreq - minFreq) * scaleFactor) + minWeight);
				}
				WordInfo tag = new Word(word, weight);
				this.tags.add(tag);
			}
		}
	}

	public SortedSet<WordInfo> getWords() {
		return this.tags;
	}

	private SortedSet<WordInfo> tags = new TreeSet<WordInfo>();

	static private final int minWordLength = 3;
	
	/**
	 * We don't want these
	 */
	static private final String[] stopWordsArray = { "a", "about", "above", "accordingly", "after",
			"again", "against", "ah", "all", "also", "although", "always", "am", "among",
			"amongst", "an", "and", "any", "anymore", "anyone", "are", "as", "at", "away", "be",
			"been", "begin", "beginning", "beginnings", "begins", "begone", "begun", "being",
			"below", "between", "but", "by", "ca", "can", "cannot", "come", "could", "did", "do",
			"doing", "during", "each", "either", "else", "end", "et", "etc", "even", "ever", "far",
			"ff", "following", "for", "from", "further", "furthermore", "get", "go", "goes",
			"going", "got", "had", "has", "have", "he", "her", "hers", "herself", "him", "himself",
			"his", "how", "i", "if", "in", "into", "is", "it", "its", "itself", "last", "lastly",
			"less", "many", "may", "me", "might", "more", "must", "my", "myself", "near", "nearly",
			"never", "new", "next", "no", "not", "now", "o", "of", "off", "often", "oh", "on",
			"only", "or", "other", "otherwise", "our", "ourselves", "out", "over", "perhaps",
			"put", "puts", "quite", "s", "said", "saw", "say", "see", "seen", "shall", "she",
			"should", "since", "so", "some", "such", "t", "than", "that", "the", "their", "them",
			"themselves", "then", "there", "therefore", "these", "they", "this", "those", "though",
			"throughout", "thus", "to", "too", "toward", "unless", "until", "up", "upon", "us",
			"ve", "very", "was", "we", "were", "what", "whatever", "when", "where", "which",
			"while", "who", "whom", "whomever", "whose", "why", "with", "within", "without",
			"would", "yes", "your", "yours", "yourself", "yourselves", "quot", "amp", "doesn",
			"better", "rather", "you", "most", "need", "however", "whilst", "because" };

	private static final SimpleLogger logger = new SimpleLogger(WordCloud.class);

}
