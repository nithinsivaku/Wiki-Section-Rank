/**
 * @Author: Nithin
 * @Date:   2019-03-17T17:15:55-04:00
 * @Last modified by:   Nithin
 * @Last modified time: 2019-11-27T21:11:53-05:00
 */
package edu.unh.cs.nithin.arrfTools;

import java.io.BufferedWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.unh.cs.nithin.tools.CarHelper;
import edu.unh.cs.treccar_v2.Data.Page;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;


/**
 * Responsible for making the train Set in arff format.
 * Each paragraph and the paragraph id are stored in trainset File
 * which then will will used by the classfifier to learn
 */
public class TrainSet implements Serializable {

	// Weka constants
	private Instances trainingData;
	private ArrayList<String> classValues;
	private ArrayList<Attribute> attributes;

	// custom constants
	private Map<String, List<Page>> entries;
	private String outputFilePath;

	/**
	 * @param categoryPages
	 */
	public TrainSet(Map<String, List<Page>> categoryPages, String outputPath) {
		setOutputFilePath(outputPath);
		setEntries(categoryPages);
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

	/**
	 * createCategoryTrainSet - given a map of category and list of pages
	 * creates a weka accepted trainset file.
	 * @throws IOException
	 */
	public void createCategoryTrainSet() throws IOException {
		Map<String, List<Page>> categoriesPages = getEntries();
		for(Entry<String, List<Page>> categoryPages : categoriesPages.entrySet()) {
			initTrainSet(); // initialise attributes for every new category
			String categoryName = categoryPages.getKey();
			System.out.println("Adding pages names under category " + categoryName + " ");
			List<Page> pages = categoryPages.getValue();
			CarHelper helper = new CarHelper(pages);
			Map<String, String> headingPara = helper.getHeadingPara();
			Set<String> uniqueHeadings = helper.getUniqueHeadings();

			// add class labels before paragraph
			for(String heading : uniqueHeadings) {
				System.out.println(heading);
				addHeading(heading);
			}
			System.out.println("Done adding heading");
			setupAfterHeadingAdded();

			// add paragraph and class label
			for(String heading : headingPara.keySet()) {
				System.out.println( heading + headingPara.get(heading));
				addParagrah(headingPara.get(heading), heading);
			}
			System.out.println("Done Adding class values \n");
			createDatasetFile(getOutputFilePath() + "/trainset/" + categoryName.replaceAll("[^A-Za-z0-9]", "_"));
		}
	}

	/**
	 * [addHeading add the class label to the fle header before assigning the label to the document]
	 * @param heading [class label]
	 */
	public void addHeading(String heading) {
		// if required, double the capacity.
		int capacity = classValues.size();
		if (classValues.size() > (capacity - 5)) {
			classValues.ensureCapacity(capacity * 2);
		}
		classValues.add(heading);
	}

	/**
	 * [addParagrah add the paragraph and its corresponding class label to the trainset]
	 * @param  paragraph             [paragraph]
	 * @param  classValue            [label]
	 * @throws IllegalStateException
	 */
	public void addParagrah(String paragraph, String classValue) throws IllegalStateException {
		paragraph = paragraph.toLowerCase();

		// Make message into instance.
		Instance instance = makeInstance(paragraph, trainingData);
		// Set class value for instance.
		instance.setClassValue(classValue);
		// Add instance to training data.
		trainingData.add(instance);

	}

	/**
	 * [makeInstance create instance of length 2, assign paragrah to text attribute]
	 * @param  paragraph [paragraph]
	 * @param  data      [intances]
	 * @return           [single instance]
	 */
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

	/**
	 * [setupAfterHeadingAdded after all the labels have been added to the file, setup the attributes to add para nad class]
	 */
	public void setupAfterHeadingAdded() {
		attributes.add(new Attribute("@@class@@.", classValues));
		// Create dataset with initial capacity of 100, and set index of class.
		trainingData = new Instances("trainv2.0Set", attributes, 100);
		trainingData.setClassIndex(trainingData.numAttributes() - 1);
	}

	/**
	 * [createDatasetFile Write everything to the file in arff format]
	 * @param  path        [path to the file]
	 * @throws IOException
	 */
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

	/**
	 * @return the entries
	 */
	public Map<String, List<Page>> getEntries() {
		return entries;
	}

	/**
	 * @param entries the entries to set
	 */
	public void setEntries(Map<String, List<Page>> entries) {
		this.entries = entries;
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
}
