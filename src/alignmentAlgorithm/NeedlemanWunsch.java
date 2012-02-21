package alignmentAlgorithm;

import hierarchicStructure.HierarchicStructure;
import hierarchicStructure.Transformer;
import java.util.ArrayList;
import ontology.Ontology;
import ontology.Translater;
import sequenceElement.ActionElement;

public class NeedlemanWunsch {
	
	private double match = 1;
	private double mismatch = -1;
	private double gap = 0;
	
	//the sum of alpha, beta, gamma and delta must be 1
	private double alpha = 0.3;
	private double beta = 0.3;
	private double gamma = 0.1;
	private double delta = 0.3;
	
	private double exponent = 3;
	
	HierarchicStructure hierarchy = new HierarchicStructure("C:/Users/Administrator/Desktop/Data/HierarchicStructure.txt");
	Transformer transformer = new Transformer(hierarchy);
	
	private ArrayList<ActionElement> seq1;
	private ArrayList<ActionElement> seq2;
	
	private Ontology ontology;
	
	private int m;
	private int n;
	
	private double[][] matrix;
	private String[][] traceback;
	private ActionElement[][] alignments;
	
	private int pointer = 0;
	private int nothingCount = 0;
	
	public NeedlemanWunsch(ArrayList<ActionElement> seq1, ArrayList<ActionElement> seq2, int function, Ontology ontology){
		seq1 = transformer.transform(seq1);
		seq2 = transformer.transform(seq2);
		this.seq1 = seq1;
		this.seq2 = seq2;
		this.ontology = ontology;
		m = seq1.size();
		n = seq2.size();
		matrix = new double[m+1][n+1];
		traceback = new String[m+1][n+1];
		alignments = new ActionElement[2][m+n];
		//initialisation of the matrix + traceback
		matrix[0][0] = 0;
		traceback[0][0] = "done";
		for (int j = 1; j <= n; j++){
			matrix[0][j] = j * gap;
			traceback[0][j] = "left";
		}
		for (int i = 1; i <= m; i++){
			matrix[i][0] = i * gap;
			traceback[i][0] = "up";
		}
		//recursive calculation of the remaining alignment-scores + traceback
		for (int i = 1; i <= m; i++){
			for (int j = 1; j <= n; j++){
				double score1;
				double d;
				if (function == 1) {
					d = Compare1(seq1.get(i - 1), seq2.get(j - 1));
					score1 = matrix[i - 1][j - 1] + d;
				} 
				else {
					d = Compare2(seq1.get(i - 1), seq2.get(j - 1));
					score1 = matrix[i - 1][j - 1] + d;
				}
				
				
				if (d == match) {
					String s = seq1.get(i - 1).getHashMap().get("verb");
					if (s != null) {
						if (s.equals("none")) {
							score1 -= 0.5;
						}
					}
				}
				
				
				double score2 = matrix[i-1][j] + gap;
				double score3 = matrix[i][j-1] + gap;
				
				//System.out.println(seq1.get(i - 1).getName() + " - " + seq2.get(j - 1).getName());
				//System.out.println(d);
				
				if (score1 > score2 && score1 > score3){
					matrix[i][j] = score1;
					traceback[i][j] = "diag";
				} else if (score2 > score3){
						matrix[i][j] = score2;
						traceback[i][j] = "up";
				} else {
						matrix[i][j] = score3;
						traceback[i][j] = "left";
				}
			}
		}
		
		this.countNothing(m, n);
		
	}
	
	private double Compare1(ActionElement a1, ActionElement a2){
		if (a1.getName().equals(a2.getName())) {
			return match;
		} else {
			return mismatch;
		}
	}
	
