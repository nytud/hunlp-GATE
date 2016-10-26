package hu.nytud.gate.util;

import java.util.*;

/**
 * Util for obtaining pos and features from hfst output for parsers.
 * Created by zsibritajanos on 2016.08.02..
 * @author Janos Zsibrita
 */

public class DepTool {

  /**
   * Default
   */
  private static final String FEATURED_DEF_VALUE = "none";

  /**
   * NOUN
   */
  private static final Map<String, String> NOUN_DEF_MAP = new LinkedHashMap<>();

  static {
    NOUN_DEF_MAP.put("SubPOS", "c");
    NOUN_DEF_MAP.put("Num", "s");
    NOUN_DEF_MAP.put("Cas", "n");
    NOUN_DEF_MAP.put("NumP", FEATURED_DEF_VALUE);
    NOUN_DEF_MAP.put("PerP", FEATURED_DEF_VALUE);
    NOUN_DEF_MAP.put("NumPd", FEATURED_DEF_VALUE);
  }

  /**
   * VERB
   */
  private static final Map<String, String> VERB_DEF_MAP = new LinkedHashMap<>();

  static {
    VERB_DEF_MAP.put("SubPOS", "m");
    VERB_DEF_MAP.put("Mood", "i");
    VERB_DEF_MAP.put("Tense", "s");
    VERB_DEF_MAP.put("Per", FEATURED_DEF_VALUE);
    VERB_DEF_MAP.put("Num", FEATURED_DEF_VALUE);
    VERB_DEF_MAP.put("Def", FEATURED_DEF_VALUE);
  }

  /**
   * DET
   */
  private static final Map<String, String> DET_DEF_MAP = new LinkedHashMap<>();

  static {
    DET_DEF_MAP.put("SubPOS", "f");
  }

  /**
   * CONJ
   */
  private static final Map<String, String> CONJ_DEF_MAP = new LinkedHashMap<>();

  static {
    CONJ_DEF_MAP.put("SubPOS", "c");
    CONJ_DEF_MAP.put("Form", "s");
    CONJ_DEF_MAP.put("Coord", "w");
  }


  /**
   * ADV
   */
  private static final Map<String, String> ADV_DEF_MAP = new LinkedHashMap<>();

  static {
    ADV_DEF_MAP.put("SubPOS", "x");
    ADV_DEF_MAP.put("Deg", FEATURED_DEF_VALUE);
    ADV_DEF_MAP.put("Num", FEATURED_DEF_VALUE);
    ADV_DEF_MAP.put("Per", FEATURED_DEF_VALUE);
  }

  /**
   * ADJ
   */
  private static final Map<String, String> ADJ_DEF_MAP = new LinkedHashMap<>();

  static {
    ADJ_DEF_MAP.put("SubPOS", "f");
    ADJ_DEF_MAP.put("Deg", "p");
    ADJ_DEF_MAP.put("Num", "s");
    ADJ_DEF_MAP.put("Cas", "n");
    ADJ_DEF_MAP.put("NumP", FEATURED_DEF_VALUE);
    ADJ_DEF_MAP.put("PerP", FEATURED_DEF_VALUE);
    ADJ_DEF_MAP.put("NumPd", FEATURED_DEF_VALUE);
  }

  /**
   * NUM
   */
  private static final Map<String, String> NUM_DEF_MAP = new LinkedHashMap<>();

  static {
    NUM_DEF_MAP.put("SubPOS", "c");
    NUM_DEF_MAP.put("Num", "s");
    NUM_DEF_MAP.put("Cas", "n");
    NUM_DEF_MAP.put("Form", "d");
    NUM_DEF_MAP.put("NumP", FEATURED_DEF_VALUE);
    NUM_DEF_MAP.put("PerP", FEATURED_DEF_VALUE);
    NUM_DEF_MAP.put("NumPd", FEATURED_DEF_VALUE);
  }

  /**
   * INJ
   */
  private static final Map<String, String> INJ_DEF_MAP = new LinkedHashMap<>();

  static {
    INJ_DEF_MAP.put("SubPOS", "o");
  }

  /**
   * POST
   */
  private static final Map<String, String> POST_DEF_MAP = new LinkedHashMap<>();

  static {
    POST_DEF_MAP.put("SubPOS", "t");
  }


  /**
   * NOUN
   */
  private static final Map<String, List<ConllPair>> NOUN_INFRA_FEATURE_MAP = new TreeMap<>();

