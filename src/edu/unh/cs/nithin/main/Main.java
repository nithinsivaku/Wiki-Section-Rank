/**
 * @Author: Nithin
 * @Date:   2019-03-17T17:15:55-04:00
 * @Last modified by:   Nithin
 * @Last modified time: 2019-09-01T16:22:02-04:00
 */
package edu.unh.cs.nithin.main;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.bounce.event.CardEvent;

import edu.unh.cs.nithin.arrfTools.TrainSet;
import edu.unh.cs.nithin.classifier.RandomForestClassifier;
import edu.unh.cs.nithin.re_rank.ClassifierReRank;
import edu.unh.cs.nithin.retrieval_model.BM25;
import edu.unh.cs.nithin.tools.CarHelper;
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
			case "build-classifer-model":
				buildClassifierModel(args[1], args[2]);
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
		String[] categoryNames = new String[] {"Category:Diseases and disorders"};
		BM25 bm25 = new BM25(pagesFile, indexPath, outputPath);
		for(String catName : categoryNames) {
			bm25.PageSearch(catName);
			bm25.SectionSearch(catName);
		}
		System.out.println(" Retrieval over");
	}

	/**
	 * [buildClassifierModel Train a weka format classifier model
	 * given training set and model output path]
	 * @param arffFile  [weka format training set]
	 * @param modelPath [output path .modelfile]
	 * @throws Exception
	 */
	private static void buildClassifierModel(String arffFiles, String modelPath) throws Exception {
		System.out.println(" Building Random Forest Classifier Model");
		RandomForestClassifier rfc = new RandomForestClassifier(arffFiles, modelPath);
		rfc.buildRandomForestModel();
		System.out.println("Random Forest Classifier model built at " + modelPath + " ");
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
		String pagesFile = "/home/ns1077/work/unprocessedAllButBenchmark.v2.1/unprocessedAllButBenchmark.Y2.cbor";
		CarHelper ch = new CarHelper();
		Map<String, Integer> categoryCount = ch.findCategoryCount(pagesFile);
		ch.writeToCsv(categoryCount, outputPath);
		
//		String[] categoryNames = new String[] {"Category:Radiometry", "Category:American mathematicians", "Category:Diseases and disorders", "Category:Living_people"};
//		QrelsGenerator qg = new QrelsGenerator(trainingCorpus, outputPath, categoryNames);
//		Map<String, List<Page>> categoryPages = qg.getCategoriesPages();
//		qg.generateQrels(categoryPages); 
//		TrainSet ts = new TrainSet(categoryPages, outputPath);
//		ts.createCategoryTrainSet();
	}
}