	private double Compare2(ActionElement a1, ActionElement a2){
		if (a1.getName().equals(a2.getName())) {
			return match;
		} else {
			double compare = 0;
			Translater translater = new Translater();
			String verb1 = a1.getHashMap().get("verb");
			String verb2 = a2.getHashMap().get("verb");
			String firstObject1 = a1.getHashMap().get("object1");
			String firstObject2 = a2.getHashMap().get("object1");
			String preposition1 = a1.getHashMap().get("preposition");
			String preposition2 = a2.getHashMap().get("preposition");
			String secondObject1 = a1.getHashMap().get("object2");
			String secondObject2 = a2.getHashMap().get("object2");
			
			double a = 0;
			if (!verb1.equals(verb2)) {
				if (verb1.isEmpty() || verb2.isEmpty()) {
					a = mismatch;
				} else {
					verb1 = translater.getTranslateMap().get(verb1);
					verb2 = translater.getTranslateMap().get(verb2);
					a = mismatch + 2.0 * match * Math.pow(ontology.getWupSimilarity(verb1, verb2), exponent);
				}
			} else if (verb1.equals(verb2) && !verb1.isEmpty() && !verb2.isEmpty()) {
				a = match;
			}
			//System.out.println(verb1 + " - " + verb2 + "; a = " + a);
			
			double b = 0;
			if (!firstObject1.equals(firstObject2)) {
				if (firstObject1.isEmpty() || firstObject2.isEmpty()) {
					b = mismatch;
				} else {
					firstObject1 = translater.getTranslateMap().get(firstObject1);
					firstObject2 = translater.getTranslateMap().get(firstObject2);
					b = mismatch + 2.0 * match * Math.pow(ontology.getWupSimilarity(firstObject1, firstObject2), exponent);
				}
			} else if (firstObject1.equals(firstObject2) && !firstObject1.isEmpty() && !firstObject2.isEmpty()) {
				b = match;
			}
			//System.out.println(firstObject1 + " - " + firstObject2 + "; b = " + b);
			
			double c = 0;
			if (!preposition1.equals(preposition2)) {
				if (preposition1.isEmpty() || preposition2.isEmpty()) {
					c = mismatch;
				} else {
					preposition1 = translater.getTranslateMap().get(preposition1);
					preposition2 = translater.getTranslateMap().get(preposition2);
					c = mismatch + 2.0 * match * Math.pow(ontology.getWupSimilarity(preposition1, preposition2), exponent);
				}
			} else if (preposition1.equals(preposition2) && !preposition1.isEmpty() && !preposition2.isEmpty()) {
				c = match;
			}
			//System.out.println(preposition1 + " - " + preposition2 + "; c = " + c);
			
			double d = 0;
			if (!secondObject1.equals(secondObject2)) {
				if (secondObject1.isEmpty() || secondObject2.isEmpty()) {
					d = mismatch;
				} else {
					secondObject1 = translater.getTranslateMap().get(secondObject1);
					secondObject2 = translater.getTranslateMap().get(secondObject2);
					d = mismatch + 2.0 * match * Math.pow(ontology.getWupSimilarity(secondObject1, secondObject2), exponent);
				}
			} else if (secondObject1.equals(secondObject2) && !secondObject1.isEmpty() && !secondObject2.isEmpty()) {
				d = match;
			}
			//System.out.println(secondObject1 + " - " + secondObject2 + "; d = " + d);
			
			// a, b, c and d are element of [mismatch;match]
			compare = alpha * a + beta * b + gamma * c + delta * d;
			//System.out.println("--> " + alpha + "*" + a + " + " + beta + "*" + b + " + " + gamma + "*" + c + " + " + delta + "*" + d + " = " + compare );
			//System.out.println();
			return compare;
		}
	}
	
	public void printMatrix(){
		for (int i = 0; i <= m; i++){
			for (int j = 0; j <= n; j++){
				double x = Math.round(matrix[i][j] * 100.0) / 100.0;
				String s = String.valueOf(x);
				//maximum of 8 characters
				for (int k = 8 - s.length(); k > 0; k--){
					System.out.print(" ");
				}
				System.out.print(s);
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public void printTraceback(){
		for (int i = 0; i <= m; i++){
			for (int j = 0; j <= n; j++){
				String s = traceback[i][j];
				//maximum of 8 characters
				for (int k = 8 - s.length(); k > 0; k--){
					System.out.print(" ");
				}
				System.out.print(s);
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public void printAlignment(){
		if (pointer == 0) {
			calculateAlignmentRecursive(m, n);
			alignments = transformer.retransform(alignments);
		}
		for (int i = pointer - 1; i >= 0; i--){
			String s1 = alignments[0][i].getName();
			String s2 = alignments[1][i].getName();
			//maximum of 50 characters
			for (int k = 50 - s1.length(); k > 0; k--){
				System.out.print(" ");
			}
			//System.out.println(s1 + " & - & " + s2);
			System.out.println(s1 + " & - & " + s2 + "\\\\");
		}
		System.out.println();
		System.out.println("L�nge seq1 = " + m + "; L�nge seq2 = " + n);
		double score = this.getScore();
		System.out.println("Alignment-Score: " + score);
		System.out.println();
	}
	
	private void calculateAlignmentRecursive(int m, int n){
		String s = traceback[m][n];
		if (s.equals("diag")) {
			alignments[0][pointer] = seq1.get(m - 1);
			alignments[1][pointer] = seq2.get(n - 1);
			pointer++;
			calculateAlignmentRecursive(m - 1, n - 1);
		} else if (s.equals("left")) {
			alignments[0][pointer] = new ActionElement("$|$");
			alignments[1][pointer] = seq2.get(n - 1);
			pointer++;
			calculateAlignmentRecursive(m, n - 1);
		} else if (s.equals("up")) {
			alignments[0][pointer] = seq1.get(m - 1);
			alignments[1][pointer] = new ActionElement("$|$");
			pointer++;
			calculateAlignmentRecursive(m - 1, n);
		}
	}
	
	private void countNothing(int m, int n) {
		String s = traceback[m][n];
		if (s.equals("diag")){
			String s1 = seq1.get(m - 1).getHashMap().get("verb");
			String s2 = seq2.get(n - 1).getHashMap().get("verb");
			if (s1 != null && s2 != null) {
				if (s1.equals("none") && s2.equals("none")) {
					//System.out.println("++");
					nothingCount++;
				}
			}
			countNothing(m - 1, n - 1);
		} else if (s.equals("left")){
			countNothing(m, n - 1);
		} else if (s.equals("up")){
			countNothing(m - 1, n);
		}
	}
	
	public double getScore(){
		//System.out.println(matrix[m][n] + " + 0.5 * " + nothingCount + " / ((" + m + " + " + n + ") / 2)");
		double score = (matrix[m][n] + 0.5 * nothingCount) / ((m + n) / 2);
		//score = (score + 0.5) / 1.5;
		return Math.round(score * 100.0) / 100.0;
	}
	
}
