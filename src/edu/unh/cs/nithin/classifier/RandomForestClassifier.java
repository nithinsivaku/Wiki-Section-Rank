package edu.unh.cs.nithin.classifier;
import java.io.File;

import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.stemmers.LovinsStemmer;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class RandomForestClassifier {
	
	private String outputFilePath;
	private String arrfFilePath;
	
	public RandomForestClassifier(String outputPath, String arrfPath) {
		setArrfFilePath(arrfPath);
		setOutputFilePath(outputPath);
	}

	public void buildRandomForestModel() throws Exception {
		File arrfDir = new File(getArrfFilePath());
		for(File arrfFile : arrfDir.listFiles()) {
			String arrfFileName = arrfFile.getName();
			System.out.println("Training RF classifier with the trainset");
			DataSource trainSource = new DataSource(arrfFile.getAbsolutePath()); //may be wrong
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

			weka.core.SerializationHelper.write(getOutputFilePath() + "/" +arrfFileName + ".model", fc);
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
		this.outputFilePath = outputFilePath;
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
	public void setArrfFilePath(String arrfFilePath) {
		this.arrfFilePath = arrfFilePath;
	}


}
