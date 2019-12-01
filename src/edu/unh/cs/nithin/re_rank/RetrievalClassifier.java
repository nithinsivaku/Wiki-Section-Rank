/**
 * @Author: Nithin Sivakumar <Nithin>
 * @Date:   2019-11-30T17:18:02-05:00
 * @Last modified by:   Nithin
 * @Last modified time: 2019-11-30T17:19:22-05:00
 */
package edu.unh.cs.nithin.re_rank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class RetrievalClassifier {
	
	private String outputPath;
	private String indexPath;
	private float predictionConfidence;
	private String pagesFile;
	private String folderPath;
	
	/**
	 * set output file paths
	 * @param indexPath
	 * @param outputPath
	 * @param predConf
	 */
	public RetrievalClassifier(String indexPath, String outputPath, float predConf, String pagesFile, String folderPath) {
		setIndexPath(indexPath);
		setOutputPath(outputPath);
		setPredictionConfidence(predConf);
		setPagesFile(pagesFile);
		setFolderPath(folderPath);
	}
	
	/**
	 * Execute bm25 for section level paragraphs for given category in outlines.cbor
	 * @param catName
	 * @throws IOException
	 */
	public void runBm25(String catName) throws IOException {
		File runfile = new File(getOutputPath() + "/bm25/" + catName);
		runfile.createNewFile();
		FileWriter writer = new FileWriter(runfile);
		IndexSearcher searcher = setupIndexSearcher(getIndexPath(), "paragraph.lucene");
		searcher.setSimilarity(new BM25Similarity());
		final MyQueryBuilder queryBuilder = new MyQueryBuilder(new StandardAnalyzer());
		final FileInputStream fileInputStream3 = new FileInputStream(new File(getPagesFile()));
		System.out.println("starting searching for sections ...");
		int count = 0;
		for (Data.Page page : DeserializeData.iterableAnnotations(fileInputStream3)) {
			ArrayList<String> categories = page.getPageMetadata().getCategoryNames();
			if (categories.contains(catName)) {
				System.out.println(page.getPageId() + " ----- " );
				for (List<Data.Section> sectionPath : page.flatSectionPaths()) {
					final String queryId = Data.sectionPathId(page.getPageId(), sectionPath);
					String queryStr = buildSectionQueryStr(page, sectionPath);
					System.out.println(queryStr);
					TopDocs tops = searcher.search(queryBuilder.toQuery(queryStr), 100);
					ScoreDoc[] scoreDoc = tops.scoreDocs;
					for (int i = 0; i < scoreDoc.length; i++) {
						ScoreDoc score = scoreDoc[i];
						final Document doc = searcher.doc(score.doc);
						final String paragraphid = doc.getField("paragraphid").stringValue();
						final float searchScore = score.score;
						final int searchRank = i + 1;
						writer.write(queryId + " Q0 " + paragraphid + " " + searchRank + " " + searchScore
								+ " Lucene-BM25\n");
						count++;
					}
				}
			}
		}
		writer.flush();
		writer.close();
		System.out.println("Write " + count + " results\nQuery Done!");
	}
	
	/**
	 * @param catName
	 * @throws Exception 
	 */
	public void classifyRunfiles(String catName) throws Exception {
		
		String reRankNBPath = getOutputPath()+ "/rerank/NaiveBayes";
		String reRankRFPath = getOutputPath()+ "rerank/RandomForest";
		File f1 = new File(reRankNBPath);
		File f2 = new File(reRankRFPath);
		if(!f1.exists()) f1.mkdirs();
		if(!f2.exists()) f2.mkdirs();
		
		// Load classifier and rerank runfiles
		Classifier naiveBayesModel = loadModel(catName, "NaiveBayes");
		Classifier randomForestModel = loadModel(catName, "RandomForest");
		classifyRunfile(catName, naiveBayesModel, reRankNBPath);
		classifyRunfile(catName, randomForestModel, reRankRFPath);
	}
	
	/**
	 * @param catName
	 * @param outPath 
	 * @param naiveBayesModel
	 * @throws Exception 
	 */
	private void classifyRunfile(String catName, Classifier model, String outPath) throws Exception {
		
		// Load the trainset data format
		String trainsetPath = getFolderPath()+"/trainset/" + catName + ".arff";
		DataSource source = new DataSource(trainsetPath);
		Instances trainingData = source.getDataSet();
		trainingData.setClassIndex(trainingData.numAttributes() - 1);
		
		// create outfile object and setup writer
		File outRunfile = new File(outputPath + "/" +catName);
		outRunfile.createNewFile();
		FileWriter writer = new FileWriter(outRunfile);
		
		// load the runfile genrated before and classify each result
		String runFile = getOutputPath()+ "/bm25/" + catName;
		File file = new File(runFile);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		while (((st = br.readLine()) != null)) {
			String[] tokens = st.split(" ");
			String paraId = tokens[2];
			String paragraph = getParagraphForId(indexPath, paraId);

			Instances testset = trainingData.stringFreeStructure();
			Instance insta = makeInstance(paragraph, testset);
			double predicted = model.classifyInstance(insta);
			double[] prediction = model.distributionForInstance(insta);
			double predictionRate = prediction[(int) predicted];
			if(predictionRate >= predictionConfidence) {
				String predictedClass = trainingData.classAttribute().value((int) predicted);
				writer.write(predictedClass + " " + predictionRate + " " + paraId + " " + 1 + " " + tokens[4] + " Classifier\n");
				System.out.println(predictedClass + predictionRate + paraId + " " + 1 + " " + tokens[4] + " Classifier\n");
			} else {
				writer.write(tokens[0] + " " + tokens[1] + " " + paraId + " " + 1 + " " + tokens[4] + " Classifier\n");
			}
		}
		writer.flush();
		writer.close();
		System.out.println("Writen  classified results\nQuery Done!" + outRunfile.getName());
	}


	
	/**
	 * @param indexPath2
	 * @param paraId
	 * @return
	 * @throws IOException 
	 */
	private String getParagraphForId(String indexPath2, String paraId) throws IOException {
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

	private Classifier loadModel(String catName, String classifierName) throws Exception {
		String modelPath = getFolderPath()+"/models/" + classifierName + "/";
		modelPath = modelPath + catName + ".model";
		System.out.println("loading " + classifierName + "Classifier model");
		System.out.println("Model Loading.......................");
		Classifier model = (Classifier) weka.core.SerializationHelper.read(modelPath);
		System.out.println("Model Loaded successfully");
		return model;
	}

	/**
	 * @author Laura Dietz
	 * Modified by : Nithin
	 * Modified Date : Nov 30, 2019 5:38:40 PM
	 */
	public static class MyQueryBuilder {

		private final StandardAnalyzer analyzer;
		private List<String> tokens;

		public MyQueryBuilder(StandardAnalyzer standardAnalyzer) {
			analyzer = standardAnalyzer;
			tokens = new ArrayList<>(128);
		}

		public BooleanQuery toQuery(String queryStr) throws IOException {

			TokenStream tokenStream = analyzer.tokenStream("text", new StringReader(queryStr));
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
				booleanQuery.add(new TermQuery(new Term("text", token)), BooleanClause.Occur.SHOULD);
			}
			return booleanQuery.build();
		}
	}
	
	/**
	 * Initialize the index searcher
	 * @param indexPath
	 * @param typeIndex
	 * @return indexsearcher object
	 * @throws IOException
	 */
	private static IndexSearcher setupIndexSearcher(String indexPath, String typeIndex) throws IOException {
		Path path = FileSystems.getDefault().getPath(indexPath, typeIndex);
		Directory indexDir = FSDirectory.open(path);
		IndexReader reader = DirectoryReader.open(indexDir);
		return new IndexSearcher(reader);
	}
	
	/**
	 * Build english query from unstructured text
	 * @param page
	 * @param sectionPath
	 * @return query string
	 */
	private static String buildSectionQueryStr(Data.Page page, List<Data.Section> sectionPath) {
		StringBuilder queryStr = new StringBuilder();
		queryStr.append(page.getPageName());
		for (Data.Section section : sectionPath) {
			queryStr.append(" ").append(section.getHeading());
		}
		return queryStr.toString();
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
	 * @return the predictionConfidence
	 */
	public float getPredictionConfidence() {
		return predictionConfidence;
	}

	/**
	 * @param predictionConfidence the predictionConfidence to set
	 */
	public void setPredictionConfidence(float predictionConfidence) {
		this.predictionConfidence = predictionConfidence;
	}

	/**
	 * @return the pagesFile
	 */
	public String getPagesFile() {
		return pagesFile;
	}

	/**
	 * @param pagesFile the pagesFile to set
	 */
	public void setPagesFile(String pagesFile) {
		this.pagesFile = pagesFile;
	}

	/**
	 * @return the folderPath
	 */
	public String getFolderPath() {
		return folderPath;
	}

	/**
	 * @param folderPath the folderPath to set
	 */
	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

}
