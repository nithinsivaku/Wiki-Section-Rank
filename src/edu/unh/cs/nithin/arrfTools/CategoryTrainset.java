package edu.unh.cs.nithin.arrfTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.Data.Section;
import edu.unh.cs.treccar_v2.Data.Page;
import edu.unh.cs.treccar_v2.Data.Page.SectionPathParagraphs;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

public class CategoryTrainset {

	public CategoryTrainset(String trainSetFile, String outputPath) throws FileNotFoundException {
		Map<String, ArrayList<Page>> m = getCategoryPageMap(trainSetFile);

	}

	/*
	 * Loop through all pages. Find list of categories associated with each page.
	 * For each category add the corresponding page name in Hashmap.
	 * Add list of pages to each categories the page falls under.
	 * Return Hashmap
	 */
	public Map<String, ArrayList<Data.Page>> getCategoryPageMap(String trainSetFilePath) throws FileNotFoundException {

		FileInputStream fileInputStream = new FileInputStream(new File(trainSetFilePath));

		Map<String, ArrayList<Data.Page>> categoryPagesMap = new HashMap<>();

		ArrayList<Data.Page> pageList;

		for (Data.Page page : DeserializeData.iterableAnnotations(fileInputStream)) {

			ArrayList<String> catList = page.getPageMetadata().getCategoryIds();

			for (String categoryId : catList) {
				pageList = categoryPagesMap.get(categoryId);

				if (pageList == null) {
					ArrayList<Data.Page> newPageList = new ArrayList<Data.Page>();
					newPageList.add(page);
					categoryPagesMap.put(categoryId, newPageList);
				} else {
					pageList.add(page);
				}

			}

		}
		return categoryPagesMap;

	}

}
