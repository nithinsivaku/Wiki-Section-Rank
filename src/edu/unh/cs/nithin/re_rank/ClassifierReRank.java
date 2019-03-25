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

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.Data.PageSkeleton;
import edu.unh.cs.treccar_v2.Data.Section;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class ClassifierReRank {

	public ClassifierReRank(String runFile, String rfModel, String nbModel, String trainDataArff, String indexPath,
			String outputPath) throws Exception {
		classifyRunFileUsingRandomForestClassifier(runFile, rfModel, trainDataArff, indexPath, outputPath);
		//classifyRunFileUsingNaiveBayesClassifier(runFile, nbModel, trainDataArff, indexPath, outputPath);
	}

	/*
	 * 
	 */
	private void classifyRunFileUsingNaiveBayesClassifier(String runFile, String nbModel, String trainDataArff,
			String indexPath, String outputPath) throws Exception {

		System.out.println(" loading NaiveBayes Classifier");
		System.out.println("Model Loading.......................");
		Classifier cls_NB = (Classifier) weka.core.SerializationHelper.read(nbModel);
		System.out.println("NaiveBayes Classifier loaded successfully");
		System.out.println("*****************************************************************************\n");

		// Load the trainid data format
		DataSource source = new DataSource(trainDataArff);
		Instances trainingData = source.getDataSet();
		trainingData.setClassIndex(trainingData.numAttributes() - 1);

		File outRunfile = new File(outputPath + "NBClassified");
		outRunfile.createNewFile();
		FileWriter writer = new FileWriter(outRunfile);

		File file = new File(runFile);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		int i = 0;
		while (((st = br.readLine()) != null)) {

			String[] tokens = st.split(" ");
			// System.out.println(tokens[0] + " " + tokens[2]);
			String temp = tokens[0];
			String paraId = tokens[2];
			String temp1;

			String paragraph = getParagraphForId(indexPath, paraId);
			// System.out.println(paragraph);

			Instances testset = trainingData.stringFreeStructure();
			Instance insta = makeInstance(paragraph, testset);

			double predicted = cls_NB.classifyInstance(insta);

			double[] prediction = cls_NB.distributionForInstance(insta);
			// double res = classifier.classifyInstance(insta);

			double relevance;

			System.out.print(".");

			System.out.println(tokens[0]);
			System.out.println(paragraph);
			System.out.println(trainingData.classAttribute().value((int) predicted));

			System.out.println((int) predicted);
			System.out.println(prediction[(int) predicted]);
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			String queryId = tokens[0];
			int rank;
			if (prediction[(int) predicted] > 0.5) {
				System.out.println("above 0.5");
				if (tokens[0] == trainingData.classAttribute().value((int) predicted)) {
					queryId = tokens[0];
					rank = Integer.parseInt(tokens[3]);
				}
				else
				{
					queryId = trainingData.classAttribute().value((int) predicted);
					rank = 1;
				}
			}
			else
			{
				queryId = tokens[0];
				rank = Integer.parseInt(tokens[3]);
			}
			writer.write(queryId + " Q0 " + paraId + " " + rank + " " + tokens[4] + " Classifier-Laura\n");
			i++;
		}

		writer.flush();
		writer.close();

		System.out.println("Writen  classified results\nQuery Done!" + outRunfile.getName() );

	}

	/*
	 * 
	 */
	private void classifyRunFileUsingRandomForestClassifier(String runFile, String rfModel, String trainDataArff,
			String indexPath, String outputPath) throws Exception {

		System.out.println(" loading Random Forest Classifier");
		System.out.println("Model Loading.......................");
		Classifier cls_RF = (Classifier) weka.core.SerializationHelper.read(rfModel);
		System.out.println("Random Forest Classifier loaded successfully");
		System.out.println("*****************************************************************************\n");
		
		// Load the trainid data format
				DataSource source = new DataSource(trainDataArff);
				Instances trainingData = source.getDataSet();
				trainingData.setClassIndex(trainingData.numAttributes() - 1);

				File outRunfile = new File(outputPath + "RFClassified");
				outRunfile.createNewFile();
				FileWriter writer = new FileWriter(outRunfile);

				File file = new File(runFile);
				BufferedReader br = new BufferedReader(new FileReader(file));
				String st;
				int i = 0;
				while (((st = br.readLine()) != null)) {

					String[] tokens = st.split(" ");
					// System.out.println(tokens[0] + " " + tokens[2]);
					String temp = tokens[0];
					String paraId = tokens[2];
					String temp1;

					String paragraph = getParagraphForId(indexPath, paraId);
					// System.out.println(paragraph);

					Instances testset = trainingData.stringFreeStructure();
					Instance insta = makeInstance(paragraph, testset);

					double predicted = cls_RF.classifyInstance(insta);

					double[] prediction = cls_RF.distributionForInstance(insta);
					// double res = classifier.classifyInstance(insta);

					double relevance;

					System.out.print(".");

					System.out.println(tokens[0]);
					System.out.println(paragraph);
					System.out.println(trainingData.classAttribute().value((int) predicted) + " - " + prediction[(int) predicted]);
					String queryId = tokens[0];
					int rank;
					if (prediction[(int) predicted] > 0.5) {
						System.out.println("above 0.5");
						if (tokens[0] == trainingData.classAttribute().value((int) predicted)) {
							queryId = tokens[0];
							rank = Integer.parseInt(tokens[3]);
						}
						else
						{
							queryId = trainingData.classAttribute().value((int) predicted);
							rank = 1;
						}
					}
					else
					{
						queryId = tokens[0];
						rank = Integer.parseInt(tokens[3]);
					}
					writer.write(queryId + " Q0 " + paraId + " " + rank + " " + tokens[4] + " Classifier-Laura\n");
					System.out.println(queryId + " Q0 " + paraId + " " + rank + " " + tokens[4] + " Classifier-Laura\n");
					i++;
					break;
				}

				writer.flush();
				writer.close();

				System.out.println("Writen  classified results\nQuery Done!" + outRunfile.getName() );

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
		for (int i = 0; i < 0; i++) {
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

	// Author: Laura dietz
	private static String buildSectionQueryStr(Data.Page page, List<Data.Section> sectionPath) {
		StringBuilder queryStr = new StringBuilder();
		queryStr.append(page.getPageName());
		for (Data.Section section : sectionPath) {
			queryStr.append(" ").append(section.getHeading());
		}

		// System.out.println("queryStr = " + queryStr);
		return queryStr.toString();
	}

	// Author: Laura dietz, modified by Nithin for lowest heading in each
	// section
	private static String buildSectionQueryStr(List<Data.Section> sectionPath) {
		String queryStr = " ";
		List<PageSkeleton> child;

		for (Data.Section section : sectionPath) {

			child = section.getChildren();
			if (!(child.isEmpty())) {
				Section s = (Section) child.get(child.size() - 1);
				queryStr = s.getHeading();

			} else {
				queryStr = section.getHeading();
			}

		}
		return queryStr;
	}

}
