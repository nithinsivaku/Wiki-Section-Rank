package edu.unh.cs.nithin.classifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 
 * @author Nithin
 * Given a folder which contains arrfSets based on category
 * build a calssifier model for each category
 */
public class CategoryClassifier {
	
	public CategoryClassifier(String arrfFolderPath, String modelFolderPath) throws IOException {
		
		buildModel(arrfFolderPath, modelFolderPath);
	}

	private void buildModel(String arrffolderPath, String modelFolderPath) throws IOException {
		// TODO Auto-generated method stub
		Files.walk(Paths.get(arrffolderPath))
	     .filter(Files::isRegularFile)
	     .forEach(System.out::println);
		
	}
}
