/**
 * @Author: Nithin Sivakumar <Nithin>
 * @Date:   2019-09-08T20:46:36-04:00
 * @Last modified by:   Nithin
 * @Last modified time: 2019-11-24T22:50:12-05:00
 */
package edu.unh.cs.nithin.retrieval_model;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

/**
 * ranking function used by lucene search engine to rank matching
 * documents according to their relevance to a given search query.
 */
public class BM25 {

	private String pagesFile;
	private String indexPath;
	private String outputPath;

	public BM25(String pagesFile, String indexPath, String outputPath) throws IOException {
		System.setProperty("file.encoding", "UTF-8");
		setIndexPath(indexPath);
		setOutputPath(outputPath);
		setPagesFile(pagesFile);
	}

	/**
	 * [Retrieve section level paragraphs for given category in outlines.cbor]
	 * @param catName
	 * @throws IOException
	 */
	public void PageSearch(String catName) throws IOException {
		File runfile = new File(getOutputPath() + "/runfile_page");
		runfile.createNewFile();
		FileWriter writer = new FileWriter(runfile);
		IndexSearcher searcher = setupIndexSearcher(getIndexPath(), "paragraph.lucene");
		searcher.setSimilarity(new BM25Similarity());
		final MyQueryBuilder queryBuilder = new MyQueryBuilder(new EnglishAnalyzer());
		final FileInputStream fileInputStream3 = new FileInputStream(new File(getPagesFile()));
		System.out.println("starting searching for pages ...");
		int count = 0;
		for (Data.Page page : DeserializeData.iterableAnnotations(fileInputStream3)) {
			ArrayList<String> categories = page.getPageMetadata().getCategoryNames();
			if (categories.contains(catName)) {
				System.out.println("lets see");
				final String queryId = page.getPageId();
				String queryStr = buildSectionQueryStr(page, Collections.<Data.Section>emptyList());
				TopDocs tops = searcher.search(queryBuilder.toQuery(queryStr), 100);
				ScoreDoc[] scoreDoc = tops.scoreDocs;
				for (int i = 0; i < scoreDoc.length; i++) {
					ScoreDoc score = scoreDoc[i];
					final Document doc = searcher.doc(score.doc); // to access stored content

					// print score and internal docid
					final String paragraphid = doc.getField("paragraphid").stringValue();
					final float searchScore = score.score;
					final int searchRank = i + 1;
					System.out.println(queryStr);
					writer.write(
							queryId + " Q0 " + paragraphid + " " + searchRank + " " + searchScore + " Lucene-BM25\n");
					count++;
				}
			}
		}
		writer.flush();
		writer.close();
		System.out.println("Write " + count + " results\nQuery Done!");

	}

	/**
	 * [Retrieve section level paragraphs for given category in outlines.cbor]
	 * @param catName
	 * @throws IOException
	 */
	public void SectionSearch(String catName) throws IOException {
		File runfile = new File(getOutputPath() + "/" + catName.replaceAll("[^A-Za-z0-9]", "_"));
		runfile.createNewFile();
		FileWriter writer = new FileWriter(runfile);
		IndexSearcher searcher = setupIndexSearcher(getIndexPath(), "paragraph.lucene");
		searcher.setSimilarity(new BM25Similarity());
		final MyQueryBuilder queryBuilder = new MyQueryBuilder(new EnglishAnalyzer());
		final FileInputStream fileInputStream3 = new FileInputStream(new File(getPagesFile()));
		System.out.println("starting searching for sections ...");
		int count = 0;
		for (Data.Page page : DeserializeData.iterableAnnotations(fileInputStream3)) {
			ArrayList<String> categories = page.getPageMetadata().getCategoryNames();
			if (categories.contains(catName)) {
				System.out.println(page.getPageId() + " ----- " );
				for (List<Data.Section> sectionPath : page.flatSectionPaths()) {
					final String queryId = Data.sectionPathId(page.getPageId(), sectionPath);
					String queryStr = buildSectionQueryStr(page, sectionPath);
					System.out.println(queryStr);
					TopDocs tops = searcher.search(queryBuilder.toQuery(queryStr), 100);
					ScoreDoc[] scoreDoc = tops.scoreDocs;
					for (int i = 0; i < scoreDoc.length; i++) {
						ScoreDoc score = scoreDoc[i];
						final Document doc = searcher.doc(score.doc);
						final String paragraphid = doc.getField("paragraphid").stringValue();
						final float searchScore = score.score;
						final int searchRank = i + 1;
						writer.write(queryId + " Q0 " + paragraphid + " " + searchRank + " " + searchScore
								+ " Lucene-BM25\n");
						count++;
					}
				}
			}
		}
		writer.flush();
		writer.close();
		System.out.println("Write " + count + " results\nQuery Done!");
	}

