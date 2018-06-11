package edu.unh.cs.nithin.main;

import java.io.File;

import java.io.FileInputStream;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;

import edu.unh.cs.nithin.retrieval_model.BM25;

import java.time.Clock;

public class Main {

	private static void usage() {
		System.out.println("Command line parameters:Method_Signal outputDirectory");

		System.exit(-1);
	}

	public static void main(String[] args) throws Exception {

		System.setProperty("file.encoding", "UTF-8");

		System.out.println("Main Works");

		String pagesFile = args[0];
		String indexPath = args[1];
		String outputPath = args[2];
		String directoryName = outputPath;
		File directory = new File(directoryName);
		if (!directory.exists())
			directory.mkdirs();

		outputPath = directory.getPath();

		BM25 bm25 = new BM25(pagesFile, indexPath, outputPath);

	}

}
