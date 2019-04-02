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
import weka.core.FastVector;
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
	//private FastVector classValues;
//	private FastVector attributes;
	ArrayList<String> listOfParagraphs = new ArrayList<String>();

	// Make Training Data for the classifier
	public TrainSet(String trainSetFile, String outputPath)
			throws IOException, ParseException {
		this();
		final String trainSetFilePath = trainSetFile;
		System.out.println(trainSetFilePath);
		final FileInputStream fileInputStream2 = new FileInputStream(new File(trainSetFilePath));

		System.out.println("Adding class values to the trainset......\n");

		Map<String, String> paraHeadingMap = getParaHeading(trainSetFilePath);

		System.out.println("Adding class values to the trainset......\n");

		for (String heading : paraHeadingMap.keySet()) {
			addHeading(heading);
		}

		System.out.println("Done adding heading");

		setupAfterHeadingAdded();

		System.out.println("Now Adding para and class values to the trainset......\n");

		for (Entry<String, String> entry : paraHeadingMap.entrySet()) {
			addParagrah(entry.getValue(), entry.getKey());
		}

		System.out.println("Done adding para and class file");

		createDatasetFile(outputPath);

	}

	private void paragraphAdd(Data.Paragraph p) {
		final String paraId = p.getParaId();
		final String para = p.getTextOnly();

		addParagrah(para, paraId);

	}

	public TrainSet() {

		// Create vector of attributes.
		this.attributes = new ArrayList<Attribute>();
		// Add attribute for holding texts.
		this.attributes.add(new Attribute("text", true));
		// Add class attribute.
		this.classValues = new ArrayList<String>();

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

	// Text contain lot of Stop words.
	// This method is depreciated
	// Using this for time being.
	public String removeStopWords(String input) throws IOException {
		String sCurrentLine;
		Set<String> stopwords = new HashSet<String>();
		ArrayList<String> wordList = new ArrayList<String>();
		FileReader fr = new FileReader("/Users/Nithin/Desktop/stopwords.txt");
		BufferedReader br = new BufferedReader(fr);
		while ((sCurrentLine = br.readLine()) != null) {
			stopwords.add(sCurrentLine);
			System.out.println(sCurrentLine);

		}

		String[] output = input.split(" ");
		for (String word : output) {
			String wordCompare = word.toUpperCase();
			if (!stopwords.contains(wordCompare)) {
				wordList.add(word);
			}
		}

		String joinedString = StringUtils.join(wordList, " ");
		System.out.println(joinedString);

		return joinedString;
	}

	/*
	 * Find the instances of the keywords in the train set file if present add that
	 * page to training list page
	 */
	public Map<String, String> getParaHeading(String trainSetFilePath) throws FileNotFoundException {

		List<Data.Page> pageList = new ArrayList<Data.Page>();
		FileInputStream fileInputStream = new FileInputStream(new File(trainSetFilePath));
		String Heading = "";
		Map<String, String> mapParaHeading = new HashMap<>();

		int pageCount = 0;
		// loop through wikipedia page in its order
		for (Data.Page page : DeserializeData.iterableAnnotations(fileInputStream)) {
			pageCount++;
			String pageHeading = page.getPageId();
			Heading = pageHeading; // Heading will be page heading at the start of the page

			for (SectionPathParagraphs sectionPathParagraph : page.flatSectionPathsParagraphs()) {

				Iterator<Section> sectionPathIter = sectionPathParagraph.getSectionPath().iterator();

				// check for subheading
				while (sectionPathIter.hasNext()) {
					Section section = sectionPathIter.next();
					
					String sectionHeading = pageHeading + "/" + section.getHeadingId();  // fix the slash

					if (sectionPathIter.hasNext()) {
						Section nextSection = sectionPathIter.next();
						Heading = sectionHeading + "/" + nextSection.getHeadingId();
					} else {
						Heading = sectionHeading;
					}

				}
				System.out.println(pageCount + "  "  +Heading);
				
				String para = sectionPathParagraph.getParagraph().getTextOnly();
				System.out.println(para);
				mapParaHeading.put(Heading, para);
				System.out.println("adding to map");
				

			}

			


			if(pageCount == 1546204)
			{
				System.out.println("breaking here");
				break;
			}


			
		}
		return mapParaHeading;

	}

	public void setupAfterHeadingAdded() {
		attributes.add(new Attribute("@@class@@.", classValues));
		// Create dataset with initial capacity of 100, and set index of class.
		trainingData = new Instances("trainv2.0Set", attributes, 100);
		trainingData.setClassIndex(trainingData.numAttributes() - 1);
	}

	public void createDatasetFile(String path) throws IOException {
		path = path + "thirtyPercent" + "PagesInUnprocessedAllButBenchmark" + ".arff";
		File f = new File(path);
		f.createNewFile();
		FileWriter fw = new FileWriter(f);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(trainingData.toString());
		bw.close();
		System.out.println("check for arff file in " + path);
	}

	public Instances getTraningData() {
		return trainingData;
	}

}