  static {
    // cas
    NOUN_INFRA_FEATURE_MAP.put("[Supe]", Arrays.asList(new ConllPair("Cas", "p")));
    NOUN_INFRA_FEATURE_MAP.put("[Ins]", Arrays.asList(new ConllPair("Cas", "i")));

    NOUN_INFRA_FEATURE_MAP.put("[Dat]", Arrays.asList(new ConllPair("Cas", "g")));
    NOUN_INFRA_FEATURE_MAP.put("[All]", Arrays.asList(new ConllPair("Cas", "t")));

    NOUN_INFRA_FEATURE_MAP.put("[Nom]", Arrays.asList(new ConllPair("Cas", "n")));

    NOUN_INFRA_FEATURE_MAP.put("[Acc]", Arrays.asList(new ConllPair("Cas", "a")));
    NOUN_INFRA_FEATURE_MAP.put("[Subl]", Arrays.asList(new ConllPair("Cas", "s")));

    NOUN_INFRA_FEATURE_MAP.put("[Ine]", Arrays.asList(new ConllPair("Cas", "2")));
    NOUN_INFRA_FEATURE_MAP.put("[Ade]", Arrays.asList(new ConllPair("Cas", "3")));

    NOUN_INFRA_FEATURE_MAP.put("[Pl]", Arrays.asList(new ConllPair("Num", "p")));

    NOUN_INFRA_FEATURE_MAP.put("[Poss.3Sg]", Arrays.asList(new ConllPair("Num", "s"), new ConllPair("NumP", "s"), new ConllPair("PerP", "3")));
    NOUN_INFRA_FEATURE_MAP.put("[Pl.Poss.3Sg]", Arrays.asList(new ConllPair("Num", "p"), new ConllPair("NumP", "s"), new ConllPair("PerP", "3")));
  }

  /**
   * VERB
   */
  private static final Map<String, List<ConllPair>> VERB_INFRA_FEATURE_MAP = new TreeMap<>();

  static {
    VERB_INFRA_FEATURE_MAP.put("[Pst.NDef.3Sg]", Arrays.asList(new ConllPair("Per", "3"), new ConllPair("Num", "s"), new ConllPair("Def", "n")));
    VERB_INFRA_FEATURE_MAP.put("[Pst.Def.3Sg]", Arrays.asList(new ConllPair("Per", "3"), new ConllPair("Num", "s"), new ConllPair("Def", "y")));

    VERB_INFRA_FEATURE_MAP.put("[Prs.NDef.3Sg]", Arrays.asList(new ConllPair("Tense", "p"), new ConllPair("Per", "3"), new ConllPair("Num", "s"), new ConllPair("Def", "n")));
    VERB_INFRA_FEATURE_MAP.put("[Prs.NDef.3Pl]", Arrays.asList(new ConllPair("Tense", "p"), new ConllPair("Per", "3"), new ConllPair("Num", "p"), new ConllPair("Def", "n")));

    VERB_INFRA_FEATURE_MAP.put("[Prs.Def.3Sg]", Arrays.asList(new ConllPair("Tense", "p"), new ConllPair("Per", "3"), new ConllPair("Num", "s"), new ConllPair("Def", "y")));
    VERB_INFRA_FEATURE_MAP.put("[Prs.Def.3Pl]", Arrays.asList(new ConllPair("Tense", "p"), new ConllPair("Per", "3"), new ConllPair("Num", "p"), new ConllPair("Def", "y")));
    VERB_INFRA_FEATURE_MAP.put("[Prs.NDef.2Pl]", Arrays.asList(new ConllPair("Tense", "s"), new ConllPair("Per", "3"), new ConllPair("Num", "p"), new ConllPair("Def", "n")));

    VERB_INFRA_FEATURE_MAP.put("[Pst.NDef.3Pl]", Arrays.asList(new ConllPair("Per", "3"), new ConllPair("Num", "p"), new ConllPair("Def", "n")));
    VERB_INFRA_FEATURE_MAP.put("[Pst.Def.3Pl]", Arrays.asList(new ConllPair("Tense", "s"), new ConllPair("Per", "3"), new ConllPair("Num", "p"), new ConllPair("Def", "y")));

    VERB_INFRA_FEATURE_MAP.put("[Inf]", Arrays.asList(new ConllPair("Mood", "i"), new ConllPair("Num", "p"), new ConllPair("Def", "n")));

    VERB_INFRA_FEATURE_MAP.put("[_Caus/V]", Arrays.asList(new ConllPair("SubPOS", "s")));
    VERB_INFRA_FEATURE_MAP.put("[_Mod/V]", Arrays.asList(new ConllPair("SubPOS", "o")));
    VERB_INFRA_FEATURE_MAP.put("[_Freq/V]", Arrays.asList(new ConllPair("SubPOS", "f")));
  }

