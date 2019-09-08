/**
 * @Author: Nithin
 * @Date:   2019-03-17T17:15:55-04:00
 * @Last modified by:   Nithin
 * @Last modified time: 2019-09-01T16:22:02-04:00
 */
package edu.unh.cs.nithin.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.lucene.queryparser.classic.ParseException;
import org.netlib.util.booleanW;

import edu.unh.cs.nithin.arrfTools.PageWiseTrainSet;
import edu.unh.cs.nithin.arrfTools.TrainSet;
import edu.unh.cs.nithin.classifier.CategoryClassifier;
import edu.unh.cs.nithin.classifier.RandomForestClassifier;
import edu.unh.cs.nithin.customParas.CustomParaGenerator;
import edu.unh.cs.nithin.customParas.CustomTrainSetGenerator;
import edu.unh.cs.nithin.re_rank.ClassifierReRank;
import edu.unh.cs.nithin.retrieval_model.BM25;
import edu.unh.cs.nithin.tools.Indexer;
import edu.unh.cs.nithin.tools.QrelsGenerator;
import edu.unh.cs.treccar_v2.Data.Page;

public class Main {

	public static void main(String[] args) throws Exception {
		System.setProperty("file.encoding", "UTF-8");
		String choice = args[0];
		switch(choice) {
			case "retrieval":
				retrieval(args[1], args[2], args[3]);
				break;
			case "wikikreator":
				wikikreator(args[1], args[2]);
				break;
			case "train":
				train(args[1], args[2]);
				break;
			case "custom-retrieval":
				customRetrieval(args[1], args[2], args[3]);
				break;
			case "custom-train":
				customTrain(args[1], args[2], args[3]);
				break;
			case "build-classifer-model":
				buildClassifierModel(args[1], args[2]);
				break;
			case "build-category-classifier":
				buildCategoryClassifier(args[1], args[2]);
				break;
			case "train-pages":
				trainPages(args[1], args[2], args[3]);
				break;
			case "classify-runfile":
				classifyRunFile(args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
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
	 * [retrieval execute BM25 retrieval model and generate a run file
	 *  for given query corpus and paragraph index corpus.
	 *  All the String paramters are file paths]
	 * @param pagesFile  [queries]
	 * @param indexPath  [paragraph index]
	 * @param outputPath [run file]
	 * @throws IOException
	 */
	private static void retrieval(String pagesFile, String indexPath, String outputPath) throws IOException {
		System.out.println(" Starting retrieval");
		String directoryName = outputPath;
		File directory = new File(directoryName);
		if (!directory.exists())
			directory.mkdirs();
		outputPath = directory.getPath();
		BM25 bm25 = new BM25(pagesFile, indexPath, outputPath);
		System.out.println(" Retrieval over");
	}

	/**
	 * [train create weka format trainset for given training dataset]
	 * @param paraFile       [unprocessedAllButBenchmark file]
	 * @param arrfOutputPath [outputh .arrf file]
	 * @throws ParseException
	 * @throws IOException
	 */
	private static void train(String paraFile, String arrfOutputPath) throws IOException, ParseException {
		TrainSet ts = new TrainSet(paraFile, arrfOutputPath);
		System.out.println(" Training Set Created ");
	}

	/**
	 * [customRetrieval execute BM25 retrieval model for particular set of queries
	 *  and generate a run file for given query corpus and paragraph index corpus.
	 *  All the String paramters are file paths]
	 * @param pagesFile  [queries]
	 * @param indexPath  [paragraph index]
	 * @param outputPath [run file]
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	private static void customRetrieval(String pagesFile, String indexPath, String outputPath) throws NumberFormatException, IOException {
		System.out.println(" Starting custom retrieval");
		String directoryName = outputPath;
		File directory = new File(directoryName);
		if (!directory.exists())
			directory.mkdirs();
		outputPath = directory.getPath();
		String totalNumberOfParas = "5";
		CustomParaGenerator cpg = new CustomParaGenerator(pagesFile, indexPath, outputPath,
				Integer.parseInt(totalNumberOfParas));
		System.out.println(" Custom - Retrieval over");
	}

	/**
	 * [customTrain create weka format trainset for selected number of data
	 * 	in a given training dataset]
	 * @param paraFile       [unprocessedAllButBenchmark file]
	 * @param indexPath      [indexpath to look up paraid for para]
	 * @param arrfOutputPath [ouptut .arrf file]
	 * @throws ParseException
	 * @throws IOException
	 */
	private static void customTrain(String paraFile, String indexPath, String arrfOutputPath) throws IOException, ParseException {
		System.out.println(" Starting custom training Set Creation ");
		CharSequence[] cs = { "Antibiotics", "Antimicrobial%20resistance", "Antioxidant", "Desertification",
				"Deforestation" };
		CustomTrainSetGenerator ctsg = new CustomTrainSetGenerator(paraFile, arrfOutputPath, indexPath, cs);
		System.out.println(" Training Set Created ");
	}

	/**
	 * [buildClassifierModel Train a weka format classifier model
	 * given training set and model output path]
	 * @param arffFile  [weka format training set]
	 * @param modelPath [output path .modelfile]
	 * @throws Exception
	 */
	private static void buildClassifierModel(String arffFile, String modelPath) throws Exception {
		File f = new File(arffFile);
		String arffFileName = f.getName().toString().replaceFirst("[.][^.]+$", "");
		System.out.println(" Building Random Forest Classifier Model");
		RandomForestClassifier rfc = new RandomForestClassifier(arffFile, modelPath, arffFileName);
		System.out.println("Random Forest Classifier model built at " + modelPath + " ");
	}

	/**
	 * [buildCategoryClassifier Train a weka format classifier model for all the categories]
	 * @param arrfFolderPath  [empty folder path to store all training files]
	 * @param modelFolderPath [empty folder path to store all model files]
	 * @throws Exception
	 */
	private static void buildCategoryClassifier(String arrfFolderPath, String modelFolderPath) throws Exception {
		CategoryClassifier cc = new CategoryClassifier(arrfFolderPath, modelFolderPath);
	}

	/**
	 * [trainPages Train a weka format classifier for all the pages in trainig corpus]
	 * @param trainingSetPath [empty folder path to store all training files]
	 * @param paraFilePath    [unprocessedAllButBenchmark file]
	 * @param modelPath       [empty folder path to store all model files]
	 * @throws Exception
	 */
	private static void trainPages(String trainingSetPath, String paraFilePath, String modelPath) throws Exception {
		PageWiseTrainSet pwt = new PageWiseTrainSet(trainingSetPath, paraFilePath);

		// build the classifier model for all the pages headings
		System.out.println(" Building Random Forest Classifier Model");
		File[] files = new File(trainingSetPath).listFiles();
		for(File file : files) {
			String arffFileName = file.getName().toString().replaceFirst("[.][^.]+$", "").replaceAll("[\\s\\:]","_");
			String arffFile = file.getAbsolutePath();
			System.out.println(arffFile + " " + arffFileName);
			RandomForestClassifier rfc = new RandomForestClassifier(arffFile, modelPath, arffFileName);
			System.out.println("Random Forest Classifier model built at " + modelPath + arffFileName + ".model ");
		}
	}

	/**
	 * [classifyRunFile Main prediction funtion.
	 * predict headings for each paragraph in the runfile from bm25]
	 * @param runFile                     [runfile from bm25]
	 * @param randomforestClassifierModel [rfmodel path]
	 * @param naiveBayesModel             [nbmodel path]
	 * @param trainDataArff               [trainingdata path]
	 * @param indexPath                   [indexpath to lookup para-id]
	 * @param outputPath                  [final output path]
	 * @param predConfidence			  [prediction Confidence]
	 * @throws Exception
	 * @throws NumberFormatException
	 */
	private static void classifyRunFile(String runFile, String randomforestClassifierModel, String naiveBayesModel, String trainDataArff, String indexPath, String outputPath, String predConfidence) throws NumberFormatException, Exception {
		ClassifierReRank cRR = new ClassifierReRank(runFile, randomforestClassifierModel, naiveBayesModel,
				trainDataArff, indexPath, outputPath, Float.parseFloat(predConfidence));
	}

	/**
	 * [index index all the paragraphs in paracorpus]
	 * @throws IOException
	 */
	private static void index() throws IOException {
		Indexer indexer = new Indexer();
	}

	/**
	 * @param trainingCorpus
	 * @param outputPath
	 * @throws IOException 
	 */
	private static void wikikreator(String trainingCorpus, String outputPath) throws IOException {
		String[] categoryNames = new String[] {"Category:Radiometry", "Category:American mathematicians", "Category:Diseases and disorders", "Category:Living_people"};
		QrelsGenerator qg = new QrelsGenerator(trainingCorpus, outputPath, categoryNames);
		Map<String, List<Page>> categoryPages = qg.getCategoriesPages();
		qg.generateQrels(categoryPages); 
//		Boolean includeSectionPath = false;
//		qg.createTrainSet(categoryPages, includeSectionPath);
//		includeSectionPath = true;
//		qg.createTrainSet(categoryPages, includeSectionPath);
	}
}
