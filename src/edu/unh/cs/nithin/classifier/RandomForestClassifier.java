package edu.unh.cs.nithin.classifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.stemmers.LovinsStemmer;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class RandomForestClassifier {
	
	public RandomForestClassifier()
	{
		
	}

	public RandomForestClassifier(String arrfFile, String modelPath, String arffFileName) throws Exception {
		System.out.println("Training RF classifier with the trainset");
		DataSource trainSource = new DataSource(arrfFile);
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

		weka.core.SerializationHelper.write(modelPath + "/" +arffFileName + "RF_Page.model", fc);
	}
	
	public void buildRandomForestModel(String arrfFile, String modelPath, String arffFileName) throws Exception
	{
		arffFileName = arffFileName.replaceFirst("[.][^.]+$", "");
		System.out.println(arffFileName);
		System.out.println("Training RF classifier with the trainset");
		DataSource trainSource = new DataSource(arrfFile);
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

		weka.core.SerializationHelper.write(modelPath + "/" +arffFileName + ".model", fc);
	}


}
