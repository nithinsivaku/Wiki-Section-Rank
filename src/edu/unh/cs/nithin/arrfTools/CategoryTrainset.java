package edu.unh.cs.nithin.arrfTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.Data.Section;
import edu.unh.cs.treccar_v2.Data.Page.SectionPathParagraphs;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

public class CategoryTrainset {

	public CategoryTrainset(String trainSetFile, String outputPath) throws FileNotFoundException {
		Map<String, String> m = getParaHeading(trainSetFile);
		
	}
	
	
	/*
	 * Find the instances of the keywords in the train set file if present add that
	 * page to training list page
	 */
	public Map<String, String> getParaHeading(String trainSetFilePath) throws FileNotFoundException {

		List<Data.Page> pageList = new ArrayList<Data.Page>();
		FileInputStream fileInputStream = new FileInputStream(new File(trainSetFilePath));
		String Heading = "";
		Map<String, String> mapParaHeading = new HashMap<>();

		int pageCount = 0;
		// loop through wikipedia page in its order
		
		Map<String, ArrayList<Data.Page>> categoryPages = new HashMap<>();
		ArrayList<Data.Page> pagesInCategory;
		for (Data.Page page : DeserializeData.iterableAnnotations(fileInputStream)) {
			
			pagesInCategory = new ArrayList<>();
			
			
			
			pageCount++;
			String pageHeading = page.getPageId();
			Heading = pageHeading; // Heading will be page heading at the start of the page

			System.out.println(page.getPageMetadata().getCategoryIds());
			
			for( String categoryId : page.getPageMetadata().getCategoryIds() )
			{
				pagesInCategory.add(page);
				categoryPages.put(categoryId, pagesInCategory );
			}
			
			if(pageCount == 1546204)
			{
				System.out.println("breaking here");
				break;
			}


			
		}
		return mapParaHeading;

	}
	
}
