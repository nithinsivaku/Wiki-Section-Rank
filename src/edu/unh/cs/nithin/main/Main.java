/**
 * @Author: Nithin
 * @Date:   2019-03-17T17:15:55-04:00
 * @Last modified by:   Nithin
 * @Last modified time: 2019-09-01T16:22:02-04:00
 */
package edu.unh.cs.nithin.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import edu.unh.cs.nithin.arrfTools.TrainSet;
import edu.unh.cs.nithin.classifier.ClassifierTrainer;
import edu.unh.cs.nithin.re_rank.ClassifierReRank;
import edu.unh.cs.nithin.re_rank.RetrievalClassifier;
import edu.unh.cs.nithin.retrieval_model.BM25;
import edu.unh.cs.nithin.tools.Indexer;
import edu.unh.cs.nithin.tools.QrelsGenerator;
import edu.unh.cs.treccar_v2.Data.Page;

public class Main {
	
	private static String pwd; 

	public static void main(String[] args) throws Exception {
		System.setProperty("file.encoding", "UTF-8");
		setPwd(Paths.get("").toAbsolutePath().toString());
		String choice = args[0];
		switch(choice) {
			case "retrieval":
				retrieval(args[1], args[2]);
				break;
			case "wikikreator":
				wikikreator(args[1], args[2]);
				break;
			case "build-classifer-model":
				buildClassifierModel(args[1]);
				break;
			case "classify-runfile":
				classifyRunFile(args[1], args[2], args[3]);
				break;
			case "Index":
				index();
				break;
			default:
				System.out.println("mode is not given ");
				break;
		}
	}

	/**
	 * [execute BM25 retrieval model and generate a run file
	 *  for given query corpus and paragraph index corpus.
	 *  All the String paramters are file paths]
	 * @param pagesFile  [queries]
	 * @param indexPath  [paragraph index]
	 * @param outputPath [run file]
	 * @throws Exception 
	 */
	private static void retrieval(String pagesFile, String indexPath) throws Exception {
		String outfiles = "/outFiles/runFiles/bm25";									// create outputfiles directory
		String pwd = getPwd();
		String outputPath = pwd+outfiles;
		File outfile = new File(outputPath);
		if(!outfile.exists()) {
			outfile.mkdirs();
		}
		System.out.println(" Starting retrieval");
		String[] categoryNames = new String[] {"Category:Habitat", "Category:Christmas food", "Category:Environmental terminology", "Category:Diseases and disorders"};
		BM25 bm25 = new BM25(pagesFile, indexPath, outputPath);
		for (String catName : categoryNames) {
			bm25.SectionSearch(catName);
		}
	}

	/**
	 * [Train a weka format classifier model for the trainsets created in wikikreator method]
	 * @param arffFile  [weka format training set]
	 * @param modelPath [output path .modelfile]
	 * @throws Exception
	 */
	private static void buildClassifierModel(String outputPath) throws Exception {
		System.out.println(" Building Classifier Model");
		ClassifierTrainer ct = new ClassifierTrainer(outputPath);
		ct.buildNaiveBayesModel();
		ct.buildRandomForestModel();
	}
	
	/***
	 * [predicts the paraheading for each line in given runfile]
	 * @param runFile filepath
	 * @param indexPath filepath
	 * @param outputPath filepath
	 * @throws Exception
	 */
	private static void classifyRunFile(String folderPath, String indexPath, String pagesFile) throws Exception {
		String[] categoryNames = new String[] {"Category:Habitat", "Category:Christmas food", "Category:Environmental terminology", "Category:Diseases and disorders"};
		String outfiles = "/outFiles/runFiles/classify";									// create outputfiles directory
		String pwd = getPwd();
		String outputPath = pwd+outfiles;
		File outfile = new File(outputPath);
		if(!outfile.exists()) {
			outfile.mkdirs();
		}
		
		float predConf = (float) 0.2;
		RetrievalClassifier rc = new RetrievalClassifier(indexPath, outputPath, predConf, pagesFile, folderPath);
		for(String catName : categoryNames) {
			rc.runBm25(catName);
			catName = catName.replaceAll("[^A-Za-z0-9]", "_");
			rc.classifyRunfiles(catName);
		}
		
//		runFile = "/Users/Nithin/Desktop/outputFilesIR/runFiles/ecologypercent20.txt";
//		ClassifierReRank crr = new ClassifierReRank(runFile, indexPath, outputPath);
//		crr.classifyRunFile(runFile);
//		crr.classifyRunFile(runFile, "Category_Environmental_terminology");
	}
	
	/**
	 * [index all the paragraphs in para-corpus]
	 * @throws IOException
	 */
	private static void index() throws IOException {
		Indexer indexer = new Indexer();
	}
	
	/**
	 * [Responsible for 
	 * 1. creating qrels for the category names provided
	 * 2. create weka accepted training set for the category names ]
	 * @param trainingCorpus
	 * @param outputPath
	 * @throws IOException
	 */
	private static void wikikreator(String trainingCorpus, String outputPath) throws IOException {
		String[] categoryNames = new String[] {"Category:Habitat", "Category:Christmas food", "Category:Environmental terminology", "Category:Diseases and disorders"};
		QrelsGenerator qg = new QrelsGenerator(trainingCorpus, outputPath, categoryNames);
		Map<String, List<Page>> categoryPages = qg.getCategoriesPages();
		qg.generateQrels(categoryPages); 
		TrainSet ts = new TrainSet(categoryPages, outputPath);
		ts.createCategoryTrainSet();
	}

	/**
	 * @return the pwd
	 */
	public static String getPwd() {
		return pwd;
	}

	/**
	 * @param pwd the pwd to set
	 */
	public static void setPwd(String pwd) {
		Main.pwd = pwd;
	}
}
