/**
 * 
 */
package edu.unh.cs.nithin.arrfTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.Data.Page;
import edu.unh.cs.treccar_v2.Data.Page.SectionPathParagraphs;
import edu.unh.cs.treccar_v2.Data.Section;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

/**
 * @author Nithin Sivakumar
 * Modified Date : Aug 19, 2019 9:56:03 PM
 */
public class PageWiseTrainSet {

	private List<String> blackListedHeadings;
	/**
	 * @param rootPath
	 * @param paraFilePath
	 * @throws IOException 
	 */
	public PageWiseTrainSet(String trainingSetPath, String paraFilePath) throws IOException {
		blackListedHeadings = new ArrayList<>(Arrays.asList("External links", "Further reading", "References", "See also", "Categories"));
		processAllPages(trainingSetPath, paraFilePath);
	}

	/**
	 * @param rootPath
	 * @param paraFilePath
	 * @throws IOException 
	 */
	private void processAllPages(String trainingSetPath, String paraFilePath) throws IOException {
		FileInputStream fStream = new FileInputStream(new File(paraFilePath));
		List<String> headingIds;
		for(Data.Page page : DeserializeData.iterableAnnotations(fStream)) {
			String pageName = page.getPageName().replaceAll("[\\s\\:]","_");
			if(pageName.contains("/")) continue;
			System.out.println(pageName);
			headingIds = new ArrayList<>();
			Map<String, String> headingPara = getHeadingParaMap(page);
			
			// add all the headingIds under single page
			for(String headingId : headingPara.keySet()) {
				headingIds.add(headingId);
			}
			
			// make the training set for this page
			if(headingIds.size() > 1) { // as of now weka cant handle unary class labels
				TrainSet ts = new TrainSet(pageName, headingIds, headingPara, trainingSetPath);
			}	
		}
	}

	/**
	 * @param page
	 * @return
	 */
	private Map<String, String> getHeadingParaMap(Page page) {
		String Heading = page.getPageId();
		Map<String, String> headingPara = new HashMap<String, String>();
		for(SectionPathParagraphs sectionPathParagraphs : page.flatSectionPathsParagraphs()) { 
			Iterator<Section> sectionPathIter = sectionPathParagraphs.getSectionPath().iterator();
			while(sectionPathIter.hasNext()) {
				Section section = sectionPathIter.next();
				Heading = section.getHeadingId();
				if(sectionPathIter.hasNext()) {
					Section nextSection = sectionPathIter.next();
					Heading = nextSection.getHeadingId();
				}
			}
			if(blackListedHeadings.contains(Heading)) continue;
			String para = sectionPathParagraphs.getParagraph().getTextOnly();
			if(headingPara.get(Heading) == null) {
				headingPara.put(Heading, para);
			} else {
				para = headingPara.get(Heading) + " " + para;
				headingPara.put(Heading, para);
			}
		}
		return headingPara;
	}
		
}
