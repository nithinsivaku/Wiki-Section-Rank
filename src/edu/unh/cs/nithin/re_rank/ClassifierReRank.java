package edu.unh.cs.nithin.re_rank;

import java.io.BufferedReader;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class ClassifierReRank {
	
	private String outputPath;
	private String indexPath;
	private float predictionConfidence = (float) 0.2;
	
	/**
	 * set output file paths
	 * @param runFile
	 * @param indexPath
	 * @param outputPath
	 * @throws Exception
	 */
	public ClassifierReRank(String runFile, String indexPath, String outputPath) throws Exception {
		setIndexPath(indexPath);
		setOutputPath(outputPath);
	}
	
	/**
	 * Reads the runfile line by line and predicts the paraheading for the pargraph in each line 
	 * @param runFile filePath
	 * @param modelName modelName to load 
	 * @throws Exception
	 */
	public void classifyRunFile(String runFile, String modelName) throws Exception {
		String outPath = getOutputPath();
		String indexPath = getIndexPath();
		String modelPath = getFilePath("models", outPath, modelName);
		String trainsetPath = getFilePath("trainset", outPath, modelName);
		
		System.out.println(" loading Random Forest Classifier");
		System.out.println("Model Loading.......................");
		Classifier cls_RF = (Classifier) weka.core.SerializationHelper.read(modelPath);
		System.out.println("Random Forest Classifier loaded successfully");
		System.out.println("*****************************************************************************\n");

		// Load the trainset data format
		DataSource source = new DataSource(trainsetPath);
		Instances trainingData = source.getDataSet();
		trainingData.setClassIndex(trainingData.numAttributes() - 1);

		File outRunfile = new File(outputPath + "/rerank/" +  modelName);
		outRunfile.createNewFile();
		FileWriter writer = new FileWriter(outRunfile);
		File file = new File(runFile);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		while (((st = br.readLine()) != null)) {
			String[] tokens = st.split(" ");
			String paraId = tokens[2];
			String paragraph = getParagraphForId(indexPath, paraId);

			Instances testset = trainingData.stringFreeStructure();
			Instance insta = makeInstance(paragraph, testset);
			double predicted = cls_RF.classifyInstance(insta);
			double[] prediction = cls_RF.distributionForInstance(insta);
			double predictionRate = prediction[(int) predicted];
			if(predictionRate >= predictionConfidence) {
				String predictedClass = trainingData.classAttribute().value((int) predicted);
				writer.write(predictedClass + " " + predictionRate + " " + paraId + " " + 1 + " " + tokens[4] + " Classifier\n");
				System.out.println(predictedClass + predictionRate + paraId + " " + 1 + " " + tokens[4] + " Classifier\n");
			}
		}
		writer.flush();
		writer.close();
		System.out.println("Writen  classified results\nQuery Done!" + outRunfile.getName());

	}

	/**
	 * return the file path based on method name
	 * @param outPath
	 * @return
	 */
	private String getFilePath(String choice, String outPath, String modelName) {
		String filePath = "";
		String outputPath = getOutputPath();
		switch(choice) {
		case "trainset" : 
			filePath = outputPath + choice + "/" + modelName + ".arff";
			break;
		case "models" :
			filePath = outputPath + choice + "/" + modelName + ".model";
			break;
		}
		return filePath;
	}

	private Instance makeInstance(String text, Instances data) {
		// Create instance of length two.
		Instance instance = new DenseInstance(2);
		// Set value for message attribute
		Attribute messageAtt = data.attribute("text");
		instance.setValue(messageAtt, messageAtt.addStringValue(text));
		// Give instance access to attribute information from the dataset.
		instance.setDataset(data);
		return instance;
	}

	private String getParagraphForId(String indexPath, String paraId) throws IOException, ParseException {
		// TODO Auto-generated method stub
		String paragraph = null;
		Analyzer analyzer = new StandardAnalyzer();
		IndexSearcher searcher = setupIndexSearcher(indexPath, "paragraph.lucene");
		searcher.setSimilarity(new BM25Similarity());
		final MyQueryBuilder queryBuilder = new MyQueryBuilder(new StandardAnalyzer());

		QueryParser qp = new QueryParser("paraid", analyzer);
		TopDocs tops = searcher.search(queryBuilder.toQuery(paraId), 1);
		ScoreDoc[] scoreDoc = tops.scoreDocs;
		for (int i = 0; i < scoreDoc.length; i++) {
			ScoreDoc score = scoreDoc[i];
			final Document doc = searcher.doc(score.doc);
			paragraph = doc.getField("text").stringValue();

		}

		String paraText = searcher.doc(searcher.search(queryBuilder.toQuery(paraId), 1).scoreDocs[0].doc)
				.getField("text").stringValue();

		return paraText;
	}

	// Author: Laura dietz
	public static class MyQueryBuilder {

		private final StandardAnalyzer analyzer;
		private List<String> tokens;

		public MyQueryBuilder(StandardAnalyzer standardAnalyzer) {
			analyzer = standardAnalyzer;
			tokens = new ArrayList<>(128);
		}

		public BooleanQuery toQuery(String queryStr) throws IOException {

			TokenStream tokenStream = analyzer.tokenStream("paragraphid", new StringReader(queryStr));
			tokenStream.reset();
			tokens.clear();
			while (tokenStream.incrementToken()) {
				final String token = tokenStream.getAttribute(CharTermAttribute.class).toString();
				tokens.add(token);
			}
			tokenStream.end();
			tokenStream.close();
			BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
			for (String token : tokens) {
				booleanQuery.add(new TermQuery(new Term("paragraphid", token)), BooleanClause.Occur.SHOULD);
			}
			return booleanQuery.build();
		}
	}

	// Author: Laura dietz
	private static IndexSearcher setupIndexSearcher(String indexPath, String typeIndex) throws IOException {
		Path path = FileSystems.getDefault().getPath(indexPath, typeIndex);
		Directory indexDir = FSDirectory.open(path);
		IndexReader reader = DirectoryReader.open(indexDir);
		return new IndexSearcher(reader);
	}

	/**
	 * @return the outputPath
	 */
	public String getOutputPath() {
		return outputPath;
	}

	/**
	 * @param outputPath the outputPath to set
	 */
	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	/**
	 * @return the indexPath
	 */
	public String getIndexPath() {
		return indexPath;
	}

	/**
	 * @param indexPath the indexPath to set
	 */
	public void setIndexPath(String indexPath) {
		this.indexPath = indexPath;
	}

	/**
	 * @param runFile
	 * @throws Exception 
	 */
	public void classifyRunFile(String runFile) throws Exception {
		String outPath = getOutputPath();
		String indexPath = getIndexPath();
		String modelPath = "/Users/Nithin/Desktop/outputFilesIR/models/Category_Environmental_terminology.model";
		String trainsetPath = "/Users/Nithin/Desktop/outputFilesIR/trainset/Category_Environmental_terminology.arff";
		
		System.out.println(" loading Random Forest Classifier");
		System.out.println("Model Loading.......................");
		Classifier cls_RF = (Classifier) weka.core.SerializationHelper.read(modelPath);
		System.out.println("Random Forest Classifier loaded successfully");
		System.out.println("*****************************************************************************\n");

		// Load the trainset data format
		DataSource source = new DataSource(trainsetPath);
		Instances trainingData = source.getDataSet();
		trainingData.setClassIndex(trainingData.numAttributes() - 1);

		File outRunfile = new File(outputPath + "/rerank/" +  "ecology");
		outRunfile.createNewFile();
		FileWriter writer = new FileWriter(outRunfile);
		File file = new File(runFile);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		while (((st = br.readLine()) != null)) {
			String[] tokens = st.split(" ");
			String paraId = tokens[2];
			String paragraph = getParagraphForId(indexPath, paraId);

			Instances testset = trainingData.stringFreeStructure();
			Instance insta = makeInstance(paragraph, testset);
			double predicted = cls_RF.classifyInstance(insta);
			double[] prediction = cls_RF.distributionForInstance(insta);
			double predictionRate = prediction[(int) predicted];
			String classLabel = trainingData.classAttribute().value((int) predicted);
			if(predictionRate >= predictionConfidence) {
				String predictedClass = trainingData.classAttribute().value((int) predicted);
				writer.write(predictedClass + " " + predictionRate + " " + paraId + " " + 1 + " " + tokens[4] + " Classifier\n");
				System.out.println(predictedClass + predictionRate + paraId + " " + 1 + " " + tokens[4] + " Classifier\n");
			}
		}
		writer.flush();
		writer.close();
		System.out.println("Writen  classified results\nQuery Done!" + outRunfile.getName());
		
	}

}
