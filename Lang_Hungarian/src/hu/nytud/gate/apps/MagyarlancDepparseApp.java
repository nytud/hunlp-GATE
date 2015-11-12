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
@CreoleResource(name = "Magyarlánc Depparse",
	comment = "Hungarian sentencer & tokenizer & POS-tagger & lemmatizer & dependency parser",
	autoinstances = @AutoInstance(parameters = {
			@AutoInstanceParam(name="pipelineURL",
							   value="magyarlanc_dep.gapp"), 
			@AutoInstanceParam(name="menu", value="Hungarian")}))
public class MagyarlancDepparseApp extends PackagedController {

	private static final long serialVersionUID = 1L;

}
