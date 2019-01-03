package edu.unh.cs.nithin.customParas;

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
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.StringToWordVector;

// Responsible for making the train Set
// Each paragraph and the paragraph id are given 
// to the classifier and trained

public class CustomTrainSetGenerator implements Serializable {

	private Instances trainingData;
	private FastVector classValues;
	private FastVector attributes;
	ArrayList<String> listOfParagraphs = new ArrayList<String>();

	// Make Training Data for the classifier
	public CustomTrainSetGenerator(String trainSetFile, String outputPath, String indexPath, CharSequence[] cs)
			throws IOException, ParseException {
		this();
		final String trainSetFilePath = trainSetFile;
		System.out.println(trainSetFilePath);
		final FileInputStream fileInputStream2 = new FileInputStream(new File(trainSetFilePath));

		System.out.println("Adding class values to the trainset......\n");
		QueryIndex qi = new QueryIndex();

		List<Data.Page> matchingPageList = new ArrayList<>();
		matchingPageList = matchingPage(trainSetFilePath, cs);
		
		System.out.println("Adding class values to the trainset......\n");
		
		for(Data.Page page : matchingPageList)
		{
			final String pageId = page.getPageId();
			addHeading(pageId);
		}
		
		System.out.println("Done adding heading");

		setupAfterHeadingAdded();
		
		System.out.println("Now Adding para and class values to the trainset......\n");
		
		for(Data.Page page : matchingPageList)
		{
			
			for( SectionPathParagraphs sectionpara : page.flatSectionPathsParagraphs())
			{
				final String para = sectionpara.getParagraph().getTextOnly();
				final String pageId = page.getPageId();
				
				System.out.println(" Page ID =  " + pageId);
				System.out.println(" Para    =  " + para);

				addParagrah(para, pageId);				
			}
			System.out.print(".");
		}

		System.out.println("Done adding para and class file");

		createDatasetFile(outputPath);
		
		
	}

	private void paragraphAdd(Data.Paragraph p) {
		final String paraId = p.getParaId();
		final String para = p.getTextOnly();
		

		addParagrah(para, paraId);

	}

	public CustomTrainSetGenerator() {

		// Create vector of attributes.
		this.attributes = new FastVector(2);
		// Add attribute for holding texts.
		this.attributes.addElement(new Attribute("text", (FastVector) null));
		// Add class attribute.
		this.classValues = new FastVector();

	}

	public void addHeading(String heading) {
		// if required, double the capacity.
		int capacity = classValues.capacity();
		if (classValues.size() > (capacity - 5)) {
			classValues.setCapacity(capacity * 2);
		}
		classValues.addElement(heading);
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
		Instance instance = new Instance(2);

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
	public List<Data.Page> matchingPage(String trainSetFilePath, CharSequence[] cs) throws FileNotFoundException {
		List<Data.Page> pageList = new ArrayList<Data.Page>();

		FileInputStream fileInputStream = new FileInputStream(new File(trainSetFilePath));

		Boolean addPage = false;

		int i = 0;
		for (Data.Page page : DeserializeData.iterableAnnotations(fileInputStream)) {

			if(i == 20)
				break;
			for (CharSequence sequence : cs) {
				System.out.println("Searching for " + sequence + " in " + page.getPageName());
				if (page.getSkeleton().toString().contains(sequence)) {
					addPage = true;
				}
			}

			if (addPage) {
				i++;
				System.out.println("adding - " + page.getPageName());
				pageList.add(page);
			}

		}

		return pageList;

	}

	// Small bugs in this function..
	public Pipe buildPipe() {

		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		// Read data from File objects
		pipeList.add(new Input2CharSequence("UTF-8"));

		// Regular expression for what constitutes a token.
		// This pattern includes Unicode letters, Unicode numbers,
		// and the underscore character. Alternatives:
		// "\\S+" (anything not whitespace)
		// "\\w+" ( A-Z, a-z, 0-9, _ )
		// "[\\p{L}\\p{N}_]+|[\\p{P}]+" (a group of only letters and numbers OR
		// a group of only punctuation marks)
		Pattern tokenPattern = Pattern.compile("[\\p{L}\\p{N}_]+");

		// Tokenize raw strings
		pipeList.add(new CharSequence2TokenSequence(tokenPattern));

		// Normalize all tokens to all lowercase
		pipeList.add(new TokenSequenceLowercase());

		// Remove stopwords from a standard English stoplist.
		// options: [case sensitive] [mark deletions]
		pipeList.add(new TokenSequenceRemoveStopwords(new File("/Users/Nithin/Desktop/stopwords.txt"), "UTF-8", false,
				false, false));

		// Rather than storing tokens as strings, convert
		// them to integers by looking them up in an alphabet.
		// pipeList.add(new TokenSequence2FeatureSequence());

		// Do the same thing for the "target" field:
		// convert a class label string to a Label object,
		// which has an index in a Label alphabet.
		// pipeList.add(new Target2Label());

		// Now convert the sequence of features to a sparse vector,
		// mapping feature IDs to counts.
		pipeList.add(new FeatureSequence2FeatureVector());

		// Print out the features and the label
		pipeList.add(new PrintInputAndTarget());

		InstanceList instances = new InstanceList(buildPipe());

		instances.addThruPipe(new ArrayIterator(listOfParagraphs));

		for (cc.mallet.types.Instance inst : instances) {
			System.out.println(inst.getName());
		}

		return new SerialPipes(pipeList);

	}

	public void setupAfterHeadingAdded() {
		attributes.addElement(new Attribute("@@class@@.", classValues));
		// Create dataset with initial capacity of 100, and set index of class.
		trainingData = new Instances("trainv2.0Set", attributes, 100);
		trainingData.setClassIndex(trainingData.numAttributes() - 1);
	}

	public void createDatasetFile(String path) throws IOException {
		path = path + "/20" + "Pages" + ".arff";
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
