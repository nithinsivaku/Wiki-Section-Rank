package edu.unh.cs.nithin.arrfTools;

import java.io.BufferedReader;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.netlib.util.booleanW;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.FeatureSequence2FeatureVector;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.PrintInputAndTarget;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.pipe.iterator.StringArrayIterator;
import cc.mallet.types.InstanceList;
import edu.unh.cs.nithin.tools.QueryIndex;
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.Data.Page;
import edu.unh.cs.treccar_v2.Data.Page.SectionPathParagraphs;
import edu.unh.cs.treccar_v2.Data.PageSkeleton;
import edu.unh.cs.treccar_v2.Data.Paragraph;
import edu.unh.cs.treccar_v2.Data.Section;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.StringToWordVector;

// Responsible for making the train Set
// Each paragraph and the paragraph id are given 
// to the classifier and trained

public class TrainSet implements Serializable {

	private Instances trainingData;
	private ArrayList<String> classValues;
	private ArrayList<Attribute> attributes;

	// Make Training Data for the classifier
	public TrainSet(String trainSetFile, String outputPath) throws IOException, ParseException {

		createCategoryTrainSet(trainSetFile, outputPath);
	}

	/*
	 * Create vector of attributes.
	 * Add attribute for holding texts.
	 * Add class attribute.
	 */
	public void initTrainSet() {
		this.attributes = new ArrayList<Attribute>();	
		this.attributes.add(new Attribute("text", true));
		this.classValues = new ArrayList<String>();

	}

	public void createCategoryTrainSet(String trainSetFile, String outputPath) throws IOException {
		
		int categoryCount= 0;
		
		CategoryTrainset ct = new CategoryTrainset();
		Map<String, ArrayList<Page>> categoryPageMap = ct.getCategoryPageMap(trainSetFile);
		
		int categoryTotal = categoryPageMap.size();
		
		
		for (Entry<String, ArrayList<Page>> entry : categoryPageMap.entrySet()) {
			
			initTrainSet(); // create attributes for every new category
			
			String category = entry.getKey();
			System.out.println("Adding pages names under category " + category + " ");
			ArrayList<Page> pageNames = entry.getValue();
			Map<String, String> headingParaMap = ct.getHeadingParaMap(pageNames);
			for(String heading :headingParaMap.keySet())
			{
				System.out.println(heading);
				addHeading(heading);
			}
			System.out.println("Done adding heading");

			setupAfterHeadingAdded();
			
			System.out.println("Adding class values to the trainset......\n");
			for(String heading :headingParaMap.keySet())
			{
				System.out.println( heading + headingParaMap.get(heading));
				addParagrah(headingParaMap.get(heading), heading);
			}
			System.out.println("Done Adding class values \n");

			System.out.println("category total: " + categoryTotal + " categoryCount: " + categoryCount );
			createDatasetFile(outputPath + category);
			
			categoryCount++;

		}
	}

	public void addHeading(String heading) {
		// if required, double the capacity.
		int capacity = classValues.size();
		if (classValues.size() > (capacity - 5)) {
			classValues.ensureCapacity(capacity * 2);
		}
		classValues.add(heading);
	}

	public void addParagrah(String paragraph, String classValue) throws IllegalStateException {
		paragraph = paragraph.toLowerCase();

		// Make message into instance.
		Instance instance = makeInstance(paragraph, trainingData);
		// Set class value for instance.
		instance.setClassValue(classValue);
		// Add instance to training data.
		trainingData.add(instance);

	}

	private Instance makeInstance(String paragraph, Instances data) {
		// Create instance of length two.
		Instance instance = new DenseInstance(2);

		// Set value for message attribute
		Attribute messageAtt = data.attribute("text");

		instance.setValue(messageAtt, messageAtt.addStringValue(paragraph));

		// Give instance access to attribute information from the dataset.
		instance.setDataset(data);
		return instance;
	}


	public void setupAfterHeadingAdded() {
		attributes.add(new Attribute("@@class@@.", classValues));
		// Create dataset with initial capacity of 100, and set index of class.
		trainingData = new Instances("trainv2.0Set", attributes, 100);
		trainingData.setClassIndex(trainingData.numAttributes() - 1);
	}

	public void createDatasetFile(String path) throws IOException {
		path = path + ".arff";
		File f = new File(path);
		f.createNewFile();
		FileWriter fw = new FileWriter(f);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(trainingData.toString());
		bw.close();
		System.out.println("check for arff file in " + path);
		
	}


}
