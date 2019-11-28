/**
 * @Author: Nithin
 * @Date:   2019-09-01T12:16:54-04:00
 * @Last modified by:   Nithin
 * @Last modified time: 2019-09-05T19:18:40-04:00
 */

package edu.unh.cs.nithin.tools;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.Data.Page;
import edu.unh.cs.treccar_v2.Data.Page.SectionPathParagraphs;
import edu.unh.cs.treccar_v2.Data.PageMetadata;
import edu.unh.cs.treccar_v2.Data.Section;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

public class QrelsGenerator {

	private String paraFilePath;
	private String outputFilePath;
	private String[] categoryNames;
	private Map<String, List<Data.Page>> categoryPages;

	/**
	 * Constructor responsible for setting file output paths and given category names
	 * @param paraFile
	 * @param outputFile
	 * @param categoryNames
	 * @throws FileNotFoundException
	 */
	public QrelsGenerator(String paraFile, String outputFile, String[] categoryNames) throws FileNotFoundException {
		setParaFilePath(paraFile);
		setOutputFilePath(outputFile);
		setCategoryNames(categoryNames);
		setCategoryPages(new HashMap<String, List<Data.Page>>());
	}
	
	/**
	 * Return a map of list of pages associated for provided category names
	 * @return categoriesPages
	 * @throws FileNotFoundException
	 */
	public Map<String, List<Page>> getCategoriesPages() throws FileNotFoundException {
		String[] categoryNames = getCategoryNames();
		Map<String, List<Page>> categoriesPages = getCategoryPages();
		FileInputStream fStream = new FileInputStream(new File(getParaFilePath()));
		int count = 0;
		for (Data.Page page : DeserializeData.iterableAnnotations(fStream)) {
			if(count == 1) break;
			System.out.println(page.getPageName());
			PageMetadata pageMetaData = page.getPageMetadata();
			for (String categoryName : pageMetaData.getCategoryNames()) {
				for (String matchingCategoryName : categoryNames) {
					if (categoryName.equals(matchingCategoryName)) {
						putObject(categoryName, page);
						count++;
					}
				}
				System.out.println(categoryName);
			}
		}
		return categoriesPages;
	}

	/**
	 * Helper function to store list of pages that falls under same category
	 * @param key   categoryName where pages get added to
	 * @param value page which will be added to a category
	 */
	private void putObject(String key, Data.Page value) {
		Map<String, List<Page>> categoryPageMap = getCategoryPages();
		List<Page> pagesList = categoryPageMap.get(key);
		if (pagesList != null) {
			pagesList.add(value);
			categoryPageMap.put(key, pagesList);
		} else {
			List<Page> newPagesList = new ArrayList<Data.Page>();
			newPagesList.add(value);
			categoryPageMap.put(key, newPagesList);
		}
	}

	/**
	 * @param catPages
	 * @throws IOException
	 */
	public void generateQrels(Map<String, List<Page>> catPages) throws IOException {
		for (Entry<String, List<Page>> entrySet : catPages.entrySet()) {
			String catName = entrySet.getKey();
			List<Page> pages = entrySet.getValue();
			createQrelFile(catName, pages);
		}

	}

	/**
	 * Creates qrel file for given category and list of pages
	 * 
	 * @param catName
	 * @param pages
	 * @param includeHeadingPath
	 * @throws IOException
	 */
	private void createQrelFile(String catName, List<Page> pages) throws IOException {
		String outputPath = getOutputFilePath() + "/cat_qrels";
		String qrelFileName = catName.replaceAll("[^A-Za-z0-9]", "_") + ".qrels";
		File qrelfile = new File(outputPath + "/" + qrelFileName);
		qrelfile.createNewFile();
		FileWriter writer = new FileWriter(qrelfile);
		for (Page page : pages) {
			String Heading = page.getPageId();
			for (SectionPathParagraphs sectionPathParagraphs : page.flatSectionPathsParagraphs()) {
				Iterator<Section> sectionPathIter = sectionPathParagraphs.getSectionPath().iterator();
				while (sectionPathIter.hasNext()) {
					sectionPathIter.next();
					Heading = Data.sectionPathId(page.getPageId(), sectionPathParagraphs.getSectionPath());
					System.out.println(Heading);
					if (sectionPathIter.hasNext()) {
						sectionPathIter.next();
					}
				}
				String paraId = sectionPathParagraphs.getParagraph().getParaId();
				writer.write(Heading + " 0 " + paraId + " 1\n");
			}
		}
		writer.flush();
		writer.close();
		System.out.println(qrelFileName + " is at " + qrelfile.getAbsolutePath());
	}

	/**
	 * @return the paraFilePath
	 */
	public String getParaFilePath() {
		return paraFilePath;
	}

	/**
	 * @param paraFilePath the paraFilePath to set
	 */
	public void setParaFilePath(String paraFilePath) {
		this.paraFilePath = paraFilePath;
	}

	/**
	 * @return the outputFilePath
	 */
	public String getOutputFilePath() {
		return outputFilePath;
	}

	/**
	 * @param outputFilePath the outputFilePath to set
	 */
	public void setOutputFilePath(String outputFilePath) {
		this.outputFilePath = outputFilePath;
	}

	/**
	 * @return the categoryPages
	 */
	public Map<String, List<Data.Page>> getCategoryPages() {
		return categoryPages;
	}

	/**
	 * @param categoryPages the categoryPages to set
	 */
	public void setCategoryPages(Map<String, List<Data.Page>> categoryPages) {
		this.categoryPages = categoryPages;
	}

	/**
	 * @return the categoryNames
	 */
	public String[] getCategoryNames() {
		return categoryNames;
	}

	/**
	 * @param categoryNames the categoryNames to set
	 */
	public void setCategoryNames(String[] categoryNames) {
		this.categoryNames = categoryNames;
	}
}
