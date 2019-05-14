package edu.unh.cs.nithin.classifier;

import java.io.File;
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
	
	public CategoryClassifier(String arrfFolderPath, String modelFolderPath) throws Exception {
		
		buildModel(arrfFolderPath, modelFolderPath);
	}

	private void buildModel(String arrffolderPath, String modelFolderPath) throws Exception {
		// TODO Auto-generated method stub
		
		RandomForestClassifier rfc = new RandomForestClassifier();
		
		File[] files = new File(arrffolderPath).listFiles();
		
		for(File file : files)
		{
			if(file.isFile())
			{
				rfc.buildRandomForestModel(file.getAbsolutePath(), modelFolderPath, file.getName());
			}
		}
		
		System.out.println("Model files created for arrf files under "+arrffolderPath);
	}
}
