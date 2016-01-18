package hu.nytud.gate.apps;

import gate.creole.PackagedController;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.AutoInstanceParam;

/**
 * 
 * @author Márton Miháltz
 *
 */
@CreoleResource(name = "Magyarlanc Morphparse",
	comment = "Hungarian sentencer & tokenizer & POS-tagger & lemmatizer",
	autoinstances = @AutoInstance(parameters = {
			@AutoInstanceParam(name="pipelineURL",
							   value="magyarlanc_morph.gapp"), 
			@AutoInstanceParam(name="menu", value="Hungarian")}))
public class MagyarlancMorphparseApp extends PackagedController {

	private static final long serialVersionUID = 1L;

}
