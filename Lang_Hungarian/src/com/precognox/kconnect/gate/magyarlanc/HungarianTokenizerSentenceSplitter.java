package com.precognox.kconnect.gate.magyarlanc;

import gate.Factory;
import gate.FeatureMap;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleResource;
import gate.util.InvalidOffsetException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Map.Entry;
import splitter.MySplitter;
import splitter.archive.StringCleaner;
import splitter.ling.sentencesplitter.DefaultSentenceSplitter;
import splitter.ling.tokenizer.DefaultWordTokenizer;

@CreoleResource(name = "HU 1. \"emToken\" Sentence Splitter and Tokenizer (magyarlanc3.0) [Windows]",
                comment = "Tokenizer and Sentence Splitter for Hungarian language base on Magyarlanc",
                icon = "tokeniser")
public class HungarianTokenizerSentenceSplitter extends AbstractLanguageAnalyser {

    private static final long serialVersionUID = 1L;

    private static final Pattern wsP = Pattern.compile("\\p{javaWhitespace}");
    private static final Pattern punctP = Pattern.compile("\\p{Punct}+");

    private DefaultSentenceSplitter splitter;
    private DefaultWordTokenizer tokenizer;
    private StringCleaner stringCleaner;

    @Override
    public Resource init() throws ResourceInstantiationException {
        try {
            splitter = new DefaultSentenceSplitter();
            tokenizer = new DefaultWordTokenizer();
            stringCleaner = new StringCleaner();
            return super.init();
        } catch (Exception ex) {
            throw new ResourceInstantiationException(ex);
        }
    }

    @Override
    public void execute() throws ExecutionException {
        try {
            MySplitter mySplitter = MySplitter.getInstance();
            String text = stringCleaner.cleanString(document.getContent().toString().trim());

            long previousTokenEnd = 0;
            int[] sentenceOffsets = splitter.findSentenceOffsets(text, mySplitter.split(text));
            for (Entry<Integer, Integer> sentenceOffset : trimOffsets(text, sentenceOffsets)) {
                int ss = sentenceOffset.getKey().intValue();
                int se = sentenceOffset.getValue().intValue();
                String sentence = text.substring(ss, se);
                addSentenceAnnotation(ss, se, sentence);
                //getDocument().getAnnotations().add(ss, se, SENTENCE_ANNOTATION_TYPE, Factory.newFeatureMap());

                Iterator<Entry<Integer, Integer>> tokenIter = trimOffsets(sentence, tokenizer.findWordOffsets(sentence, mySplitter.tokenize(sentence))).iterator();
                while (tokenIter.hasNext()) {
                    Entry<Integer, Integer> token = tokenIter.next();
                    long tokenStart = token.getKey().longValue() + ss;
                    long tokenEnd = token.getValue().longValue() + ss;

                    addTokenAnnotation(tokenStart, tokenEnd, TOKEN_ANNOTATION_TYPE);
                    if (previousTokenEnd != tokenStart) {
                        addTokenAnnotation(previousTokenEnd, tokenStart, SPACE_TOKEN_ANNOTATION_TYPE);
                    }
                    previousTokenEnd = tokenEnd;
                }
            }
        } catch (Exception ex) {
            throw new ExecutionException(ex);
        }
    }

    private void addSentenceAnnotation(long start, long end, String sentence) throws InvalidOffsetException {
        FeatureMap features = Factory.newFeatureMap();
        features.put(TOKEN_LENGTH_FEATURE_NAME, end - start);
        features.put(TOKEN_STRING_FEATURE_NAME, sentence);
        getDocument().getAnnotations().add(start, end, SENTENCE_ANNOTATION_TYPE, features);
    }

    private void addTokenAnnotation(long start, long end, String annotationType) throws InvalidOffsetException {
        FeatureMap tokenFeatures = Factory.newFeatureMap();
        tokenFeatures.put(TOKEN_LENGTH_FEATURE_NAME, end - start);

        String cleanedCoveredText = stringCleaner.cleanString(
                getDocument().getContent().getContent(start, end).toString());
        if (annotationType == TOKEN_ANNOTATION_TYPE) {
            /*
             * Delete whitespaces from word characters; probably I should get
             * rid of this, see #9.
             */
            cleanedCoveredText = wsP.matcher(cleanedCoveredText).replaceAll("");
            if (punctP.matcher(cleanedCoveredText).matches()) {
                tokenFeatures.put(TOKEN_KIND_FEATURE_NAME, "punctuation");
            } else {
                tokenFeatures.put(TOKEN_KIND_FEATURE_NAME, "word");
            }
        }
        tokenFeatures.put(TOKEN_STRING_FEATURE_NAME, cleanedCoveredText);

        getDocument().getAnnotations().add(start, end, annotationType, tokenFeatures);
    }

    private List<Entry<Integer, Integer>> trimOffsets(String text, int[] offsets) {
        List<Entry<Integer, Integer>> offsetList = new ArrayList<Entry<Integer, Integer>>(offsets.length);
        for (int i = 1; i < offsets.length; ++i) {
            int start = offsets[i - 1];
            int end = offsets[i];
            while (start < end && text.charAt(start) <= ' ') {
                start++;
            }
            while (start < end && text.charAt(end - 1) <= ' ') {
                end--;
            }
            offsetList.add(new SimpleEntry<Integer, Integer>(start, end));
        }
        return offsetList;
    }

}
