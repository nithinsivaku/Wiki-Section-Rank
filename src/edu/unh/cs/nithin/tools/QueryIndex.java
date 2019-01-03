package edu.unh.cs.nithin.tools;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
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
import edu.unh.cs.treccar_v2.Data.PageSkeleton;
import edu.unh.cs.treccar_v2.Data.Section;

public class QueryIndex {

	
	public String getParagraphForId(String indexPath, String paraId) throws IOException, ParseException {
		// TODO Auto-generated method stub
		String paragraph = null;
		Analyzer analyzer = new StandardAnalyzer();
		IndexSearcher searcher = setupIndexSearcher(indexPath, "paragraph.lucene");
		searcher.setSimilarity(new BM25Similarity());
		final MyQueryBuilder queryBuilder = new MyQueryBuilder(new StandardAnalyzer());

		TopDocs tops = searcher.search(queryBuilder.toQuery(paraId), 1);
		ScoreDoc[] scoreDoc = tops.scoreDocs;
		for (int i = 0; i < 0; i++) {
			ScoreDoc score = scoreDoc[i];
			final Document doc = searcher.doc(score.doc);
			paragraph = doc.getField("text").stringValue();

		}

		String paraText = searcher.doc(searcher.search(queryBuilder.toQuery(paraId), 1).scoreDocs[0].doc)
				.getField("text").stringValue();

		return paraText;
	}
	
	// Author: Laura dietz
		public static class MyQueryBuilder {

			private final StandardAnalyzer analyzer;
			private List<String> tokens;

			public MyQueryBuilder(StandardAnalyzer standardAnalyzer) {
				analyzer = standardAnalyzer;
				tokens = new ArrayList<>(128);
			}

			public BooleanQuery toQuery(String queryStr) throws IOException {

				TokenStream tokenStream = analyzer.tokenStream("paragraphid", new StringReader(queryStr));
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
					booleanQuery.add(new TermQuery(new Term("paragraphid", token)), BooleanClause.Occur.SHOULD);
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

			// System.out.println("queryStr = " + queryStr);
			return queryStr.toString();
		}

		// Author: Laura dietz, modified by Nithin for lowest heading in each
		// section
		private static String buildSectionQueryStr(List<Data.Section> sectionPath) {
			String queryStr = " ";
			List<PageSkeleton> child;

			for (Data.Section section : sectionPath) {

				child = section.getChildren();
				if (!(child.isEmpty())) {
					Section s = (Section) child.get(child.size() - 1);
					queryStr = s.getHeading();

				} else {
					queryStr = section.getHeading();
				}

			}
			return queryStr;
		}

	
	
	
}
