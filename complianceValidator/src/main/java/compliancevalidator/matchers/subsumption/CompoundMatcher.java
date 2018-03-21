package compliancevalidator.matchers.subsumption;


import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import compliancevalidator.wordnet.RiWordNetOperations;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;




public class CompoundMatcher extends ObjectAlignment implements AlignmentProcess {
	
	
	final double THRESHOLD = 0.9;
	
	public CompoundMatcher() {
	}
	
	

	public void align( Alignment alignment, Properties param ) throws AlignmentException {
		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){
					// add mapping into alignment object 
					addAlignCell(cl1,cl2, findCompoundRelation(cl1,cl2), compoundMatch(cl1,cl2));  
					}
				}
			
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public double compoundMatch(Object o1, Object o2) throws AlignmentException {
		try {
			String s1 = ontology1().getEntityName(o1);
			String s2 = ontology2().getEntityName(o2);
			
			//System.out.println("Matching " + s1 + " and " + s2);
			
			if (s1 == null || s2 == null) return 0.;
			
			if (isCompoundSimple(s1,s2) && !s1.equals(s2)) { 
				return 1.0;
			} else if (isCompoundSimple(s2,s1) && !s2.equals(s1)) { 
				return 1.0;
			}
			else { 
				return 0.;
			}
		} catch ( OntowrapException owex ) {
			throw new AlignmentException( "Error getting entity name", owex );
		}
	}

	
	public double compoundMatchWithSynonyms(Object o1, Object o2) throws AlignmentException {
		try {
			String s1 = ontology1().getEntityName(o1);
			String s2 = ontology2().getEntityName(o2);
			if (s1 == null || s2 == null) return 0.;
			
			if (isCompound(s1,s2) && !s1.equals(s2)) { 
				return 1.0;
			} else if (isCompound(s2,s1) && !s2.equals(s1)) { 
				return 1.0;
			}
			else { 
				return 0.;
			}
		} catch ( OntowrapException owex ) {
			throw new AlignmentException( "Error getting entity name", owex );
		}
	}
	
	
	public String findCompoundRelation(Object o1, Object o2) throws AlignmentException {
		try {
			String s1 = ontology1().getEntityName(o1);
			String s2 = ontology2().getEntityName(o2);
			if (s1 == null || s2 == null) return "0.";
			
			String isA = "&lt;";
			String hasA = "&gt;";
			
			if (isCompoundSimple(s1,s2) && !s1.equals(s2)) { 
				return isA;
			} else if (isCompoundSimple(s2,s1) && !s2.equals(s1)) { 
				return hasA;
			}
			else { 
				return "0";
			}
		} catch ( OntowrapException owex ) {
			throw new AlignmentException( "Error getting entity name", owex );
		}
	}
	
	public static boolean isCompound(String a, String b) {
		boolean test = false;

		String[] compounds = a.split("(?<=.)(?=\\p{Lu})");


		for (int i = 0; i < compounds.length; i++) {
			if (b.equals(compounds[i])) {
				test = true;
			}
		}

		return test;
	}
	
	//b = AerodromeRecentWeather
	//a = Aerodrome
	//If the compound head is not the last part of a, then we count the number of words in the compound, and the fewer the higher the score.
	//The rationale is that CloudLayer can be a restriction of Cloud, and the fewer the words, the closer the semantic relation with the compound part.
	public static boolean isCompoundSimple(String a, String b) {
		
		String[] arrayA = a.split("(?<=.)(?=\\p{Lu})");
		String[] arrayB = b.split("(?<=.)(?=\\p{Lu})");
		
		int numWordsA = arrayA.length;
		int numWordsB = arrayB.length;
		
			boolean test = false;

			if (b.contains(a) && lastToken(b).equals(lastToken(a)) || a.contains(b) && lastToken(a).equals(lastToken(b))) {
				test = true;
			} else if (b.contains(a) && numWordsB <=2 || a.contains(b) && numWordsA <= 2)
				test = true;
			else {
				test = false;
			}

		return test;
	}

	public static boolean isCompoundSyn(String a, String b) {
		boolean test = false;

		String[] temp_compounds = a.split("(?<=.)(?=\\p{Lu})");
		
		ArrayList<String> compound = new ArrayList<String>();
		
		for (int i = 0; i < temp_compounds.length; i++) {
			compound.add(temp_compounds[i].toLowerCase());
		}
		
		ArrayList<String> synonyms = getSynonyms(b);
		synonyms.add(b.toLowerCase());
		
		for (String s : synonyms) {
			for (String t : compound) {
				if (s.toLowerCase().equals(t.toLowerCase())) {
					test = true;
				}
			}
		}


		return test;
	}

	
	public static ArrayList<String> getSynonyms(String a) {
		
		
		ArrayList<String> synonyms = new ArrayList<String>();
		
		String[] synsets = RiWordNetOperations.getSynonyms(a.toLowerCase());
		
		for (int i = 0; i < synsets.length; i++) {
			synonyms.add(synsets[i].toLowerCase());
		}
		
		return synonyms;
	}
	
	public static String lastToken(String inputString) {
		
		String[] compounds = inputString.split("(?<=.)(?=\\p{Lu})");
		
		int last = compounds.length;
		
		return compounds[last-1];
	}
	
	
	
	
	public static void main(String[] args) {
//		String[] onto1 = {"Short Paper", "LongPaper", "Academic_Paper", "Short_Article", "ElectricCar", "Truck"};
//		String[] onto2 = {"Car", "Paper", "Article", "Automobile", "ElectricCar"};
//		
//		for (int i = 0; i < onto1.length; i++) {
//			for (int j = 0; j < onto2.length; j++) {
//				if (isCompound(onto1[i], onto2[j])) {
//					System.out.println(onto1[i] + " < " + onto2[j]);
//				} else if (isCompound(onto2[j], onto1[i])) {
//					System.out.println(onto2[j] + " < " + onto1[i]);
//				} else {
//					System.err.println(onto1[i] + " is not subsumed by " + onto2[j]);
//				}
//			}
//		}
		
		String s2 = "AerodromeRunwayVisualRange";
		String s1 = "RunwayVisualRange";
		
		System.out.println(isCompound(s2, s1));
		
		/*String word = "article";
		
		System.out.println("Synonyms for " + word);
		ArrayList<String> syns = getSynonyms(word);
		for (String s : syns) {
			System.out.println(s);
		}*/
		
		

	}
}
