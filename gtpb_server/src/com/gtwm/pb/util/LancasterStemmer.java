/* 	author:   	Christopher O'Neill
	date:		Aug 2001   	
	comments: 	The Paice/Husk Stemmer Translated from Pascal
			adapted for algorithm demo and test program*/
/**
 * @see http 
 *      ://www.comp.lancs.ac.uk/computing/research/stemming/paice/javademo
 *      .htm
 * 
 * Updated by Oliver Kohll 2011
 */

package com.gtwm.pb.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LancasterStemmer {

	public LancasterStemmer() {
		this.initialiseRules();
	}

	/****************************************************************
	 * Method: stripSuffixes
	 * 
	 * * Returns: String
	 * 
	 * * Recievs: String word - must be lower case
	 * 
	 * * Purpose: strips suffix off word and returns * stem using paice stemming
	 * algorithm *
	 * 
	 * Note by Oliver: due to the local cache, this method is not thread safe
	 ****************************************************************/
	public String stripSuffixes(String word) {
		// First, check cache
		String cachedStem = this.cachedStems.get(word);
		if (cachedStem != null) {
			return cachedStem;
		}
		// integer variables 1 is positive, 0 undecided, -1 negative equivalent
		// of pun vars positive undecided negative
		int ruleOk = 0;
		int continueGoing = 0;

		// integer variables
		int pll = 0; // position of last letter
		int xl; // counter for number of chars to be replaced and length of
				// stemmed word if rule was applied
		int pfv; // position of first vowel
		int prt; // pointer into rule table
		int ir; // index of rule
		int iw; // index of word

		// char variables
		char ll; // last letter

		// String variables equivalent of tenchar variables
		String rule = ""; // variable holding the current rule
		String stem = ""; // string holding the word as it is being stemmed this
							// is returned as a stemmed word.

		// boolean variable
		boolean intact = true; // intact if the word has not yet been stemmed to
								// determine a requirement of some stemming rules

		// set stem = to word (removed .toLowerCase as the input is already lower case)
		stem = word;
		// set the position of pll to the last letter in the string
		pll = 0;
		// move through the word to find the position of the last letter before
		// a non letter char
		while ((pll + 1 < stem.length())
				&& ((stem.charAt(pll + 1) >= 'a') && (stem.charAt(pll + 1) <= 'z'))) {
			pll++;
		}
		if (pll < 1) {
			continueGoing = -1;
		}
		// find the position of the first vowel
		pfv = this.firstVowel(stem, pll);
		iw = stem.length() - 1;
		// repeat until continue == negative i.e. -1
		while (continueGoing != -1) {
			continueGoing = 0;
			// SEEK RULE FOR A NEW FINAL LETTER
			ll = stem.charAt(pll);
			// last letter
			// Check to see if there are any possible rules for stemming
			if ((ll >= 'a') && (ll <= 'z')) {
				prt = this.ruleIndex[charCode(ll)];
				// pointer into rule-table
			} else {
				prt = -1;
				// 0 is a vaild rule
			}
			if (prt == -1) {
				continueGoing = -1;
				// no rule available
			}
			if (continueGoing == 0)
			// THERE IS A POSSIBLE RULE (OR RULES) : SEE IF ONE WORKS
			{
				rule = (String) ruleTable.get(prt);
				// Take first rule
				while (continueGoing == 0) {
					ruleOk = 0;
					if (rule.charAt(0) != ll) {
						// rule-letter changes
						continueGoing = -1;
						ruleOk = -1;
					}
					ir = 1;
					// index of rule: 2nd character
					iw = pll - 1;
					// index of word: next-last letter
					// repeat untill the rule is not undecided find a rule that
					// is acceptable
					while (ruleOk == 0) {
						if ((rule.charAt(ir) >= '0') && (rule.charAt(ir) <= '9'))
						// rule fully matched
						{
							ruleOk = 1;
						} else if (rule.charAt(ir) == '*') {
							// match only if word intact
							if (intact) {
								ir = ir + 1;
								// move forwards along rule
								ruleOk = 1;
							} else {
								ruleOk = -1;
							}
						} else if (rule.charAt(ir) != stem.charAt(iw)) {
							// mismatch of letters
							ruleOk = -1;
						} else if (iw <= pfv) {
							// insufficient stem remains
							ruleOk = -1;
						} else {
							// move on to compare next pair of letters
							ir = ir + 1;
							// move forwards along rule
							iw = iw - 1;
							// move backwards along word
						}
					}
					// if the rule that has just been checked is valid
					if (ruleOk == 1) {
						// CHECK ACCEPTABILITY CONDITION FOR PROPOSED RULE
						xl = 0;
						// count any replacement letters
						while (!((rule.charAt(ir + xl + 1) >= '.') && (rule.charAt(ir + xl + 1) <= '>'))) {
							xl++;
						}
						xl = pll + xl + 48 - ((int) (rule.charAt(ir)));
						// position of last letter if rule used
						if (pfv == 0) {
							// if word starts with vowel...
							if (xl < 1) {
								// ...minimal stem is 2 letters
								ruleOk = -1;
							} else {
								// ruleok=1; as ruleok must already be positive
								// to reach this stage
							}
						}
						// if word start with consonant...
						else if ((xl < 2) | (xl < pfv)) {
							ruleOk = -1;
							// ...minimal stem is 3 letters...
							// ...including one or more vowel
						} else {
							// ruleok=1; as ruleok must already be positive to
							// reach this stage
						}
					}
					// if using the rule passes the assertion tests
					if (ruleOk == 1) {
						// APPLY THE MATCHING RULE
						intact = false;
						// move end of word marker to position...
						// ... given by the numeral.
						pll = pll + 48 - ((int) (rule.charAt(ir)));
						ir++;
						stem = stem.substring(0, (pll + 1));
						// append any letters following numeral to the word
						while ((ir < rule.length())
								&& (('a' <= rule.charAt(ir)) && (rule.charAt(ir) <= 'z'))) {
							stem += rule.charAt(ir);
							ir++;
							pll++;
						}
						// if rule ends with '.' then terminate
						if ((rule.charAt(ir)) == '.') {
							continueGoing = -1;
						} else {
							// if rule ends with '>' then Continue
							continueGoing = 1;
						}
					} else {
						// if rule did not match then look for another
						prt = prt + 1;
						// move to next rule in RULETABLE
						rule = ruleTable.get(prt);
						if (rule.charAt(0) != ll) {
							// rule-letter changes
							continueGoing = -1;
						}
					}
				}
			}
		}
		this.cachedStems.put(word, stem);
		return stem;
	}

	private void initialiseRules() {
		// Assign the number of the first rule that starts with each letter
		// (if any) to an alphabetic array to facilitate selection of sections
		int ruleCount = ruleTable.size();
		char ch = 'a';
		for (int j = 0; j < 25; j++) {
			this.ruleIndex[j] = 0;
		}
		for (int j = 0; j < (ruleCount - 1); j++) {
			while ((ruleTable.get(j)).charAt(0) != ch) {
				ch++;
				this.ruleIndex[charCode(ch)] = j;
			}
		}
	}

	/****************************************************************
	 * Method: charCode
	 * 
	 * * Returns: int
	 * 
	 * * Receives: char ch
	 * 
	 * * Purpose: returns the relevant array index for * specified char 'a' to
	 * 'z' *
	 ****************************************************************/
	private int charCode(char ch) {
		return ((int) ch) - 97;
	}

	/****************************************************************
	 * Method: FirstVowel
	 * 
	 * * Returns: int
	 * 
	 * * Recives: String word, int last
	 * 
	 * * Purpose: checks lower-case word for position of * the first vowel *
	 ****************************************************************/
	private int firstVowel(String word, int last) {
		int i = 0;
		if ((i < last) && (!(this.vowel(word.charAt(i), 'a')))) {
			i++;
		}
		if (i != 0) {
			while ((i < last) && (!(this.vowel(word.charAt(i), word.charAt(i - 1))))) {
				i++;
			}
		}
		if (i < last) {
			return i;
		}
		return last;
	}

	/****************************************************************
	 * Method: vowel
	 * 
	 * * Returns: boolean
	 * 
	 * * Recievs: char ch, char prev
	 * 
	 * * Purpose: determin whether ch is a vowel or not * uses prev
	 * determination when ch == y *
	 ****************************************************************/
	private boolean vowel(char ch, char prev) {
		switch (ch) {
		case 'a':
		case 'e':
		case 'i':
		case 'o':
		case 'u':
			return true;
		case 'y': {
			switch (prev) {
			case 'a':
			case 'e':
			case 'i':
			case 'o':
			case 'u':
				return false;
			default:
				return true;
			}
		}
		default:
			return false;
		}
	}
	
	private Map<String, String> cachedStems = new HashMap<String, String>();
	
	private int[] ruleIndex = new int[26]; // index to above

	/**
	 * Copied from
	 * http://www.comp.lancs.ac.uk/computing/research/stemming/paice/stemrules
	 * .txt
	 * 
	 * April 2011, with spaces removed
	 */
	private static final List<String> ruleTable = Arrays.asList("ai*2.{-ia>-ifintact}",
			"a*1.{-a>-ifintact}", "bb1.{-bb>-b}", "city3s.{-ytic>-ys}", "ci2>{-ic>-}",
			"cn1t>{-nc>-nt}", "dd1.{-dd>-d}", "dei3y>{-ied>-y}", "deec2ss.{-ceed>-cess}",
			"dee1.{-eed>-ee}", "de2>{-ed>-}", "dooh4>{-hood>-}", "e1>{-e>-}",
			"feil1v.{-lief>-liev}", "fi2>{-if>-}", "gni3>{-ing>-}", "gai3y.{-iag>-y}",
			"ga2>{-ag>-}", "gg1.{-gg>-g}", "ht*2.{-th>-ifintact}", "hsiug5ct.{-guish>-ct}",
			"hsi3>{-ish>-}", "i*1.{-i>-ifintact}", "i1y>{-i>-y}",
			"ji1d.{-ij>-id--seenois4j>&vis3j>}", "juf1s.{-fuj>-fus}", "ju1d.{-uj>-ud}",
			"jo1d.{-oj>-od}", "jeh1r.{-hej>-her}", "jrev1t.{-verj>-vert}", "jsim2t.{-misj>-mit}",
			"jn1d.{-nj>-nd}", "j1s.{-j>-s}", "lbaifi6.{-ifiabl>-}", "lbai4y.{-iabl>-y}",
			"lba3>{-abl>-}", "lbi3.{-ibl>-}", "lib2l>{-bil>-bl}", "lc1.{-cl>c}",
			"lufi4y.{-iful>-y}", "luf3>{-ful>-}", "lu2.{-ul>-}", "lai3>{-ial>-}", "lau3>{-ual>-}",
			"la2>{-al>-}", "ll1.{-ll>-l}", "mui3.{-ium>-}", "mu*2.{-um>-ifintact}",
			"msi3>{-ism>-}", "mm1.{-mm>-m}", "nois4j>{-sion>-j}", "noix4ct.{-xion>-ct}",
			"noi3>{-ion>-}", "nai3>{-ian>-}", "na2>{-an>-}", "nee0.{protect-een}", "ne2>{-en>-}",
			"nn1.{-nn>-n}", "pihs4>{-ship>-}", "pp1.{-pp>-p}", "re2>{-er>-}", "rae0.{protect-ear}",
			"ra2.{-ar>-}", "ro2>{-or>-}", "ru2>{-ur>-}", "rr1.{-rr>-r}", "rt1>{-tr>-t}",
			"rei3y>{-ier>-y}", "sei3y>{-ies>-y}", "sis2.{-sis>-s}", "si2>{-is>-}",
			"ssen4>{-ness>-}", "ss0.{protect-ss}", "suo3>{-ous>-}", "su*2.{-us>-ifintact}",
			"s*1>{-s>-ifintact}", "s0.{-s>-s}", "tacilp4y.{-plicat>-ply}", "ta2>{-at>-}",
			"tnem4>{-ment>-}", "tne3>{-ent>-}", "tna3>{-ant>-}", "tpir2b.{-ript>-rib}",
			"tpro2b.{-orpt>-orb}", "tcud1.{-duct>-duc}", "tpmus2.{-sumpt>-sum}",
			"tpec2iv.{-cept>-ceiv}", "tulo2v.{-olut>-olv}", "tsis0.{protect-sist}",
			"tsi3>{-ist>-}", "tt1.{-tt>-t}", "uqi3.{-iqu>-}", "ugo1.{-ogu>-og}", "vis3j>{-siv>-j}",
			"vie0.{protect-eiv}", "vi2>{-iv>-}", "ylb1>{-bly>-bl}", "yli3y>{-ily>-y}",
			"ylp0.{protect-ply}", "yl2>{-ly>-}", "ygo1.{-ogy>-og}", "yhp1.{-phy>-ph}",
			"ymo1.{-omy>-om}", "ypo1.{-opy>-op}", "yti3>{-ity>-}", "yte3>{-ety>-}",
			"ytl2.{-lty>-l}", "yrtsi5.{-istry>-}", "yra3>{-ary>-}", "yro3>{-ory>-}",
			"yfi3.{-ify>-}", "ycn2t>{-ncy>-nt}", "yca3>{-acy>-}", "zi2>{-iz>-}", "zy1s.{-yz>-ys}",
			"end0.");
}
