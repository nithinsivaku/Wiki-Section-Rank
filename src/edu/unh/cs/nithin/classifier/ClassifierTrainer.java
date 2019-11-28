/**
 * @Author: Nithin Sivakumar <Nithin>
 * @Date:   2019-11-27T19:37:33-05:00
 * @Last modified by:   Nithin
 * @Last modified time: 2019-11-27T21:12:06-05:00
 */
package edu.unh.cs.nithin.classifier;

import java.io.File;

import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.stemmers.LovinsStemmer;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class ClassifierTrainer {

	private String outputFilePath;
	private String arrfFilePath;

	/**
	 *
	 * @param outputPath
	 */
	public ClassifierTrainer(String outputPath) {
		setArrfFilePath(outputPath);
		setOutputFilePath(outputPath);
	}

	/**
	 * Train a Naive bayes classifier for all the files present in trainset folder
	 * @throws Exception 
	 */
	public void buildNaiveBayesModel() throws Exception {
		File arrfDir = new File(getArrfFilePath());
		for(File arrfFile : arrfDir.listFiles()) {
			String arrfFileName = arrfFile.getName().toString().replaceFirst("[.][^.]+$", "");
			System.out.println("Training NB classifier for -" + arrfFileName);
			DataSource trainSource = new DataSource(arrfFile.getAbsolutePath());
			Instances trainingSet = trainSource.getDataSet();
			System.out.println("loaded dataSet");
			if (trainingSet.classIndex() == -1)
				trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
			NaiveBayes nb = new NaiveBayes();
			System.out.println("build Started");
			// the filter
			StringToWordVector filter = new StringToWordVector();
			filter.setInputFormat(trainingSet);
			filter.setIDFTransform(true);
			System.out.println("Filter applied - StringtoWord");
			LovinsStemmer stemmer = new LovinsStemmer();
			filter.setStemmer(stemmer);
			filter.setLowerCaseTokens(true);
			System.out.println("Stemmer done");
			
			// Create the FilteredClassifier object
			FilteredClassifier fc = new FilteredClassifier();
			
			// specify filter
			fc.setFilter(filter);
			fc.setClassifier(nb);
			
			// Build the meta-classifier
			fc.buildClassifier(trainingSet);
			String nbDirectory = getOutputFilePath() + "/NaiveBayes/";
			String arffFilePath = nbDirectory + arrfFileName + ".model";
			File f = new File(nbDirectory);
		    if(!f.exists()){
		        f.mkdirs();
		    }
			weka.core.SerializationHelper.write(arffFilePath, nb);
			System.out.println("Naive Bayes Classifier model built at " + arffFilePath + " ");
		}
	}

	/**
	 * Train a Random Forest classifier for all the files present in trainset folder
	 * @throws Exception 
	 * 
	 */
	public void buildRandomForestModel() throws Exception {
		File arrfDir = new File(getArrfFilePath());
		for(File arrfFile : arrfDir.listFiles()) {
			String arrfFileName = arrfFile.getName().toString().replaceFirst("[.][^.]+$", "");
			System.out.println("Training RF classifier for -" + arrfFileName);
			DataSource trainSource = new DataSource(arrfFile.getAbsolutePath());
			Instances trainingSet = trainSource.getDataSet();
			System.out.println("loaded dataSet");
			if (trainingSet.classIndex() == -1)
				trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
			RandomForest rf = new RandomForest();
			System.out.println("build Started");
			// the filter
			StringToWordVector filter = new StringToWordVector();
			filter.setInputFormat(trainingSet);
			filter.setIDFTransform(true);
			System.out.println("Filter applied - StringtoWord");
			LovinsStemmer stemmer = new LovinsStemmer();
			filter.setStemmer(stemmer);
			filter.setLowerCaseTokens(true);
			System.out.println("Stemmer done");
			
			// Create the FilteredClassifier object
			FilteredClassifier fc = new FilteredClassifier();
			
			// specify filter
			fc.setFilter(filter);
			fc.setClassifier(rf);
			
			// Build the meta-classifier
			fc.buildClassifier(trainingSet);
			String nbDirectory = getOutputFilePath() + "/RandomForest/";
			String arffFilePath = nbDirectory + arrfFileName + ".model";
			File f = new File(nbDirectory);
		    if(!f.exists()){
		        f.mkdirs();
		    }
			weka.core.SerializationHelper.write(arffFilePath, rf);
			System.out.println("Random Forest Classifier model built at " + arffFilePath + " ");
		}

	}

	/**
	 * @return the outputFilePath
	 */
	public String getOutputFilePath() {
		return outputFilePath;
	}

	/**
	 * @param outputFilePath the outputFilePath to set
	 */
	public void setOutputFilePath(String outputFilePath) {
		this.outputFilePath = outputFilePath + "/models";
	}

	/**
	 * @return the arrfFilePath
	 */
	public String getArrfFilePath() {
		return arrfFilePath;
	}

	/**
	 * @param arrfFilePath the arrfFilePath to set
	 */
	public void setArrfFilePath(String outputPath) {
		this.arrfFilePath = outputPath + "/trainset";
	}

}
