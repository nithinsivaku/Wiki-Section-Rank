package edu.unh.cs.nithin.classifier;
import java.io.File;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.stemmers.LovinsStemmer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.filters.unsupervised.instance.Resample;

public class RandomForestClassifier {
	
	private String outputFilePath;
	private String arrfFilePath;
	
	public RandomForestClassifier(String outputPath) {
		setArrfFilePath(outputPath);
		setOutputFilePath(outputPath);
	}

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
			String arffFilePath = getOutputFilePath() + "/" +arrfFileName + ".model";
			weka.core.SerializationHelper.write(arffFilePath, nb);
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
		this.outputFilePath = outputFilePath+"/models";
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
		this.arrfFilePath = outputPath+"/enviromentTrainset";
	}


}
