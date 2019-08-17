/**
 * @Author: Nithin
 * @Date:   2019-03-17T17:15:55-04:00
 * @Last modified by:   Nithin
 * @Last modified time: 2019-08-11T18:13:00-04:00
 */



package edu.unh.cs.nithin.classifier;

import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.stemmers.LovinsStemmer;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class NaiveBayesClassifier {

	public NaiveBayesClassifier(String arffFile, String modelPath, String arffFileName) throws Exception {
		DataSource trainSource = new DataSource(arffFile);
		Instances trainingSet = trainSource.getDataSet();
		if (trainingSet.classIndex() == -1)
			trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
		weka.classifiers.bayes.NaiveBayes nb = new weka.classifiers.bayes.NaiveBayes();
		StringToWordVector filter = new StringToWordVector();
		filter.setInputFormat(trainingSet);
		filter.setIDFTransform(true);
//		filter.setUseStoplist(true);
		System.out.println("Filter applied - StringtoWord");
		LovinsStemmer stemmer = new LovinsStemmer();
		filter.setStemmer(stemmer);
		filter.setLowerCaseTokens(true);
		System.out.println("Stemmer done");
		// Create the FilteredClassifier object
		FilteredClassifier fc = new FilteredClassifier();
		// specify filter
		fc.setFilter(filter);
		// specify base classifier
		// specify base classifier
		fc.setClassifier(nb);
		System.out.println("building Naive Bayes Classifier...");
		fc.buildClassifier(trainingSet);

		// nb.buildClassifier(trainingSet);

		System.out.println(nb.getClass().toString());

		weka.core.SerializationHelper.write(modelPath + "/" +arffFileName + "NB_Page.model", fc);

		// System.out.println("model saved in " + );

	}

}
