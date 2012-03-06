package alignmentAlgorithm;

import java.util.ArrayList;


import ontology.Ontology;
import ontology.Translater;

import sequenceElement.ActionElement;

public class SmithWaterman {

	double match = 1.0;
	double mismatch = -1.5;
	double gap = -0.75;
	
	//the sum of alpha, beta, gamma and delta must be 1
	private double alpha = 0.3;
	private double beta = 0.3;
	private double gamma = 0.1;
	private double delta = 0.3;
	
	private double exponent = 3;
	
	private ArrayList<ActionElement> seq1;
	private ArrayList<ActionElement> seq2;
	
	private int m;
	private int n;
	
	private double[][] matrix;
	private String[][] traceback;
	private String[][] alignments;
	
	private int pointer = 0;
	
	private double maxScore = 0;
	private ArrayList<Integer> maxScoreIndices = new ArrayList<Integer>();
	
	private Ontology ontology;
	
	public SmithWaterman(ArrayList<ActionElement> seq1, ArrayList<ActionElement> seq2, int function, Ontology ontology){
		this.seq1 = seq1;
		this.seq2 = seq2;
		this.ontology = ontology;
		m = seq1.size();
		n = seq2.size();
		matrix = new double[m+1][n+1];
		traceback = new String[m+1][n+1];
		alignments = new String[2][m*n];
		//initialisation of the matrix + traceback
		for (int i = 0; i <= m; i++){
			for (int j = 0; j <= n; j++){
				matrix[i][j] = 0;
				traceback[i][j] = "done";
			}
		}
		//recursive calculation of the remaining alignment-scores + traceback
		for (int i = 1; i <= m; i++){
			for (int j = 1; j <= n; j++){
				double score1;
				double d;
				if (function == 1) {
					d = Compare1(seq1.get(i - 1), seq2.get(j - 1));
					score1 = matrix[i - 1][j - 1] + d;
				} else {
					d = Compare2(seq1.get(i - 1), seq2.get(j - 1));
					score1 = matrix[i - 1][j - 1] + d;
				}
				
				if (d == match) {
					String s = seq1.get(i - 1).getHashMap().get("verb");
					if (s.equals("none")) {
						score1 -= 0.5;
					}
				}
				
				double score2 = matrix[i-1][j] + gap;
				double score3 = matrix[i][j-1] + gap;
				
				if (score1 > 0 || score2 > 0 || score3 > 0){
					if (score1 > score2 && score1 > score3){
						matrix[i][j] = score1;
						traceback[i][j] = "diag";
						if (score1 > maxScore){
							maxScore = score1;
						}
					} else if (score2 > score3){
						matrix[i][j] = score2;
						traceback[i][j] = "up";
						if (score2 > maxScore){
							maxScore = score2;
						}
					} else {
						matrix[i][j] = score3;
						traceback[i][j] = "left";
						if (score3 > maxScore){
							maxScore = score3;
						}
					}
				} else {
					matrix[i][j] = 0;
					traceback[i][j] = "done";
				}
				
				/*
				if (score1 > 0 || score2 > 0 || score3 > 0){
					if (score1 >= score2){
						if (score1 >= score3){
							matrix[i][j] = score1;
							traceback[i][j] = "diag";
							if (score1 > maxScore){
								maxScore = score1;
							}
						} else {
							matrix[i][j] = score3;
							traceback[i][j] = "left";
							if (score3 > maxScore){
								maxScore = score3;
							}
						}
					} else if (score2 >= score3){
						matrix[i][j] = score2;
						traceback[i][j] = "up";
						if (score2 > maxScore){
							maxScore = score2;
						}
					} else {
						matrix[i][j] = score3;
						traceback[i][j] = "left";
						if (score3 > maxScore){
							maxScore = score3;
						}
					}
				} else {
					matrix[i][j] = 0;
					traceback[i][j] = "done";
				}
				*/
			}
		}
		
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
			
			String translated1;
			String translated2;
			double a = 0;
			if (!verb1.equals(verb2)) {
				if (verb1.isEmpty() || verb2.isEmpty()) {
					a = 0;
				} else {
					translated1 = translater.getTranslateMap().get(verb1);
					translated2 = translater.getTranslateMap().get(verb2);
					a = Math.pow(ontology.getWupSimilarity(translated1, translated2), exponent);
				}
			} else if (verb1.equals(verb2) && !verb1.isEmpty() && !verb2.isEmpty()) {
				a = match;
			}
			//System.out.println(verb1 + " - " + verb2 + "; a = " + a);
			
			double b = 0;
			if (!firstObject1.equals(firstObject2)) {
				if (firstObject1.isEmpty() || firstObject2.isEmpty()) {
					b = 0;
				} else {
					translated1 = translater.getTranslateMap().get(firstObject1);
					translated2 = translater.getTranslateMap().get(firstObject2);
					b = Math.pow(ontology.getWupSimilarity(translated1, translated2), exponent);
				}
			} else if (firstObject1.equals(firstObject2) && !firstObject1.isEmpty() && !firstObject2.isEmpty()) {
				b = match;
			}
			//System.out.println(firstObject1 + " - " + firstObject2 + "; b = " + b);
			
			double c = 0;
			if (!preposition1.equals(preposition2)) {
				if (preposition1.isEmpty() || preposition2.isEmpty()) {
					c = 0;
				} else {
					translated1 = translater.getTranslateMap().get(preposition1);
					translated2 = translater.getTranslateMap().get(preposition2);
					c = Math.pow(ontology.getWupSimilarity(translated1, translated2), exponent);
				}
			} else if (preposition1.equals(preposition2) && !preposition1.isEmpty() && !preposition2.isEmpty()) {
				c = match;
			}
			//System.out.println(preposition1 + " - " + preposition2 + "; c = " + c);
			
			double d = 0;
			if (!secondObject1.equals(secondObject2)) {
				if (secondObject1.isEmpty() || secondObject2.isEmpty()) {
					d = 0;
				} else {
					translated1 = translater.getTranslateMap().get(secondObject1);
					translated2 = translater.getTranslateMap().get(secondObject2);
					d = Math.pow(ontology.getWupSimilarity(translated1, translated2), exponent);
				}
			} else if (secondObject1.equals(secondObject2) && !secondObject1.isEmpty() && !secondObject2.isEmpty()) {
				d = match;
			}
			//System.out.println(secondObject1 + " - " + secondObject2 + "; d = " + d);
			
			// a, b, c and d are element of [mismatch;match]
			compare = alpha * a + beta * b + gamma * c + delta * d;
			compare = -1.0 + 2.0 * compare;
			//System.out.println("--> " + alpha + "*" + a + " + " + beta + "*" + b + " + " + gamma + "*" + c + " + " + delta + "*" + d + " = " + compare );
			//System.out.println();
			if (compare > 0) {
				return compare;
			} else {
				return mismatch;
			}
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
				//maximum of 6 characters
				for (int k = 6 - s.length(); k > 0; k--){
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
			for (int i = 1; i <= m; i++) {
				for (int j = 1; j <= n; j++) {
					double score = matrix[i][j];
					if (score == maxScore) {
						maxScoreIndices.add(i);
						maxScoreIndices.add(j);
					}
				}
			}
			while (!maxScoreIndices.isEmpty()) {
				int m = maxScoreIndices.remove(0);
				int n = maxScoreIndices.remove(0);
				calculateAlignmentRecursive(m, n);
			}
		}
		System.out.println("globale Alignments:");
		for (int i = pointer - 1; i >= 0; i--){
			// if "ln" println for next local alignment
			if (!alignments[0][i].equals("ln")) {
				String s1 = alignments[1][i];
				String s2 = alignments[0][i];
				//maximum of 50 characters
				for (int k = 50 - s1.length(); k > 0; k--) {
					System.out.print(" ");
				}
				System.out.println(s1 + " & - & " + s2);
			} else {
				System.out.println();
			}
		}
		System.out.println();
	}
	
