package edu.unh.cs.nithin.re_rank;

import weka.classifiers.Classifier;

public class ClassifierReRank {

	public ClassifierReRank(String runFile, String rfModel, String nbModel, String outputPath) throws Exception
	{
		classifyRunFileUsingRandomForestClassifier(runFile, rfModel, outputPath);
		classifyRunFileUsingNaiveBayesClassifier(runFile, nbModel, outputPath);
	}

	/*
	 * 
	 */
	private void classifyRunFileUsingNaiveBayesClassifier(String runFile, String nbModel, String outputPath) throws Exception {

		System.out.println(" loading NaiveBayes Classifier");
		System.out.println("Model Loading.......................");
		Classifier cls_NB = (Classifier) weka.core.SerializationHelper.read(nbModel);
		System.out.println("NaiveBayes Classifier loaded successfully");
		System.out.println("*****************************************************************************\n");
	}

	/*
	 * 
	 */
	private void classifyRunFileUsingRandomForestClassifier(String runFile, String rfModel, String outputPath) throws Exception {

		System.out.println(" loading Random Forest Classifier");
		System.out.println("Model Loading.......................");
		Classifier cls_RF = (Classifier) weka.core.SerializationHelper.read(rfModel);
		System.out.println("Random Forest Classifier loaded successfully");
		System.out.println("*****************************************************************************\n");
		
		
	}
	
}
