package compliancevalidator.matchers.equivalence;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import compliancevalidator.misc.ISub;
import compliancevalidator.misc.Jaccard;
import compliancevalidator.misc.StringUtilities;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

public class DefinitionsMatcher extends ObjectAlignment implements AlignmentProcess {

	public DefinitionsMatcher() {
	}

	public void align(Alignment alignment, Properties param) throws AlignmentException {

		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){

					// add mapping into alignment object 
					addAlignCell(cl1,cl2, "=", getDefinitionsSim(cl1,cl2));  
				}

			}

		} catch (Exception e) { e.printStackTrace(); }
	}

	public double getDefinitionsSim(Object o1, Object o2) throws OntowrapException, OWLOntologyCreationException, IOException {

		double measure = 0;
		double sim = 0;
		//get definitions (comments) associated with the entities to be matched
		Set<String> o1Defs = ontology1().getEntityComments(o1);
		Set<String> o2Defs = ontology2().getEntityComments(o2);

		//probably only one set item with definition, but just in case we merge all possible set items into a single string
		String def1 = StringUtils.join(o1Defs, " ");
		String def2 = StringUtils.join(o2Defs, " ");

		//if there is a definition it´s probably longer than 4 chars...
		if (def1.length() > 4 && def2.length() > 4) {
			sim = computeSimpleSim(def1, def2);
		}

		//need to keep our sim within [0..1] 
		if (sim > 0 && sim <= 1) {
			measure = sim;
		} else {
			measure = 0;
		}


		return measure;

	}

	
	/**
	 * A very simple string matcher based on the ISub algorithm
	 * @param s1
	 * @param s2
	 * @return
	 */
	private static double computeSimpleSim(String s1, String s2) {

		double sim = 0; 

		ISub isub = new ISub();

		sim = isub.score(s1, s2);

		return sim;
	}

	private static double computeJaccardSim(String s1, String s2) throws IOException {

		double sim = 0;

		//remove stopwords
		String s1WOStop = StringUtilities.removeStopWordsFromString(s1);
		String s2WOStop = StringUtilities.removeStopWordsFromString(s2);

		String[] s1Array = s1WOStop.split(" ");
		String[] s2Array = s2WOStop.split(" ");		

		Set<String> s1Set = new HashSet<String>(Arrays.asList(s1Array));
		Set<String> s2Set = new HashSet<String>(Arrays.asList(s2Array));

		sim = Jaccard.jaccardSetSim(s1Set, s2Set);

		return sim;

	}

}