	private void calculateAlignmentRecursive(int m, int n){
		String s = traceback[m][n];
		if (s.equals("diag")){
			String name1 = seq1.get(m - 1).getName();
			//name1 = name1.replaceAll("-", " ");
			//name1 = name1.replaceAll("_", "-");
			String name2 = seq2.get(n - 1).getName();
			//name2 = name2.replaceAll("-", " ");
			//name2 = name2.replaceAll("_", "-");
			alignments[0][pointer] = name1;
			alignments[1][pointer] = name2;
			pointer++;
			calculateAlignmentRecursive(m - 1, n - 1);
		} else if (s.equals("left")){
			String name2 = seq2.get(n - 1).getName();
			//name2 = name2.replaceAll("-", " ");
			//name2 = name2.replaceAll("_", "-");
			//alignments[0][pointer] = "$|$";
			alignments[0][pointer] = "|";
			alignments[1][pointer] = name2;
			pointer++;
			calculateAlignmentRecursive(m, n - 1);
		} else if (s.equals("up")){
			String name1 = seq1.get(m - 1).getName();
			//name1 = name1.replaceAll("-", " ");
			//name1 = name1.replaceAll("_", "-");
			alignments[0][pointer] = name1;
			//alignments[1][pointer] = "$|$";
			alignments[1][pointer] = "|";
			pointer++;
			calculateAlignmentRecursive(m - 1, n);
		} else {
			alignments[0][pointer] = "ln";
			pointer++;
		}
	}
	
	public double getScore() {
		return maxScore;
	}
	
}