  /**
   * CONJ
   */
  private static final Map<String, List<ConllPair>> CONJ_INFRA_FEATURE_MAP = new TreeMap<>();

  static {
    //
  }

  /**
   * ADJ
   */
  private static final Map<String, List<ConllPair>> ADJ_INFRA_FEATURE_MAP = new TreeMap<>();

  static {
    ADJ_INFRA_FEATURE_MAP.put("[_Manner/Adv]", Arrays.asList(new ConllPair("Cas", "w")));
    ADJ_INFRA_FEATURE_MAP.put("[_Comp/Adj][Nom]", Arrays.asList(new ConllPair("Deg", "c")));
    ADJ_INFRA_FEATURE_MAP.put("[/Adj][Ade]", Arrays.asList(new ConllPair("Cas", "3")));
  }

  /**
   * NUM
   */
  private static final Map<String, List<ConllPair>> NUM_INFRA_FEATURE_MAP = new TreeMap<>();

  static {
    NUM_INFRA_FEATURE_MAP.put("[/Num|Digit]", Arrays.asList(new ConllPair("SubPOS", "f")));
    NUM_INFRA_FEATURE_MAP.put("[_Ord/Adj][Nom]", Arrays.asList(new ConllPair("SubPOS", "o")));
  }

  /**
   * INJ
   */
  private static final Map<String, List<ConllPair>> INJ_INFRA_FEATURE_MAP = new TreeMap<>();

  static {
    //
  }

  /**
   * POST
   */
  private static final Map<String, List<ConllPair>> POST_INFRA_FEATURE_MAP = new TreeMap<>();

  static {
    //
  }

  /**
   * ADV
   */
  private static final Map<String, List<ConllPair>> ADV_INFRA_FEATURE_MAP = new TreeMap<>();

  static {
    ADV_INFRA_FEATURE_MAP.put("[/Adv|Pro]", Arrays.asList(new ConllPair("SubPOS", "d")));
  }

  /**
   * DET
   */
  private static final Map<String, List<ConllPair>> DET_INFRA_FEATURE_MAP = new TreeMap<>();

  static {
    //
  }


  /**
   * CoNLL feature key-value pair.
   */
  public static class ConllPair {

    public final String feat;
    public final String value;

    public ConllPair(String feat, String value) {
      this.feat = feat;
      this.value = value;
    }
  }

  /**
   * Map to CoNLL feature String.
   *
   * @param feats
   * @return
   */
  private static String asConllString(Map<String, String> feats) {
    StringBuffer stringBuffer = new StringBuffer();

    for (Map.Entry<String, String> entry : feats.entrySet()) {
      stringBuffer.append(entry.getKey());
      stringBuffer.append("=");
      stringBuffer.append(entry.getValue());
      stringBuffer.append("|");
    }

    String s = stringBuffer.toString().trim();

    return s.substring(0, s.length() - 1);
  }

  /**
   * Extracts the CoNLL features.
   *
   * @param infra
   * @param defMap
   * @param featMap
   * @return
   */
  public static String getFeatures(String infra, Map<String, String> defMap, Map<String, List<ConllPair>> featMap) {
    Map<String, String> features = new LinkedHashMap<>(defMap);

    for (String infraFeat : featMap.keySet()) {
      if (infra.contains(infraFeat)) {
        for (ConllPair conllPair : featMap.get(infraFeat)) {
          features.put(conllPair.feat, conllPair.value);
        }
      }
    }

    return asConllString(features);
  }

  /**
   * Extracts the CoNLL features.
   *
   * @param infra
   * @return
   */
  public static String getFeatures(String infra, String form) {

    if (!infra.contains("[")) {
      return "_";
    }

    String pos = getPos(infra, form);

    switch (pos) {
      case "N":
        return getFeatures(infra, NOUN_DEF_MAP, NOUN_INFRA_FEATURE_MAP);
      case "V":
        return getFeatures(infra, VERB_DEF_MAP, VERB_INFRA_FEATURE_MAP);
      case "Det":
        return getFeatures(infra, DET_DEF_MAP, DET_INFRA_FEATURE_MAP);
      case "Cnj":
        return getFeatures(infra, CONJ_DEF_MAP, CONJ_INFRA_FEATURE_MAP);
      case "Adv":
        return getFeatures(infra, ADV_DEF_MAP, ADV_INFRA_FEATURE_MAP);
      case "Adj":
        return getFeatures(infra, ADJ_DEF_MAP, ADJ_INFRA_FEATURE_MAP);
      case "Num":
        return getFeatures(infra, NUM_DEF_MAP, NUM_INFRA_FEATURE_MAP);
      case "Post":
        return getFeatures(infra, POST_DEF_MAP, POST_INFRA_FEATURE_MAP);
      case "Inj-Utt":
        return getFeatures(infra, INJ_DEF_MAP, INJ_INFRA_FEATURE_MAP);
    }

    return "_"; // XXX null volt, de az mire volt jó?
  }

