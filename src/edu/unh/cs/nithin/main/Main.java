/**
 * @Author: Nithin
 * @Date:   2019-03-17T17:15:55-04:00
 * @Last modified by:   Nithin
 * @Last modified time: 2019-08-17T18:41:17-04:00
 */
package edu.unh.cs.nithin.main;

import java.io.File;

import java.util.Arrays;

import edu.unh.cs.nithin.arrfTools.PageWiseTrainSet;
import edu.unh.cs.nithin.arrfTools.TrainSet;
import edu.unh.cs.nithin.classifier.CategoryClassifier;
import edu.unh.cs.nithin.classifier.RandomForestClassifier;
import edu.unh.cs.nithin.customParas.CustomParaGenerator;
import edu.unh.cs.nithin.customParas.CustomTrainSetGenerator;
import edu.unh.cs.nithin.re_rank.ClassifierReRank;
import edu.unh.cs.nithin.retrieval_model.BM25;
import edu.unh.cs.nithin.tools.Indexer;


public class Main {

	public static void main(String[] args) throws Exception {

		System.setProperty("file.encoding", "UTF-8");

		String mode = args[0];

		if (mode.equals("retrieval")) {
			String pagesFile = args[1];
			String indexPath = args[2];
			String outputPath = args[3];

			System.out.println(" Starting" + mode);

			String directoryName = outputPath;
			File directory = new File(directoryName);
			if (!directory.exists())
				directory.mkdirs();

			outputPath = directory.getPath();

			BM25 bm25 = new BM25(pagesFile, indexPath, outputPath);
			System.out.println(" Retrieval over");
		} else if (mode.equals("train")) {
			String paraFile = args[1];
			String arrfOutputPath = args[2];

			TrainSet ts = new TrainSet(paraFile, arrfOutputPath);

			System.out.println(" Training Set Created ");

		} else if (mode.equals("custom-retrieval")) {
			String pagesFile = args[1];
			String indexPath = args[2];
			String outputPath = args[3];

			System.out.println(" Starting" + mode);

			String directoryName = outputPath;
			File directory = new File(directoryName);
			if (!directory.exists())
				directory.mkdirs();

			outputPath = directory.getPath();

			String totalNumberOfParas = args[4];
			CustomParaGenerator cpg = new CustomParaGenerator(pagesFile, indexPath, outputPath,
					Integer.parseInt(totalNumberOfParas));
			System.out.println(" Custom - Retrieval over");

		} else if (mode.equals("custom-train")) {
			String paraFile = args[1];
			String indexPAth = args[2];
			String arrfOutputPath = args[3];

			// String[] arr = {"Carbohydrate", "Chocolate", "Cholera", "Ethics", "Flavor"};

			CharSequence[] cs = { "Antibiotics", "Antimicrobial%20resistance", "Antioxidant", "Desertification",
					"Deforestation" };
			CustomTrainSetGenerator ctsg = new CustomTrainSetGenerator(paraFile, arrfOutputPath, indexPAth, cs);

			System.out.println(" Training Set Created ");

		} else if (mode.equals("build-classifer-model")) {
			String arffFile = args[1];
			String modelPath = args[2];

			File f = new File(arffFile);
			String arffFileName = f.getName().toString().replaceFirst("[.][^.]+$", "");

			System.out.println(" Building Random Forest Classifier Model");
			RandomForestClassifier rfc = new RandomForestClassifier(arffFile, modelPath, arffFileName);
			System.out.println("Random Forest Classifier model built at " + modelPath + " ");

			int[] arr = {1,2,3,4,5};

		}

		else if(mode.equals("build-category-classifier"))
		{
			String arrfFolderPath = args[1];
			String modelFolderPath = args[2];
			CategoryClassifier cc = new CategoryClassifier(arrfFolderPath, modelFolderPath);
		} else if(mode.equals("train-pages")) {
			String trainingSetPath = args[1];
			String paraFilePath = args[2];
			String modelPath = args[3];
			PageWiseTrainSet pwt = new PageWiseTrainSet(trainingSetPath, paraFilePath);
			
			// build the classifier model for all the pages headings
			System.out.println(" Building Random Forest Classifier Model");
			File[] files = new File(trainingSetPath).listFiles();
			for(File file : files) {
				String arffFileName = file.getName().toString().replaceFirst("[.][^.]+$", "");
				String arffFile = file.getAbsolutePath();
				System.out.println(arffFile + " " + arffFileName);
				RandomForestClassifier rfc = new RandomForestClassifier(arffFile, modelPath, arffFileName);
				System.out.println("Random Forest Classifier model built at " + modelPath + arffFileName + ".model ");
			}
		} 

		else if (mode.equals("classify-runfile")) {
			String runFile = args[1];
			String randomforestClassifierModel = args[2];
			String naiveBayesModel = args[3];
			String trainDataArff = args[4];
			String indexPath = args[5];
			String outputPath = args[6];
			String predConfidence = args[7];

			ClassifierReRank cRR = new ClassifierReRank(runFile, randomforestClassifierModel, naiveBayesModel,
					trainDataArff, indexPath, outputPath, Float.parseFloat(predConfidence));
		} else if (mode.equals("Index")) {
			Indexer indexer = new Indexer();
		} else {
			System.out.println("mode is not given ");
		}

	}
}
