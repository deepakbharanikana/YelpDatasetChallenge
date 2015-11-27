package com.search.project.yelp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class EasySearch {

	private String indexPath;
	private String queryString;
	// private ArrayList<QueryTermInfo> qtList;
	private BufferedWriter bufferedFileWriter;
	public static int BATCHSIZE = 100;
	int batchWriteCount = 0;
	String batchWriterString;

	public EasySearch(String indexPath) {
		batchWriterString = "";
		batchWriteCount = 0;
		setIndexPath(indexPath);
	}

	public String getIndexPath() {
		return indexPath;
	}

	public void setIndexPath(String indexPath) {
		this.indexPath = indexPath;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public BufferedWriter getBufferedFileWriter() {
		return bufferedFileWriter;
	}

	public void setBufferedFileWriter(BufferedWriter bufferedFileWriter) {
		this.bufferedFileWriter = bufferedFileWriter;
	}

	public void printOutput(String outputString) {
		batchWriteCount++;
		batchWriterString = batchWriterString + outputString + "\n";
		if (batchWriteCount == BATCHSIZE) {
			System.out.println(batchWriterString);
			try {
				getBufferedFileWriter().write(batchWriterString + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			batchWriterString = "";
			batchWriteCount = 0;
		}
	}

	public void getRanking() {
		IndexReader reader;

		try {

			reader = DirectoryReader
					.open(FSDirectory.open(Paths.get(indexPath)));
			IndexSearcher searcher = new IndexSearcher(reader);

			int N = reader.maxDoc();
			// Get preprocessed query terms
			Analyzer analyzer = new StandardAnalyzer();
			QueryParser queryParser = new QueryParser("CATEGORIES", analyzer);
			Query query = queryParser.parse(queryParser.escape(queryString));
			Set<Term> queryTerms = new LinkedHashSet<Term>();
			searcher.createNormalizedWeight(query, false).extractTerms(
					queryTerms);
			printOutput("Terms in the query: ");
			// TODO Get the list of query terms and save them
			// qtList = new ArrayList<QueryTermInfo>();
			// for (Term t : queryTerms) {
			// printOutput(t.text());
			// QueryTermInfo qt = new QueryTermInfo();
			// qt.setTerm(t.text());
			// qtList.add(qt);
			// }

			printOutput(queryTerms.toString());
			ArrayList<String> qtList = new ArrayList<String>();
			for (Term t : queryTerms) {
				qtList.add(t.text());
			}

			// HashMap<String, DocumentScoreTuple> documentScoreMap = new
			// HashMap<String, DocumentScoreTuple>();

			// TODO Run the code for each query term in the queryString
			for (String qt : qtList) {
				// Get document frequency
				int df = reader.docFreq(new Term("CATEGORIES", qt));
				printOutput("Number of documents containing the term \"" + qt
						+ "\" for field \"CATEGORIES\": " + df);

				// Get document length and term frequency
				DefaultSimilarity dSimi = new DefaultSimilarity();
				List<LeafReaderContext> leafContexts = reader.getContext()
						.reader().leaves();

				TopScoreDocCollector collector = TopScoreDocCollector
						.create(25);
				searcher.search(query, collector);
				ScoreDoc[] docs = collector.topDocs().scoreDocs;

				one: for (LeafReaderContext leafContext : leafContexts) {
					int startDocNo = leafContext.docBase;
					int numberOfDoc = leafContext.reader().maxDoc();

					// Get frequency of the query term from its postings
					PostingsEnum de = MultiFields.getTermDocsEnum(
							leafContext.reader(), "CATEGORIES",
							new BytesRef(qt));

					int doc;
					if (de != null) {
						while ((doc = de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {

							// cnt of term in the doc
							int ct = de.freq();
							// normalized doc length is docLength
							int docID = de.docID();
							float normDocLength = dSimi
									.decodeNormValue(leafContext.reader()
											.getNormValues("CATEGORIES")
											.get(docID));
							float docLength = 1 / (normDocLength * normDocLength);
							// N = variable N
							// df(ti) = query term's df.
							int dfti = reader
									.docFreq(new Term("CATEGORIES", qt));
							// qt.setDf(dfti);
							Double TF = ((double) ct / docLength);
							Double IDF = Math.log10(1 + ((double) N / dfti));
							Double relScore = TF * IDF;
							relScore = TF;

							printOutput("ct: " + ct);
							printOutput("docLength: " + docLength);
							printOutput("N: " + N);
							printOutput("dfti: " + dfti);
							printOutput("TF: " + TF.toString());
							printOutput("IDF: " + IDF.toString());
							printOutput("relScore: " + relScore);
							// prepare the documentScoreTuple for current
							// document and term
							// DocumentScoreTuple docScoreTuple;
							// if (!documentScoreMap.containsKey(searcher.doc(
							// de.docID() + startDocNo).get("DOCNO"))) {
							// docScoreTuple = new DocumentScoreTuple(searcher
							// .doc(de.docID() + startDocNo).get(
							// "DOCNO"));
							// docScoreTuple.initiateScoreMap(qtList);
							// docScoreTuple.addScoreWithTerm(qt.getTerm(),
							// relScore);
							// documentScoreMap.put(
							// searcher.doc(de.docID() + startDocNo)
							// .get("DOCNO"), docScoreTuple);
							//
							// } else {
							// docScoreTuple = documentScoreMap.get(searcher
							// .doc(de.docID() + startDocNo).get(
							// "DOCNO"));
							// docScoreTuple.addScoreWithTerm(qt.getTerm(),
							// relScore);
							// documentScoreMap.put(
							// searcher.doc(de.docID() + startDocNo)
							// .get("DOCNO"), docScoreTuple);
							//
							// }
							// printOutput("Document Name: "
							// + searcher.doc(de.docID() + startDocNo)
							// .get("DOCNO"));
							// break one;
						}
					}

				}
			}

			// int i = 0;
			// for (Entry<String, DocumentScoreTuple> entry : documentScoreMap
			// .entrySet()) {
			// Double scoreForThisDoc = 0.0;
			// String documentName = entry.getKey();
			// DocumentScoreTuple docTuple = entry.getValue();
			// printOutput("Document Number: " + i);
			// printOutput("Document Name: " + documentName);
			//
			// for (Entry<String, Double> scoreTuple : docTuple
			// .getScoreWithTerms().entrySet()) {
			// Double scoreForThisDocAndTerm = scoreTuple.getValue();
			// printOutput("Score with Term \"" + scoreTuple.getKey()
			// + "\": " + scoreForThisDocAndTerm);
			// scoreForThisDoc += scoreForThisDocAndTerm;
			// }
			//
			// printOutput("Score with Query \"" + getQueryString() + "\": "
			// + entry.getValue().getTotalRelScore());
			// printOutput("");
			// i++;
			// }

			reader.close();
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void executeJob() {
		// Initialize Writer to write the console output to a file
		try {
			setBufferedFileWriter(new BufferedWriter(
					new FileWriter(
					// "C:/Users/mgokhale/Documents/GitHub/Z534_Search/src/com/search/project/yelp/OutputFiles"
							"F:/Users/Milind/Documents/GitHub/Z534_Search/src/com/search/project/yelp/OutputFiles/output.txt")));

			getRanking();

			// print remaining last batch which may be filled less than batch
			// size
			System.out.println(batchWriterString);
			getBufferedFileWriter().write(batchWriterString + "\n");
			getBufferedFileWriter().close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		EasySearch es = new EasySearch("C:/searchproject/yelp/index/business");
		// "C:/Users/mgokhale/Documents/GitHub/Z534_Search/src/com/search/project/yelp/index");

		// Initialize Writer to write the console output to a file
		es.setQueryString("PA, Restaurants, Eyewear & Opticians, Nutritionists");
		es.executeJob();
	}
}
