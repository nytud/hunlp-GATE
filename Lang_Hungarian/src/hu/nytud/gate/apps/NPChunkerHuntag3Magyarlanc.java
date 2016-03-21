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
@CreoleResource(name = "NP chunking with Huntag3 and Magyarlanc",
	comment = "Hungarian NP chunker using Huntag3 with sentencer & tokenizer & POS-tagger & lemmatizer from Magyarlanc",
	autoinstances = @AutoInstance(parameters = {
			@AutoInstanceParam(name="pipelineURL",
							   value="NP-chunk_huntag3_magyarlanc.gapp"), 
			@AutoInstanceParam(name="menu", value="Hungarian")}))
public class NPChunkerHuntag3Magyarlanc extends PackagedController {

	private static final long serialVersionUID = 1L;

}