  /**
   * Get the POS.
   *
   * @param infraAna
   * @return
   */
  public static String getPos(String infraAna, String form) {

    // ha a token maga a '['
    if (form.equals("[")) {
      return "["; // ti. jelenleg punct => pos == form XXX -- vö: [GSZ]
    }

    // ha nincs elemzés (gondolom ezt nézzük itt) = nincs benne '['
    // de ez fut le 'OTHER' esetén is XXX
    if (!infraAna.contains("[")) {
      return form;
    }

    String corrected = infraAna;
    if (!infraAna.contains("[/") && !infraAna.equals("OTHER")) {
      corrected = corrected.substring(corrected.indexOf("["));
      corrected = posCorrector(corrected);
    }

    String pos = corrected.substring(corrected.indexOf("[/") + 2);

    pos = pos.substring(0, pos.indexOf("]"));

    if (pos.contains("|")) {
      pos = pos.substring(0, pos.indexOf("|"));
    }

    if (pos.contains("Supl")) {
      infraAna = infraAna.replace("[/Supl]", "");
      getPos(infraAna, form);
      // return getPos(infraAna, form);
      // kell a return, kul ez elvesz! :) -- ld. [GSP]
    }

    return pos;
  }

  /**
   * POS correction.
   *
   * @param pos
   * @return
   */
  private static String posCorrector(String pos) {

    if (pos.startsWith("[Adj]")) {
      return pos.replace("[Adj]", "[/Adj]");
    }

    if (pos.startsWith("[N]")) {
      return pos.replace("[N]", "[/N]");
    }

    return pos;
  }

  public static String getLemma(String infra, String form) {

    // ha a token maga a '['
    if (form.equals("[")) {
      return "["; // punct => lemma == form
    }

    // ha nincs elemzés (gondolom ezt nézzük itt) = nincs benne '['
    // de ez fut le 'OTHER' esetén is XXX
    String infraLemma = infra.contains("[") ? infra.substring(0, infra.indexOf("[")) : form;
    return infraLemma;
  }

  public static String getLemmaPosFeatures(String infra, String form) {
    return getLemma(infra, form) + "\t" +
           getPos(infra, form) + "\t" +
           getFeatures(infra, form);
  }

  public static void main(String[] args) {

    String[][] data = {
      // hétfő	N	SubPOS=c|Num=s|Cas=p|NumP=none|PerP=none|NumPd=none
      {"hétfő[/N][Supe]", "Hétfőn"},

      // folytatódik	V	SubPOS=m|Mood=i|Tense=p|Per=3|Num=s|Def=n
      {"folytatódik[/V][Prs.NDef.3Sg]", "folytatódik"},

      // az	Det	SubPOS=f
      {"az[/Det|art.Def]", "az"},

      // . . _
      {"OTHER", "."},

      // hongkongi A SubPOS=f|Deg=p|Num=s|Cas=n|NumP=none|PerP=none|NumPd=none
      {"hongkongi[Adj][Nom]", "hongkongi"},

      // gyerek  N SubPOS=c|Num=p|Cas=n|NumP=s|PerP=3|NumPd=none
      // XXX jó ez?
      {"gyerek[/N][Pl.Poss.3Sg][Nom]", "gyerekei"},

      // felébred  V SubPOS=m|Mood=i|Tense=s|Per=3|Num=p|Def=n
      {"felébred[/V][Prs.NDef.2Pl]", "felébredtek"},

      // gyerek  N SubPOS=c|Num=s|Cas=n|NumP=none|PerP=none|NumPd=none
      // XXX [Pl.Poss.2Sg] nincs kezelve -- miért???
      {"gyerek[/N][Pl.Poss.2Sg][Nom]", "Gyerekeid"},

      // leg[/Supl]=leg+jó[/Adj]=jo+bb[_Comp/Adj]=bb+[Nom]
      {"legjobb[/Supl][/Adj][_Comp/Adj][Nom]", "legjobb"},

      // a nyitó '[' gondot okoz
      // XXX valóban '[OTHER' most a lemma+tag,
      //     de ez nem tuti, hogy jó így véglegesnek
      {"[OTHER", "["}

    };

    System.out.println();
    for ( String[] x : data ) {
      System.out.println( x[0] + "\t" + getLemmaPosFeatures( x[0], x[1] ) );
      System.out.println();
    }
    
  }
}
