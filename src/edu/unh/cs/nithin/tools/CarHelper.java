/**
 * 
 */
package edu.unh.cs.nithin.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import com.opencsv.CSVWriter;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.Data.Page;
import edu.unh.cs.treccar_v2.Data.Section;
import edu.unh.cs.treccar_v2.Data.Page.SectionPathParagraphs;
import edu.unh.cs.treccar_v2.Data.PageMetadata;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

/**
 * @author Nithin Sivakumar Modified Date : Sep 9, 2019 9:57:03 PM
 */

public class CarHelper {
	
	// class constants
	private Set<String> uniqueHeadings;
	private List<Page> pages;
	
	/**
	 * set input pages to be processed
	 * @param pageNames
	 */
	public CarHelper(List<Page> pageNames) {
		setPages(pageNames);
	}
	
	
	/**
	 * 
	 */
	public CarHelper() {
		// TODO Auto-generated constructor stub
	}

	public Map<String, Integer> findCategoryCount(String pagesFile) throws FileNotFoundException {
		Map<String, Integer> categoryCount = new HashMap<>();
		final FileInputStream fileInputStream = new FileInputStream(new File(pagesFile));
		for (Data.Page page : DeserializeData.iterableAnnotations(fileInputStream)) {
			PageMetadata pageMetaData = page.getPageMetadata();
			ArrayList<String> categories = pageMetaData.getCategoryNames();
			for(String category : categories) {
				System.out.println(category);
				if(categoryCount.get(category) != null) {
					int count = categoryCount.get(category);
					categoryCount.put(category, count+1);
				} else {
					categoryCount.put(category, 1);
				}
			}
		}
		return categoryCount;
	}
	
	/**
	 * @param categoryCount
	 * @param outputPath
	 * @throws IOException 
	 */
	public void writeToCsv(Map<String, Integer> categoryCount, String outputPath) throws IOException {
		outputPath = outputPath+"/CategoryCount.csv";
		System.out.println("Writing category count to csv file");
	    File file = new File(outputPath);
	    FileWriter outputfile = new FileWriter(file);
	    CSVWriter writer = new CSVWriter(outputfile);
	    String[] header = { "Category Name", "Count"};
	    writer.writeNext(header);
		for(Entry<String, Integer> entry : categoryCount.entrySet()) {
			String categoryName = entry.getKey();
			Integer count = entry.getValue();
			String[] data = new String[2];
			data[0] = categoryName;
			data[1] = count.toString();
			writer.writeNext(data);
		}
		writer.close();
		System.out.println("check for file at " + outputPath);
		
	}
		
	/**
	 * Loop through all pages set in the constructor. 
	 * For each page add the heading name and paragraph to a Hashmap.
	 * @return the headingParaMap
	 */
	public Map<String, String> getHeadingPara() {
		Map<String, String> headingPara = new HashMap<String, String>();
		Set<String> headingsSet = new HashSet<>();
		List<Page> pageNames = getPages();
		String Heading = "";
		for (Page page : pageNames) {
			String pageHeading = page.getPageId();
			Heading = pageHeading; // Heading will be page heading at the start of the page
			for (SectionPathParagraphs sectionPathParagraphs : page.flatSectionPathsParagraphs()) {
				Iterator<Section> sectionPathIter = sectionPathParagraphs.getSectionPath().iterator();

				// check for subheading
				while (sectionPathIter.hasNext()) {
					Section section = sectionPathIter.next();
					Heading = section.getHeadingId();
					if (sectionPathIter.hasNext()) {
						Section nextSection = sectionPathIter.next();
						Heading = nextSection.getHeadingId();
					}
				}
				String para = sectionPathParagraphs.getParagraph().getTextOnly();
				headingsSet.add(Heading);
				if (headingPara.get(Heading) == null) {
					headingPara.put(Heading, para);
				} else {
					para = headingPara.get(Heading) + " " + para;
					headingPara.put(Heading, para);
				}
			}
		}
		setUniqueHeadings(headingsSet);
		return headingPara;
	}
	
	/**
	 * @return the uniqueHeadings
	 */
	public Set<String> getUniqueHeadings() {
		return uniqueHeadings;
	}

	/**
	 * @param uniqueHeadings the uniqueHeadings to set
	 */
	public void setUniqueHeadings(Set<String> uniqueHeadings) {
		this.uniqueHeadings = uniqueHeadings;
	}


	/**
	 * @return the pages
	 */
	public List<Page> getPages() {
		return pages;
	}

	/**
	 * @param pages the pages to set
	 */
	public void setPages(List<Page> pages) {
		this.pages = pages;
	}

}