	/**
	 * [perform BM25 retrieval for given query strings]
	 * @param queryStrings
	 * @param outFile
	 * @throws IOException
	 */
	public void querySearch(String[] queryStrings, String outFile) throws IOException {
		File runfile = new File(outFile);
		runfile.createNewFile();
		FileWriter writer = new FileWriter(runfile);
		IndexSearcher searcher = setupIndexSearcher(getIndexPath(), "paragraph.lucene");
		searcher.setSimilarity(new BM25Similarity());
		final MyQueryBuilder queryBuilder = new MyQueryBuilder(new EnglishAnalyzer());
		System.out.println("starting searching for sections ...");
		int count = 0;
		for (String queryStr : queryStrings) {
			System.out.println(queryStr);
			TopDocs tops = searcher.search(queryBuilder.toQuery(queryStr), 10);
			ScoreDoc[] scoreDoc = tops.scoreDocs;
			for (int i = 0; i < scoreDoc.length; i++) {
				ScoreDoc score = scoreDoc[i];
				final Document doc = searcher.doc(score.doc);
				final String paragraphid = doc.getField("paragraphid").stringValue();
				final float searchScore = score.score;
				final int searchRank = i + 1;
				writer.write(queryStr + " Q0 " + paragraphid + " " + searchRank + " " + searchScore + " Lucene-BM25\n");
			}
		}
		writer.flush();
		writer.close();
		System.out.println("Write " + count + " results\nQuery Done!");
	}

	// Remove Duplicates from the runfile for sections
	public static void stripDuplicatesFromFile(String filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		Set<String> lines = new HashSet<String>(); // maybe should be bigger
		String line;
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}
		reader.close();
		System.out.println("Removing Duplicates");
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		for (String unique : lines) {
			writer.write(unique);
			writer.newLine();
		}
		writer.close();
	}

	// Author: Laura dietz
	public static class MyQueryBuilder {

		private final EnglishAnalyzer analyzer;
		private List<String> tokens;

		public MyQueryBuilder(EnglishAnalyzer standardAnalyzer) {
			analyzer = standardAnalyzer;
			tokens = new ArrayList<>(128);
		}

		public BooleanQuery toQuery(String queryStr) throws IOException {

			TokenStream tokenStream = analyzer.tokenStream("text", new StringReader(queryStr));
			tokenStream.reset();
			tokens.clear();
			while (tokenStream.incrementToken()) {
				final String token = tokenStream.getAttribute(CharTermAttribute.class).toString();
				tokens.add(token);
			}
			tokenStream.end();
			tokenStream.close();
			BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
			for (String token : tokens) {
				booleanQuery.add(new TermQuery(new Term("text", token)), BooleanClause.Occur.SHOULD);
			}
			return booleanQuery.build();
		}
	}

	// Author: Laura dietz
	private static IndexSearcher setupIndexSearcher(String indexPath, String typeIndex) throws IOException {
		Path path = FileSystems.getDefault().getPath(indexPath, typeIndex);
		Directory indexDir = FSDirectory.open(path);
		IndexReader reader = DirectoryReader.open(indexDir);
		return new IndexSearcher(reader);
	}

	// Author: Laura dietz
	private static String buildSectionQueryStr(Data.Page page, List<Data.Section> sectionPath) {
		StringBuilder queryStr = new StringBuilder();
		queryStr.append(page.getPageName());
		for (Data.Section section : sectionPath) {
			queryStr.append(" ").append(section.getHeading());
		}
		return queryStr.toString();
	}

	/**
	 * @return the pagesFile
	 */
	public String getPagesFile() {
		return pagesFile;
	}

	/**
	 * @param pagesFile the pagesFile to set
	 */
	public void setPagesFile(String pagesFile) {
		this.pagesFile = pagesFile;
	}

	/**
	 * @return the indexPath
	 */
	public String getIndexPath() {
		return indexPath;
	}

	/**
	 * @param indexPath the indexPath to set
	 */
	public void setIndexPath(String indexPath) {
		this.indexPath = indexPath;
	}

	/**
	 * @return the outputPath
	 */
	public String getOutputPath() {
		return outputPath;
	}

	/**
	 * @param outputPath the outputPath to set
	 */
	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}
}
