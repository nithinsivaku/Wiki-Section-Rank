package edu.unh.cs.nithin.main;

import java.io.File;


import java.io.FileInputStream;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;

import edu.unh.cs.nithin.classifier.NaiveBayesClassifier;
import edu.unh.cs.nithin.classifier.RandomForestClassifier;
import edu.unh.cs.nithin.customParas.CustomParaGenerator;
import edu.unh.cs.nithin.customParas.CustomTrainSetGenerator;
import edu.unh.cs.nithin.re_rank.ClassifierReRank;
import edu.unh.cs.nithin.retrieval_model.BM25;

import java.time.Clock;

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

			int maxParaCount = 500;

			//TrainSet ts = new TrainSet(paraFile, arrfOutputPath, maxParaCount);

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

			CharSequence[] cs = { "Antibiotics", "Antimicrobial resistance", "Antioxidant", "Desertification", "Deforestation" };
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

			System.out.println(" Building Naive Bayes Classifier Model");
			NaiveBayesClassifier nbc = new NaiveBayesClassifier(arffFile, modelPath, arffFileName);
			System.out.println("NaiveBayes Classifier model built at " + modelPath + " ");

		}
		
		else if(mode.equals("classify-runfile")) {
			String runFile = args[1];
			String randomforestClassifierModel = args[2];
			String naiveBayesModel = args[3];
			String outputPath = args[4];
			
			ClassifierReRank cRR = new ClassifierReRank(runFile, randomforestClassifierModel, naiveBayesModel, outputPath);
		}
		else {
			System.out.println("mode is not given ");
		}

	}

}
