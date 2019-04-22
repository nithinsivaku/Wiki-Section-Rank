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

	public CategoryTrainset() throws FileNotFoundException {
		
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

		int pageCount = 0;
		
		for (Data.Page page : DeserializeData.iterableAnnotations(fileInputStream)) {

			pageCount++;
			ArrayList<String> catList = page.getPageMetadata().getCategoryIds();

			for (String categoryId : catList) {
				pageList = categoryPagesMap.get(categoryId);

				if (pageList == null) {
					ArrayList<Data.Page> newPageList = new ArrayList<Data.Page>();
					newPageList.add(page);
					categoryPagesMap.put(categoryId, newPageList);
				} else {
					pageList.add(page);
					categoryPagesMap.put(categoryId, pageList);
				}

			}
			System.out.println(pageCount);
			if(pageCount == 50)
			{
				break;
			}
		}
		return categoryPagesMap;

	}
	
	/*
	 * Loop through all pages.
	 * For each page add the heading name and paragraph to Hashmap.
	 */
	public Map<String, String> getHeadingParaMap(ArrayList<Page> pageNames)
	{
		String Heading = "";
		Map<String, String> mapParaHeading = new HashMap<>();
		for(Page page : pageNames)
		{
			String pageHeading = page.getPageId();
			for (SectionPathParagraphs sectionPathParagraph : page.flatSectionPathsParagraphs()) {

				Iterator<Section> sectionPathIter = sectionPathParagraph.getSectionPath().iterator();

				// check for subheading
				while (sectionPathIter.hasNext()) {
					Section section = sectionPathIter.next();

					String sectionHeading = pageHeading + "/" + section.getHeadingId(); // fix the slash

					if (sectionPathIter.hasNext()) {
						Section nextSection = sectionPathIter.next();
						Heading = sectionHeading + "/" + nextSection.getHeadingId();
					} else {
						Heading = sectionHeading;
					}

				}

				String para = sectionPathParagraph.getParagraph().getTextOnly();
				if(mapParaHeading.get(Heading) == null)
				{
					mapParaHeading.put(Heading, para);
				}
				else
				{
					para += mapParaHeading.get(Heading);
					mapParaHeading.put(Heading, para);
				}
				

			}
		}
		return mapParaHeading;
		
	}

}
