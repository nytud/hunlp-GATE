package hu.nytud.gate.postaggers;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import gate.Resource;
import gate.Utils;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;
import hu.ppke.itk.nlpg.docmodel.ISentence;
import hu.ppke.itk.nlpg.purepos.ITagger;
import hu.ppke.itk.nlpg.purepos.MorphTagger;
import hu.ppke.itk.nlpg.purepos.cli.configuration.Configuration;
import hu.ppke.itk.nlpg.purepos.model.internal.CompiledModel;
import hu.ppke.itk.nlpg.purepos.model.internal.RawModel;
import hu.ppke.itk.nlpg.purepos.morphology.NullAnalyzer;
import hu.u_szeged2.config.Config;
import hu.u_szeged2.pos.purepos.MySerilalizer;

import hu.nytud.gate.util.DepTool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  Magyarlánc Hungarian POS-tagger and lemmatizer processing resource,
 *  Based on precognox's HungarianLemmatizerPosTagger + MyPurePos class
 *  @author Peter K
 */ 
@CreoleResource(name = "HU 3. \"emTag\" POS Tagger and Lemmatizer (PurePOS in magyarlanc3.0, hfst)",
                comment = "Adds feature and lemma annotations",
                icon = "pos-tagger")
public class Magyarlanc3POSTaggerLemmatizer extends AbstractLanguageAnalyser {

	private static final long serialVersionUID = 1L;

	private static final double BEAM_LOG_THETA = Math.log(1000);
	private double SUFFIX_LOG_THETA = Math.log(10);
	private int MAX_GUESSED = 5;
	private boolean USE_BEAM_SEARCH = false;

	private static volatile ITagger iTagger = null;

	public Resource init() throws ResourceInstantiationException {
		if (iTagger == null) {
			
			RawModel rawmodel = null;
		    try {
		        rawmodel = MySerilalizer.readModel(Config.getInstance().getPurePosModel());
		    } catch (Exception e) {
		        e.printStackTrace();
		    }

		    CompiledModel<String, Integer> model = rawmodel.compile(new Configuration());

			iTagger = new MorphTagger(model, new NullAnalyzer(), BEAM_LOG_THETA, SUFFIX_LOG_THETA, MAX_GUESSED, USE_BEAM_SEARCH);
    	}
		
		return this;
	}

	@Override
    public void execute() throws ExecutionException {
        try {
            Document doc = getDocument();
            AnnotationSet annotations = doc.getAnnotations();
            for (Annotation sentenceAnnotation : annotations.get(SENTENCE_ANNOTATION_TYPE)) {
                List<Annotation> tokens = Utils.getOverlappingAnnotations(annotations, sentenceAnnotation, TOKEN_ANNOTATION_TYPE).inDocumentOrder();

                //annotált{{annotál[\V]||annotáció[\N]}}
                List<String> input = new ArrayList<>();
                for (Annotation token : tokens) {
                	Object anas_obj = token.getFeatures().get(inputAnasFeatureName);
            		String text = document.getContent().getContent(
                			token.getStartNode().getOffset(), token.getEndNode().getOffset()
        				).toString();
                	if (!(anas_obj instanceof List<?>)) {
                		input.add(text);
                		System.err.println("Invalid format for "+inputAnasFeatureName+" in "+text);
                	} else {
                	    @SuppressWarnings("unchecked")
                		List<Map<String,String>> anas = (List<Map<String,String>>)anas_obj;
                	    List<String> opts = new ArrayList<>();
                		for(Map<String,String> anas1 : anas) {
                			if (!anas1.containsKey("lemma") || !anas1.containsKey("feats")) {
                				System.err.println("Invalid entry for "+inputAnasFeatureName+" in "+text);
                			} else {
                    			opts.add(anas1.get("lemma")+anas1.get("feats"));                				
                			}
                		}
                		if (!opts.isEmpty()) {
                			input.add(text+"{{" + String.join("||", opts)+ "}}");
                		} else {
                			input.add(text);
                		}
                	}
                }

                ISentence tagged = iTagger.tagSentence(input);
                
                for (int i = 0; i < tagged.size(); ++i) {
                    FeatureMap featureMap = tokens.get(i).getFeatures();

                    String stem = tagged.get(i).getStem(); // lemma
                    String tag = tagged.get(i).getTag(); // hfstcode analysis tag

                    // felébred[/V][Prs.NDef.2Pl]
                    String hfstlemmaana = stem + tag; // XXX egyben nem hozzáférhető vhogy? :)

                    // felébredtek
                    Object oform = featureMap.get("string");
                    String form = oform == null ? "" : oform.toString();

                    featureMap.put(outputLemmaAnnotName, stem);
                    featureMap.put("hfstana", tag); // -> for huntag3-based tools!
                    //featureMap.put("hfstlemmaana", hfstlemmaana);
                    String pos = DepTool.getPos( hfstlemmaana, form ); 
                    String features = DepTool.getFeatures( hfstlemmaana, form ); 
                    featureMap.put(outputPosAnnotName, pos ); 
                    featureMap.put(outputFeatureAnnotName, features ); 
                }                
            }
        } catch (Exception ex) {
            throw new ExecutionException(ex);
        }
    }

	@RunTime
	@CreoleParameter(
			comment="The name of the lemma annot on output 'Token' annotations",
			defaultValue="lemma")
	public void setOutputLemmaAnnotName(String f) {
		this.outputLemmaAnnotName = f;
	}
	public String getOutputLemmaAnnotName() {
		return this.outputLemmaAnnotName;
	}
	private String outputLemmaAnnotName;

	@RunTime
	@CreoleParameter(
			comment="The name of the POS annot on output 'Token' annotations",
			defaultValue="pos")
	public void setOutputPosAnnotName(String f) {
		this.outputPosAnnotName = f;
	}
	public String getOutputPosAnnotName() {
		return this.outputPosAnnotName;
	}
	private String outputPosAnnotName;

	@RunTime
	@CreoleParameter(
			comment="The name of the morphfeature annot on output 'Token' annotations",
			defaultValue="feature")
	public void setOutputFeatureAnnotName(String f) {
		this.outputFeatureAnnotName = f;
	}
	public String getOutputFeatureAnnotName() {
		return this.outputFeatureAnnotName;
	}
	private String outputFeatureAnnotName;

	@RunTime
	@CreoleParameter(
			comment = "The name of the feature that holds the morphological analysis data on output annotation types (tokens)",
			defaultValue = "anas")  
	public void setOutputAnasFeatureName(String newOutputAnasFeatureName) {
		inputAnasFeatureName = newOutputAnasFeatureName;
	}
	public String getOutputAnasFeatureName() {
	    return inputAnasFeatureName;
	}
	protected String inputAnasFeatureName;
  // XXX ez zavaros: most input vagy output??? (input, nem?)

}
