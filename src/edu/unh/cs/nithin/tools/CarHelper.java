/**
 * 
 */
package edu.unh.cs.nithin.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.unh.cs.treccar_v2.Data.Page;
import edu.unh.cs.treccar_v2.Data.Section;
import edu.unh.cs.treccar_v2.Data.Page.SectionPathParagraphs;

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
