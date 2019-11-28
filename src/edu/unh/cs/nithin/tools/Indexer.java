/**
 * @Author: Nithin Sivakumar <Nithin>
 * @Date:   2019-03-17T17:15:55-04:00
 * @Last modified by:   Nithin
 * @Last modified time: 2019-11-27T21:12:55-05:00
 */
package edu.unh.cs.nithin.tools;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * build a lucene index of trec car paragraphs
 */
public class Indexer {

	public Indexer() throws IOException{
		System.setProperty("file.encoding", "UTF-8");

		String mode = "paragraphs";
		String indexPath = "/home/ns1077/work/paragraphIndex/";

		if (mode.equals("paragraphs")) {
			final String paragraphsFile = "/home/ns1077/work/paragraphCorpus/dedup.articles-paragraphs.cbor";
			final FileInputStream fileInputStream2 = new FileInputStream(new File(paragraphsFile));

			System.out.println("Creating paragraph index in " + indexPath);
			final IndexWriter indexWriter = setupIndexWriter(indexPath, "paragraph.lucene");
			final Iterator<Data.Paragraph> paragraphIterator = DeserializeData.iterParagraphs(fileInputStream2);

			for (int i = 1; paragraphIterator.hasNext(); i++) {
				final Document doc = paragraphToLuceneDoc(paragraphIterator.next());
				indexWriter.addDocument(doc);
			}

			System.out.println("\n Done indexing.");

			indexWriter.commit();
			indexWriter.close();
		} else if (mode.equals("pages")) {
			final String pagesFile = "Untitled⁩/Users⁩/⁨nithinsivakumar⁩/⁨Desktop⁩/⁨trec⁩/paragraphCorpus⁩/dedup.articles-paragraphs.cbor/";
			final FileInputStream fileInputStream = new FileInputStream(new File(pagesFile));

			System.out.println("Creating page index in " + indexPath);
			final IndexWriter indexWriter = setupIndexWriter(indexPath, "pages.lucene");

			final Iterator<Data.Page> pageIterator = DeserializeData.iterAnnotations(fileInputStream);

			for (int i = 1; pageIterator.hasNext(); i++) {
				final Document doc = pageToLuceneDoc(pageIterator.next());

				indexWriter.addDocument(doc);
			}

			System.out.println("\n Done indexing.");

			indexWriter.commit();
			indexWriter.close();
		}
	}

	public static class ParaToLuceneIterator implements Iterator<Document> {
		private static final int DEBUG_EVERY = 10000;
		private int counter = DEBUG_EVERY;
		private final Iterator<Data.Paragraph> paragraphIterator;

		ParaToLuceneIterator(Iterator<Data.Paragraph> paragraphIterator) {
			this.paragraphIterator = paragraphIterator;
		}

		@Override
		public boolean hasNext() {
			return this.paragraphIterator.hasNext();
		}

		@Override
		public Document next() {
			counter--;
			if (counter < 0) {
				System.out.print('.');
				counter = DEBUG_EVERY;
			}

			Data.Paragraph p = this.paragraphIterator.next();
			return paragraphToLuceneDoc(p);
		}

		@Override
		public void remove() {
			this.paragraphIterator.remove();
		}
	}

	@NotNull
	private static Document paragraphToLuceneDoc(Data.Paragraph p) {
		final Document doc = new Document();
		final String content = p.getTextOnly(); // <-- Todo Adapt this to your
												// needs!
		doc.add(new TextField("text", content, Field.Store.YES));
		doc.add(new StringField("paragraphid", p.getParaId(), Field.Store.YES)); // don't
																					// tokenize
																					// this!


		doc.add(new StringField("entities", String.join(" ", p.getEntitiesOnly()), Field.Store.YES));
		System.out.println(p.getBodies());
		return doc;
	}

	public static class PageToLuceneIterator implements Iterator<Document> {
		private static final int DEBUG_EVERY = 1000;
		private int counter = DEBUG_EVERY;
		private final Iterator<Data.Page> pageIterator;

		PageToLuceneIterator(Iterator<Data.Page> pageIterator) {
			this.pageIterator = pageIterator;
		}

		@Override
		public boolean hasNext() {
			return this.pageIterator.hasNext();
		}

		@Override
		public Document next() {
			counter--;
			if (counter < 0) {
				System.out.print('.');
				counter = DEBUG_EVERY;
			}

			Data.Page p = this.pageIterator.next();
			return pageToLuceneDoc(p);
		}

		@Override
		public void remove() {
			this.pageIterator.remove();
		}
	}

	@NotNull
	private static Document pageToLuceneDoc(Data.Page p) {
		final Document doc = new Document();
		StringBuilder content = new StringBuilder();
		pageContent(p, content); // Todo Adapt this to your needs!

		doc.add(new TextField("text", content.toString(), Field.Store.NO)); // dont
																			// store,
																			// just
																			// index
		doc.add(new StringField("pageid", p.getPageId(), Field.Store.YES)); // don't
																			// tokenize
																			// this!
		System.out.println(p.getPageName());
		System.out.println(content);
		System.out.println(p.getPageMetadata().getCategoryNames());
		return doc;
	}

	private static void sectionContent(Data.Section section, StringBuilder content) {
		content.append(section.getHeading() + '\n');
		for (Data.PageSkeleton skel : section.getChildren()) {
			if (skel instanceof Data.Section)
				sectionContent((Data.Section) skel, content);
			else if (skel instanceof Data.Para)
				paragraphContent((Data.Para) skel, content);
			else {
			}
		}
	}

	private static void paragraphContent(Data.Para paragraph, StringBuilder content) {
		content.append(paragraph.getParagraph().getTextOnly()).append('\n');
	}

	private static void pageContent(Data.Page page, StringBuilder content) {
		content.append(page.getPageName()).append('\n');

		for (Data.PageSkeleton skel : page.getSkeleton()) {
			if (skel instanceof Data.Section)
				sectionContent((Data.Section) skel, content);
			else if (skel instanceof Data.Para)
				paragraphContent((Data.Para) skel, content);
			else {
			} // ignore other
		}

	}

	@NotNull
	private static IndexWriter setupIndexWriter(String indexPath, String typeIndex) throws IOException {
		Path path = FileSystems.getDefault().getPath(indexPath, typeIndex);
		Directory indexDir = FSDirectory.open(path);
		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		return new IndexWriter(indexDir, config);
	}
}
