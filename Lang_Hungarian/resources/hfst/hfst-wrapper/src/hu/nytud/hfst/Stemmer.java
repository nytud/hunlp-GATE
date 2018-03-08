package hu.nytud.hfst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Stemmer {

	protected enum Flags {
		STEM, PREFIX, COMP_MEMBER, COMP_DELIM,
		COMP_MUST_HAVE, COMP_BEFORE_HYPHEN, STEM_IF_COMP, INT_PUNCT
	}

	protected Map<String, Set<Flags>> tag_config;
	protected Map<String, String> tag_convert;
	protected Map<String, String> tag_replace;

	protected List<Pattern> unwanted_patterns;

	protected String copy2surface;

	public class MorphemeInfo {

		public String lexical = "", surface = "", category = "";
		public boolean isPrefix, isStem, isDerivative, isCompoundMember, isCompoundDelimiter;

		public Set<Flags> flags, flags_conv;

		/*

		protected void setProps(String tag) {
			Set<Flags> f = tag_config.get(tag);
			if (f != null) for (Flags f1 : f) switch (f1) {
				case PREFIX:
					isPrefix = true; break;
				case STEM:
					isStem = true; break;
				case COMP_MEMBER:
					isCompoundMember = true; break;
				case COMP_DELIM:
					isCompoundDelimiter = true; break;
				default:
					break;
			}
			category = tag;
		}

		public MorphemeInfo(Analyzer.Morpheme parent) {
			lexical  = parent.lexical;
			surface  = parent.surface;
			category = null;

			for (String t : parent.tags) {
				// get the flags
				setProps(t);
				// stem category conversion
				String c = tag_convert.get(t);
				if (c != null) setProps(c);
				// replace the tag if necessary
				String r = tag_replace.get(t);
				if (r != null) category = r;
				//tags.add(t);
			}
		}
		 */
	}

	public static class Stem {

		public List<MorphemeInfo> morphs;

		public String szAccentedForm = "", szStem = "";
		public int iStemCode = -1;
		public int nCompounds = 0;
		public boolean bCompoundWord = false;
		public boolean bIncorrectWord = false;
		public List<Integer> compoundDelims;

		public Stem() {
			morphs = new ArrayList<>();
			compoundDelims = new ArrayList<>();
		}

		public String getTags(boolean all) {
			String res = "";
			for (int n = 0; n < morphs.size(); ++n) {
				MorphemeInfo m = morphs.get(n);
				if (!all && n < iStemCode && !m.isPrefix) {
					continue;
				}
				res += "[" + m.category + "]";
			}

			return res;
		}
	}

	public Stemmer(Properties props) {

		String item_sep = props.getProperty("stemmer.item_sep", ";");
		String value_sep = props.getProperty("stemmer.value_sep", "=");

		tag_config = new HashMap<>();
		for (Flags f : Flags.values()) {
			String[] tags = props.getProperty("stemmer." + f.name(), "").split(item_sep);
			for (String t : tags) {
				Set<Flags> flags = tag_config.get(t);
				if (flags == null) {
					flags = new HashSet<>();
					tag_config.put(t, flags);
				}
				flags.add(f);
			}
		}

		tag_convert = new HashMap<>();
		{
			String[] tags = props.getProperty("stemmer.convert", "").split(item_sep);
			for (String t : tags) {
				String[] opt = t.split(value_sep, 2);
				if (opt.length < 2) {
					continue;
				}
				tag_convert.put(opt[0], opt[1]);
			}
		}

		tag_replace = new HashMap<>();
		{
			String[] tags = props.getProperty("stemmer.replace", "").split(item_sep);
			for (String t : tags) {
				String[] opt = t.split(value_sep, 2);
				if (opt.length < 2) {
					continue;
				}
				tag_replace.put(opt[0], opt[1]);
			}
		}

		unwanted_patterns = new ArrayList<>();
		for (int n = 1; true; ++n) {
			String pat = props.getProperty("stemmer.exclude" + Integer.toString(n));
			if (pat == null) {
				break;
			}
			unwanted_patterns.add(Pattern.compile(pat));
		}

		copy2surface = props.getProperty("stemmer.copy2surface", "");
	}

	// rough port from c++
	public Stem process(String input) {
		Stem stem = new Stem();

		for (Pattern p : unwanted_patterns) {
			Matcher m = p.matcher(input);
			if (m.find()) {
				return null;
			}
		}

		/*
		boolean can_be_compound  = false;
		boolean prev_is_compound = false;
		boolean is_compound = false;
		boolean look_for_compound = false;

		int hyph_pos =-1, stem_pos = -1;

		for (Analyzer.Morpheme m : input.morphs) {

			MorphemeInfo m2 = new MorphemeInfo(m);
			stem.morphs.add(m2);
			if (m2.flags.contains(Flags.COMP_MUST_HAVE) || m2.conv_flags.contains(Flags.COMP_MUST_HAVE)) {
				can_be_compound = true;
			}
			// at least 2 compound members next to each other
			if (m2.flags.contains(Flags.COMP_MEMBER)) {
				if (prev_is_compound) is_compound = true;
				prev_is_compound = true;
			} else {
				prev_is_compound = false;

				if (look_for_compound && m2.conv_flags.contains(Flags.COMP_MEMBER)) {
					m2.flags.add(Flags.COMP_MEMBER);
				}
			}

			if (m2.flags.contains(Flags.STEM)) {
				if ("-".equals(m2.lexical)) hyph_pos = stem.morphs.size()-1;
				if (stem_pos == -1) stem_pos = stem.morphs.size()-1;
				if (m2.conv_flags.isEmpty() && !m2.flags.contains(Flags.COMP_MEMBER)) look_for_compound = true;
			}

			//copy2surface

		}


		int compounds = 0;
		for (Morpheme m : stem.morphs) {
			if (m.flags.contains(Flags.COMP_MEMBER)) ++compounds;
		}

		if (!is_compound) {
			is_compound = compounds > 1 && (hyph_pos == -1 || can_be_compound);
			if (is_compound && hyph_pos > 0) {
				if (!stem.morphs.get(hyph_pos-1).flags.contains(Flags.COMP_BEFORE_HYPHEN))
					is_compound = false;
			}
		}

		// 2 stems - "tájlátogató-felvilágosító"
		// or 1 stem + conversion
		if (is_compound) for (Morpheme m : stem.morphs) {
			if (m.flags.contains(Flags.STEM_IF_COMP))
				m.flags.add(Flags.STEM);
		}


		for (int n=stem.morphs.size()-1; n>0; n--) {
			Morpheme m = stem.morphs.get(n);
			// remove internal punctuation from the end
			if (m.flags.contains(Flags.INT_PUNCT))
				m.flags.remove(Flags.STEM);
			// last stem part pos
			if (m.flags.contains(Flags.STEM)) {
				stem_pos = n;
				break;
			}
		}
		 */
		int iState = 0;

		boolean iItIsStem = false;
		boolean bDerivative = false;
		boolean bCompoundMember = false;
		int nMustHaveCompounds = 0;			//how many morphemes with "compound must have" property
		int nLastStemCode = -1;		//last stem position
		int nPrevLastStemCode = -1; //prev state of nLastStemCode
		int iHyphenPos = -1;   //position of a hyphen
		boolean bLookForCompound = false;

		boolean bSurfLexDiff = false;
		boolean sureCompound = false;
		boolean prevCompound = false;

		String szCurCod = "";
		String surface = ""; //lexical prev_lexical, prev_surface;

		MorphemeInfo morph = new MorphemeInfo();

		for (char c : input.toCharArray()) {
			switch (iState) {
				case 0:
					if (c == '[') {
						iState = 1;
						break;
					}
					if (c == '=') {
						iState = 2;
						bSurfLexDiff = true;
						break;
					}
					if (c == '+') {
						bSurfLexDiff = false;
						break; //ignoring '+' in lexical form
					}
					stem.szAccentedForm += c;
					morph.lexical += c;
					break;
				case 1:
					if (c == ']') {
						morph.flags = tag_config.get(szCurCod);
						if (morph.flags == null) {
							morph.flags = new HashSet<>();
						}

						iItIsStem = morph.flags.contains(Flags.STEM);
						bCompoundMember = morph.flags.contains(Flags.COMP_MEMBER);

						//conversion
						String tagc = tag_convert.get(szCurCod);
						bDerivative = tagc != null;
						morph.flags_conv = bDerivative ? tag_config.get(tagc) : null;
						if (morph.flags_conv == null) {
							morph.flags_conv = new HashSet<>();
						}

						//tag replacement
						String r = tag_replace.get(szCurCod);
						if (r != null) {
							szCurCod = r;
							Set<Flags> f2 = tag_config.get(szCurCod);
							if (f2 != null) {
								morph.flags = f2;
							}
						}

						morph.category = szCurCod;
						morph.isStem = iItIsStem;
						morph.isDerivative = bDerivative;
						morph.isCompoundMember = bCompoundMember;
						morph.isCompoundDelimiter = morph.flags.contains(Flags.COMP_DELIM);
						morph.isPrefix = morph.flags.contains(Flags.PREFIX);
						morph.surface = (bSurfLexDiff ? surface : morph.lexical);

						if (morph.flags.contains(Flags.COMP_MUST_HAVE) || (morph.flags_conv != null && morph.flags_conv.contains(Flags.COMP_MUST_HAVE))) {
							nMustHaveCompounds++;
						}

						stem.morphs.add(morph);

						//van-e 2 egymast koveto compound member, (ha igen, tuti osszetett)
						if (prevCompound && bCompoundMember) {
							sureCompound = true;
						}
						prevCompound = bCompoundMember;

						//ha volt mar to es ez kepzo => a konvertaltjait megkeressuk, ha compound member, akkor beallitjuk
						if (bLookForCompound && morph.flags_conv != null && morph.flags_conv.contains(Flags.COMP_MEMBER)) {
							bCompoundMember = true;
							morph.isCompoundMember = true;
						}

						if (iItIsStem) {
							if ("-".equals(morph.lexical)) {
								iHyphenPos = stem.morphs.size() - 1;
							}
							if (stem.iStemCode == -1) {
								stem.iStemCode = stem.morphs.size() - 1;//save pos...
							}
							nLastStemCode = stem.morphs.size() - 1;
							if (nPrevLastStemCode != -1 && !"-".equals(morph.lexical)) {
								boolean convert = false;
								for (int h = nLastStemCode; h >= nPrevLastStemCode; h--) {
									MorphemeInfo m = stem.morphs.get(h);
									if (m.isStem) {
										convert = true;
									}
									if (convert && m.isDerivative) {
										String tagc2 = tag_convert.get(m.category);
										m.category = tagc2;
										m.flags = m.flags_conv;
										if (m.flags != null && m.flags.contains(Flags.STEM)) {
											m.isStem = true;
										}
									}
								}
							}
							nPrevLastStemCode = nLastStemCode;
							if (!bDerivative) {
								bLookForCompound = true; //elso toalkoto utan bekapcsoljuk, ha ez true, akkor keresunk olyan kepzot, ami compound membert csinal belole
							}
						}

						//ha cmember => novelem
						//ha to ES jon egy compoundMember kepzo => novelem
						if (bCompoundMember) {
							stem.nCompounds++;
							bLookForCompound = false;
						}
						morph = new MorphemeInfo();
						szCurCod = "";
						iState = 2;
						break;
					}
					szCurCod += c;
					if (c == '`') {
						szCurCod = ""; //6-3-as szabaly miatt (2011.07.18. NA: "Azt kene csinalni, hogy a morfologia altal visszaadott cimkek elejen levo reszt a `-ig ki kell torolni mielott bármi mást csinalnal")
					}
					break;
				case 2:
					if (c == '+') {
						iState = 0;
						//iLastPlusPos = curr_analysis.szAccentedForm.length();
					} else if (c == '=') {
						iState = 3;
					}
					break;
				case 3:
					//surface form is arriving, it may replace stem
					if (c == '+') {
						iState = 0;
						MorphemeInfo last = stem.morphs.size() > 0 ? stem.morphs.get(stem.morphs.size() - 1) : null;

						if (last != null) {
							surface = Copy2Surface(last.lexical, surface); //copy spec cars from lexical
						}					//if (m_GetCaseFromInput)
						//	CaseConvert(surface, (curr_analysis.morp.end()-1)->lexical/*prev_lexical*/); // lexical gets case state from surface
						//else
						if (stem.nCompounds > 1 && iHyphenPos != stem.morphs.size() - 2/*curr_analysis.bCompoundWord*/) {
							last.lexical = last.lexical.toLowerCase(); //if it is in compound word: lowercase ("WolfGang"=>"Wolfgang")
						}
						if (last != null) {
							last.surface = surface;
						}
						surface = "";
					} else {
						surface += c;
					}
					break;
			}

			if (iState == 5) {
				break;
			}
		}

		if (!surface.isEmpty()) { // surface form es nincs utana semmi
			MorphemeInfo last = stem.morphs.size() > 0 ? stem.morphs.get(stem.morphs.size() - 1) : null;

			if (last != null) {
				surface = Copy2Surface(last.lexical, surface); //copy spec cars from lexical
			}			//if (m_GetCaseFromInput)
			//	CaseConvert(surface, (curr_analysis.morp.end()-1)->lexical/*prev_lexical*/); // lexical gets case state from surface
			//else
			if (stem.nCompounds > 1 && iHyphenPos != stem.morphs.size() - 2/*curr_analysis.bCompoundWord*/) {
				last.lexical = last.lexical.toLowerCase(); //if it is in compound word: lowercase ("WolfGang"=>"Wolfgang")
			}
			if (last != null) {
				last.surface = surface;
			}
		}

		// === creating stem ===
		// is it compound?
		/*
			-ha 2 tove van
			-ha 1 tove + (conv->FN OR stem if compound)


		teszt-esetek:
			nagybefekteto
			husdarabolo
			husdarabologep
			darabolo-evo
			daraboloevo
			darabologep
			Lajos-
			piros-
		 */
		//TODO: es ha tobb kotojel van?
		//"tájlátogató-felvilágosító"
		if (sureCompound) { //curr_analysis.nCompounds > 1){
			//ez biztos osszetett szo, mert 2 egymast koveto compundmember van benne
			//ha nincs benne FN, de kepzett fonev igen, azt megmenti
			//look for stem if compounds
			for (int n = 0; n < stem.morphs.size(); ++n) {
				MorphemeInfo m = stem.morphs.get(n);
				if (m.flags.contains(Flags.STEM_IF_COMP)) {
					m.isStem = true;
					m.category = tag_convert.get(m.category);
					m.flags = m.flags_conv;
					stem.iStemCode = n;
					if (n >= nLastStemCode) {
						nLastStemCode = n;
					}
				}
			}
		}

		boolean compound = stem.nCompounds > 1 && (iHyphenPos == -1 || nMustHaveCompounds > 0);
		if (iHyphenPos > 0 && compound) {
			//kotojeles akkor lehet osszetett szo, ha a kotojel elott [compound before hyphen] all
			//"aa[FN][NOM]-bb[FN][NOM]" vagy "aa[FN]-bb[FN]"
			//pl "Arpad-haz"

			MorphemeInfo m = stem.morphs.get(iHyphenPos - 1);
			if (m.flags.contains(Flags.COMP_BEFORE_HYPHEN)) {
				//ha a kotojel elotti ures es az azt megelozo toalkoto =>
				if (iHyphenPos > 1 && m.lexical.isEmpty() && m.surface.isEmpty() && !stem.morphs.get(iHyphenPos - 2).isStem) {
					compound = false;
				}
			} else {
				compound = false; //ha a kotojel elott rag van, akkor ez nem osszetett szo
			}
		}

		stem.bCompoundWord = compound;

		boolean internalPunct = false;
		//most megmentjuk attol, hogy a PUNCT, PER vegu szavak to tipusa PUNCT legyen
		for (int n = stem.morphs.size() - 1; n > 0; n--) {
			MorphemeInfo m = stem.morphs.get(n);
			if (!m.flags.contains(Flags.INT_PUNCT)) {
				break;
			}
			internalPunct = true;
			m.isStem = false;
		}
		while (nLastStemCode > 0 && !stem.morphs.get(nLastStemCode).isStem) {
			nLastStemCode--;
		}

		if (compound && !sureCompound) {
			//osszetett szavaknal a stemIfCompoundokat atalakitja
			for (int n = 0; n < stem.morphs.size(); ++n) {
				MorphemeInfo m = stem.morphs.get(n);
				if (m.flags.contains(Flags.STEM_IF_COMP)) {
					m.isStem = true;
					m.category = tag_convert.get(m.category);
					m.flags = m.flags_conv;
					if (n >= nLastStemCode) {
						nLastStemCode = n;
					}
				}
			}
		}

		//osszetett szavaknal beteszi a + jelet...
		int coffset = 0;
		for (MorphemeInfo m : stem.morphs) {
			if (m.isCompoundMember || m.isCompoundDelimiter) {
				if (coffset != 0) {
					stem.compoundDelims.add(coffset); //az utolso nem kell: ott mar vege a szonak
				}
				coffset += m.surface.length();
			}
		}

		boolean internalPunctAND = true;
		if (internalPunct && iHyphenPos > 0) {
			//vegen van egy kotojel, ha elotte ragozoztt szo all, nem lehet szoosszetetel
			//pl. "magan-"
			MorphemeInfo m = stem.morphs.get(iHyphenPos - 1);
			if (m.flags.contains(Flags.COMP_BEFORE_HYPHEN)) {
				//ha a kotojel elotti ures es az azt megelozo toalkoto =>
				if (iHyphenPos > 1 && m.lexical.isEmpty() && m.surface.isEmpty() && !stem.morphs.get(iHyphenPos - 2).isStem) {
					//hadd eljen, nem megy bele az ikerszo agba
				} else {
					internalPunctAND = false; // ez mar ikerszo nem lehet
				}
			}
		}

		// beleegetjuk hogy a szokozi kotojel stem
		for (int n = 1; n < stem.morphs.size() - 1; n++) {
			if (!stem.morphs.get(n - 1).isStem || !stem.morphs.get(n + 1).isStem) {
				continue;
			}
			MorphemeInfo m = stem.morphs.get(n);
			if ("-".equals(m.surface) || "-".equals(m.lexical)) {
				m.isStem = true;
			}
		}

		if (internalPunctAND && iHyphenPos != -1 && !compound) {
			//ikerszo

			boolean half = false;
			int halfPos = stem.iStemCode;//iHyphenPos;//nLastStemCode;//;
			for (int z = (iHyphenPos > 0 ? iHyphenPos - 1 : 0); z > 0; z--) {
				if (stem.morphs.get(z).isStem) {
					halfPos = z;
					break;
				}
			}
			String tmp1 = "", tmp2 = "";
			for (int n = 0; n < stem.morphs.size(); n++) {
				MorphemeInfo m = stem.morphs.get(n);
				if ("-".equals(m.lexical)) {
					half = true;
					halfPos = nLastStemCode;
				}
				if (m.isStem) {
					if (n < halfPos) {
						stem.szStem += !m.surface.isEmpty() ? m.surface : m.lexical;
					} else {
						stem.szStem += m.lexical;
					}
				} else {
					if (!half) {
						tmp1 += m.category + " ";
					} else {
						tmp2 += m.category + " ";
					}
				}
			}
			if (!tmp1.equals(tmp2)) {
				//BAD input, stem is dropped
				stem.bIncorrectWord = true;
				stem.szStem += "<incorrect word>";
				//return 0;
			}

		} else {
			//simple case

			if (stem.morphs.size() >= nLastStemCode) {
				for (int n = 0; n <= nLastStemCode; n++) {
					if (!stem.morphs.get(n).isStem) {
						continue;
					}
					if (n < nLastStemCode) {
						stem.szStem += stem.morphs.get(n).surface;
					} else if (n == nLastStemCode/*curr_analysis.iStemCode*/) {
						stem.szStem += stem.morphs.get(n).lexical;
					}
				}
			}
		}
		stem.iStemCode = nLastStemCode;

//			if (m_regexp_stem_decision)
//			{
//				//call regular function
//				SelectStem(curr_analysis);
//			}
		return stem;
	}

	private String Copy2Surface(String in, String out) {
		if (copy2surface.isEmpty()) {
			return out; //nothing to do :)
		}
		for (int i = 0; i < out.length(); i++) {
			if (i >= in.length()) {
				return out;
			}

			if (copy2surface.indexOf(in.charAt(i)) != -1) {
				out = out.substring(0, i) + in.charAt(i) + out.substring(i);
			} else if (in.charAt(i) != out.charAt(i)) {
				return out;
			}
		}

		return out;
	}

}
