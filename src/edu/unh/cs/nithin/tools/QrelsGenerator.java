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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.Data.Page;
import edu.unh.cs.treccar_v2.Data.PageMetadata;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

public class QrelsGenerator {

	private String paraFilePath;
	private String outputFilePath;
	private String[] categoryNames;
	private Map<String, List<Data.Page>> categoryPages;

	/**
	 *
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


	public Map<String, List<Page>> getCategoriesPages() throws FileNotFoundException {
		String[] categoryNames = getCategoryNames();
		Map<String, List<Page>> categoriesPages = getCategoryPages();
		FileInputStream fStream = new FileInputStream(new File(getParaFilePath()));
		for(Data.Page page : DeserializeData.iterableAnnotations(fStream)) {
			System.out.println(page.getPageName());
			PageMetadata pageMetaData = page.getPageMetadata();
			for (String categoryName : pageMetaData.getCategoryNames()) {
				for(String matchingCategoryName : categoryNames) {
					if(categoryName.equals(matchingCategoryName)) {
						putObject(categoryName, page);
					}
				}
				System.out.println(categoryName);
			}
		}
		return categoriesPages;
	}

	/**
	 *
	 * @param key categoryName where pages get added to
	 * @param value page which will be added to a category
	 */
	private void putObject(String key, Data.Page value) {
		Map<String, List<Page>> categoryPageMap = getCategoryPages();
		List<Page> pagesList = categoryPageMap.get(key);
		if(pagesList != null) {
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
	 */
	public void generateQrels(Map<String, List<Page>> catPages) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * @param categoryPages2
	 * @param includeSectionPath
	 */
	public void createTrainSet(Map<String, List<Page>> categoryPages2, Boolean includeSectionPath) {
		// TODO Auto-generated method stub
		
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
