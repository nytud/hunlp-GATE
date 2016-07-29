package com.precognox.kconnect.gate.magyarlanc;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import gate.Utils;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;
import hu.u_szeged2.pos.purepos.MyPurePos;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import splitter.archive.StringCleaner;

@CreoleResource(name = "[DEMO] 3. POS Tagger and Lemmatizer [HU] [magyarlanc 3.0]",
                comment = "Adds MSD code and lemma annotations",
                icon = "pos-tagger")
public class HungarianLemmatizerPosTagger extends AbstractLanguageAnalyser {

	private static final long serialVersionUID = 1L;

    private static final StringCleaner STRING_CLEANER = new StringCleaner();

    private static final BiFunction<Document, Collection<Annotation>, List<String>> ANNOTS_TO_STRINGS
            = (doc, as) -> as.stream()
            .map(a -> a.getFeatures().getOrDefault(TOKEN_STRING_FEATURE_NAME, STRING_CLEANER.cleanString(Utils.stringFor(doc, a).trim())).toString())
            .collect(Collectors.toList());

    @Override
    public void execute() throws ExecutionException {
        try {
            Document doc = getDocument();
            AnnotationSet annotations = doc.getAnnotations();
            for (Annotation sentenceAnnotation : annotations.get(SENTENCE_ANNOTATION_TYPE)) {
                List<Annotation> tokens = Utils.getOverlappingAnnotations(annotations, sentenceAnnotation, TOKEN_ANNOTATION_TYPE).inDocumentOrder();

                String[][] morphParsedSentence = MyPurePos.getInstance().morphParseSentence(ANNOTS_TO_STRINGS.apply(doc, tokens));
                for (int i = 0; i < tokens.size(); i++) {
                    String[] morph = morphParsedSentence[i];
                    FeatureMap featureMap = tokens.get(i).getFeatures();
                    featureMap.put(outputLemmaFeature, morph[1]);
                    featureMap.put(outputPOSFeature, morph[2]);
                    featureMap.put(outputMorphFeature, morph[3]);
                }
            }
        } catch (Exception ex) {
            throw new ExecutionException(ex);
        }
    }

	@RunTime
	@CreoleParameter(
			comment="The name of the lemma feature on output 'Token' annotations",
			defaultValue="lemma")
	public void setOutputLemmaFeature(String f) {
		this.outputLemmaFeature = f;
	}
	public String getOutputLemmaFeature() {
		return this.outputLemmaFeature;
	}
	private String outputLemmaFeature;

	@RunTime
	@CreoleParameter(
			comment="The name of the pos feature on output 'Token' annotations",
			defaultValue="pos")
	public void setOutputPOSFeature(String f) {
		this.outputPOSFeature = f;
	}
	public String getOutputPOSFeature() {
		return this.outputPOSFeature;
	}
	private String outputPOSFeature;
		  	
	@RunTime
	@CreoleParameter(
			comment="The name of the morph feature on output 'Token' annotations",
			defaultValue="feature")
	public void setOutputMorphFeature(String f) {
		this.outputMorphFeature = f;
	}
	public String getOutputMorphFeature() {
		return this.outputMorphFeature;
	}
	private String outputMorphFeature;
}
