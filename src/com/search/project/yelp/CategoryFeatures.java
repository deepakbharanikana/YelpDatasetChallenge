/**
 * @author Milind Gokhale
 * This class primarily extracts the features for every category and ranks them by TFIDF score. 
 * Then top 100 words for each category. 
 * Then it writes category features map to the list.ser file in a serialized manner.
 * 
 * 
 * Date : November 27, 2015
 */

package com.search.project.yelp.task1;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.QueryBuilder;

import com.search.project.yelp.datatypes.Business;

/**
 * @author Milind
 * @Info This class is responsible for extracting feature words from the corpus
 *       for each category.
 * 
 */
public class CategoryFeatures {
	private String indexPath;
	private String queryString;
	private BufferedWriter bufferedFileWriter;
	public static int BATCHSIZE = 100;
	int batchWriteCount = 0;
	String batchWriterString;
	HashMap<String, HashMap<String, Integer>> categoryTokensMap = null;

	public CategoryFeatures(String indexPath) {
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

	/**
	 * 
	 * Function to get the bag of words for each category Here the bag of words
	 * are chosen as the words from review text for each category with top TFIDF
	 * scores
	 * 
	 * @return categoryFeatures
	 */
	public HashMap<String, String> getCategoryFeatures() {
		try {

			// Get the categories
			// hashmap [Key=Category: Value=hashmap[word:count]]
			categoryTokensMap = new HashMap<String, HashMap<String, Integer>>();
			HashMap<String, String> categoryFeatures = new HashMap<String, String>();

			HashSet<String> catList = Business.getCategoriesList();
			for (String category : catList) {
				// get features with scores for current category.
				HashMap<String, Double> features = getFeaturesWithScores(category);

				// Prepare and populate the category-features entry
				String queryKeyWords = "";
				queryKeyWords = sortAndPrepareFeatureWords(features);
				categoryFeatures.put(category, queryKeyWords);
			}
			return categoryFeatures;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 
	 * Get features with scores for current category 1. Get reviews for each
	 * category 2. calculate scores for each token in review set. 3. return the
	 * features with scores
	 * 
	 * @param category
	 * @return features with scores
	 * @throws IOException
	 */
	private HashMap<String, Double> getFeaturesWithScores(String category)
			throws IOException {
		IndexReader trainingSetReader = DirectoryReader.open(FSDirectory
				.open(Paths.get(indexPath + "/New_training_set")));

		IndexSearcher trainingSetSearcher = new IndexSearcher(trainingSetReader);
		TermQuery qry = new TermQuery(new Term("CATEGORIES", category));
		TopDocs businesses = trainingSetSearcher.search(qry, Integer.MAX_VALUE);
		// documents containing given category in category field
		ScoreDoc[] businessCategoryScoreDocs = businesses.scoreDocs;

		// hashset [Set of businesses for current categories] for the
		// for checking existing category's business set
		HashSet<Integer> businessCategoryDocSet = new HashSet<Integer>();

		// hashmap [Key=word: Value=word-count]]
		HashMap<String, Integer> reviewTokens = getRevTokensForBusiness(
				trainingSetSearcher, businessCategoryScoreDocs,
				businessCategoryDocSet);

		HashMap<String, Double> tokenScores = getTFIDF(reviewTokens,
				businessCategoryScoreDocs, trainingSetReader,
				businessCategoryDocSet);
		trainingSetReader.close();
		return tokenScores;
	}

	/**
	 * 
	 * // Get the TF IDF score for each review token of current review
	 * 
	 * @param reviewTokens
	 * @param businessCategoryScoreDocs
	 * @param trainingSetReader
	 * @param businessCategoryDocSet
	 * @return Hashmap containing the token and its TFIDF score
	 * @throws IOException
	 */
	private HashMap<String, Double> getTFIDF(
			HashMap<String, Integer> reviewTokens,
			ScoreDoc[] businessCategoryScoreDocs,
			IndexReader trainingSetReader,
			HashSet<Integer> businessCategoryDocSet) throws IOException {
		HashMap<String, Double> tokenScores = new HashMap<String, Double>();
		for (String token : reviewTokens.keySet()) {
			int N = businessCategoryScoreDocs.length;
			int dfti = 0;

			// hashmap [key:token,
			// value:tokenInformationForScoreCalculation]
			HashMap<String, TokenDocumentScore> tokenDocList = new HashMap<String, CategoryFeatures.TokenDocumentScore>();
			List<LeafReaderContext> leafContexts = trainingSetReader
					.getContext().reader().leaves();
			for (LeafReaderContext leafContext : leafContexts) {
				int startDocNo = leafContext.docBase;
				int numberOfDoc = leafContext.reader().maxDoc();
				// Get frequency of the query term i.e. token from its
				// postings
				int doc = 0;
				DefaultSimilarity dSimi = new DefaultSimilarity();
				PostingsEnum deReviews = MultiFields.getTermDocsEnum(
						leafContext.reader(), "REVIEWSTIPS",
						new BytesRef(token));
				if (deReviews != null) {
					while ((doc = deReviews.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
						// if the current posting document has the given
						// category in it only then go ahead and get the
						// token information for calculating TFIDF score
						// else skip
						if (businessCategoryDocSet.contains(deReviews.docID())) {
							TokenDocumentScore tokenInfo = new TokenDocumentScore();
							tokenInfo.ct = deReviews.freq();
							tokenInfo.docID = deReviews.docID();
							float normDocLength = dSimi
									.decodeNormValue(leafContext.reader()
											.getNormValues("REVIEWSTIPS")
											.get(deReviews.docID()));
							tokenInfo.docLength = 1 / (normDocLength * normDocLength);
							dfti++;
							tokenDocList.put(token, tokenInfo);
						}
					}
				}

			}
			// got all details so now get the total score for this
			// token
			Double tfidfScore = calculateTFIDFFromTokenInfo(tokenDocList, N,
					dfti);
			tokenScores.put(token, tfidfScore);
		}
		return tokenScores;
	}

	/**
	 * 
	 * // Start: For each business, 1. add business to set of businesses for
	 * current category, 2. get the review text, 3. tokenize review text it and
	 * remove the stopwords. 4. Return the tokens
	 * 
	 * @param trainingSetSearcher
	 * 
	 * 
	 * @param businessCategoryScoreDocs
	 * @param businessCategoryDocSet
	 * @return
	 * @throws IOException
	 */
	private HashMap<String, Integer> getRevTokensForBusiness(
			IndexSearcher trainingSetSearcher,
			ScoreDoc[] businessCategoryScoreDocs,
			HashSet<Integer> businessCategoryDocSet) throws IOException {
		HashMap<String, Integer> reviewTokens = new HashMap<String, Integer>();
		for (ScoreDoc businessCategoryScoreDoc : businessCategoryScoreDocs) {
			businessCategoryDocSet.add(businessCategoryScoreDoc.doc);
			Document businessCategoryDoc = trainingSetSearcher
					.doc(businessCategoryScoreDoc.doc);
			String reviewText = businessCategoryDoc.get("REVIEWSTIPS");
			if (reviewText != null) {
				tokenizeAndRemoveStopWords(reviewText, reviewTokens);
			}
		}
		return reviewTokens;
	}

	/**
	 * 
	 * Calculate the TFIDF score for each token from the information in its
	 * tokenDocInfo
	 * 
	 * @param tokenDocList
	 * @param N
	 * @param dfti
	 * @return TFIDF score
	 */
	private Double calculateTFIDFFromTokenInfo(
			HashMap<String, TokenDocumentScore> tokenDocList, int N, int dfti) {
		Double tfidfScore = 0.0;
		for (TokenDocumentScore tokenDocInfo : tokenDocList.values()) {
			// calculate TFIDF score
			Double TF = ((double) tokenDocInfo.ct / tokenDocInfo.docLength);
			Double IDF = Math.log10(1 + ((double) N / dfti));
			Double score = TF * IDF;
			tfidfScore += score;
		}
		return tfidfScore;
	}

	/**
	 * 
	 * Sort the token scores and formulate the string containing feature words
	 * 
	 * @param tokenScores
	 * @return FeatureWords String
	 */
	private String sortAndPrepareFeatureWords(
			HashMap<String, Double> tokenScores) {
		String queryKeyWords = "";
		HashMap<String, Double> sortedKeyWords = sortByValueDouble(tokenScores);

		for (String str : sortedKeyWords.keySet()) {
			queryKeyWords += " " + str;
		}
		return queryKeyWords;
	}

	/**
	 * Sorting function which will sort hash map entries based on value
	 * 
	 * @param unsortMap
	 * @return sortedMap
	 */
	private static HashMap sortByValueDouble(HashMap<String, Double> unsortMap) {
		List list = new LinkedList(unsortMap.entrySet());

		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue())
						.compareTo(((Map.Entry) (o1)).getValue());
			}
		});

		HashMap sortedMap = new LinkedHashMap();
		int numberOfWords = 0;
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
			if (sortedMap.size() == 100)
				break;
		}
		return sortedMap;

	}

	/**
	 * This function removes stop words from the reviewText and returns review
	 * tokens and their word count.
	 * 
	 * @param reviewText
	 * @param reviewTokens
	 */
	private void tokenizeAndRemoveStopWords(String reviewText,
			HashMap<String, Integer> reviewTokens) {
		String input = reviewText;
		Tokenizer tokenizer = new StandardTokenizer();

		try {
			tokenizer.setReader(new StringReader(input));
			StandardFilter standardFilter = new StandardFilter(tokenizer);
			LowerCaseFilter lowCaseFilter = new LowerCaseFilter(standardFilter);
			CharArraySet stopSet = StopFilter.makeStopSet(new String[] { "a",
					"about", "above", "above", "across", "after", "afterwards",
					"again", "against", "all", "almost", "alone", "along",
					"already", "also", "although", "always", "am", "among",
					"amongst", "amoungst", "amount", "an", "and", "another",
					"any", "anyhow", "anyone", "anything", "anyway",
					"anywhere", "are", "around", "as", "at", "back", "be",
					"became", "because", "become", "becomes", "becoming",
					"been", "before", "beforehand", "behind", "being", "below",
					"beside", "besides", "between", "beyond", "bill", "both",
					"bottom", "but", "by", "call", "can", "cannot", "cant",
					"co", "con", "could", "couldnt", "cry", "de", "describe",
					"detail", "do", "done", "down", "due", "during", "each",
					"eg", "eight", "either", "eleven", "else", "elsewhere",
					"empty", "enough", "etc", "even", "ever", "every",
					"everyone", "everything", "everywhere", "except", "few",
					"fifteen", "fify", "fill", "find", "fire", "first", "five",
					"for", "former", "formerly", "forty", "found", "four",
					"from", "front", "full", "further", "get", "give", "go",
					"had", "has", "hasnt", "have", "he", "hence", "her",
					"here", "hereafter", "hereby", "herein", "hereupon",
					"hers", "herself", "him", "himself", "his", "how",
					"however", "hundred", "ie", "if", "in", "inc", "indeed",
					"interest", "into", "is", "it", "its", "itself", "keep",
					"last", "latter", "latterly", "least", "less", "ltd",
					"made", "many", "may", "me", "meanwhile", "might", "mill",
					"mine", "more", "moreover", "most", "mostly", "move",
					"much", "must", "my", "myself", "name", "namely",
					"neither", "never", "nevertheless", "next", "nine", "no",
					"nobody", "none", "noone", "nor", "not", "nothing", "now",
					"nowhere", "of", "off", "often", "on", "once", "one",
					"only", "onto", "or", "other", "others", "otherwise",
					"our", "ours", "ourselves", "out", "over", "own", "part",
					"per", "perhaps", "please", "put", "rather", "re", "same",
					"see", "seem", "seemed", "seeming", "seems", "serious",
					"several", "she", "should", "show", "side", "since",
					"sincere", "six", "sixty", "so", "some", "somehow",
					"someone", "something", "sometime", "sometimes",
					"somewhere", "still", "such", "system", "take", "ten",
					"than", "that", "the", "their", "them", "themselves",
					"then", "thence", "there", "thereafter", "thereby",
					"therefore", "therein", "thereupon", "these", "they",
					"thickv", "thin", "third", "this", "those", "though",
					"three", "through", "throughout", "thru", "thus", "to",
					"together", "too", "top", "toward", "towards", "twelve",
					"twenty", "two", "un", "under", "until", "up", "upon",
					"us", "very", "via", "was", "we", "well", "were", "what",
					"whatever", "when", "whence", "whenever", "where",
					"whereafter", "whereas", "whereby", "wherein", "whereupon",
					"wherever", "whether", "which", "while", "whither", "who",
					"whoever", "whole", "whom", "whose", "why", "will", "with",
					"within", "without", "would", "yet", "you", "your",
					"yours", "yourself", "yourselves", "1", "2", "3", "4", "5",
					"6", "7", "8", "9", "10", "1.", "2.", "3.", "4.", "5.",
					"6.", "11", "7.", "8.", "9.", "12", "13", "14", "A", "B",
					"C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
					"O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
					"terms", "CONDITIONS", "conditions", "values",
					"interested.", "care", "sure", ".", "!", "@", "#", "$",
					"%", "^", "&", "*", "(", ")", "{", "}", "[", "]", ":", ";",
					",", "<", ".", ">", "/", "?", "_", "-", "+", "=", "a", "b",
					"c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
					"o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
					"contact", "grounds", "buyers", "tried", "said,", "plan",
					"value", "principle.", "forces", "sent:", "is,", "was",
					"like", "discussion", "tmus", "diffrent.", "layout",
					"area.", "thanks", "thankyou", "hello", "bye", "rise",
					"fell", "fall", "psqft.", "http://", "km", "miles" }, true);
			StopFilter stopFilter = new StopFilter(lowCaseFilter, stopSet);
			CharTermAttribute charTermAttribute = tokenizer
					.addAttribute(CharTermAttribute.class);

			stopFilter.reset();
			while (stopFilter.incrementToken()) {
				String token = charTermAttribute.toString().toString();
				if (reviewTokens.get(token) == null) {
					reviewTokens.put(token, 1);
				} else {
					reviewTokens.put(token, reviewTokens.get(token) + 1);
				}
			}
			// System.out.println(reviewTokens);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * This function is the trigger to execute the job of formulating category
	 * feature map and putting it in disk list.ser in serialized form.
	 */
	public void executeJob() {
		// Initialize Writer to write the console output to a file
		try {
			setBufferedFileWriter(new BufferedWriter(
					new FileWriter(UtilFunctions.getMySourcePath()
							+ "/com/search/project/yelp/OutputFiles/output.txt")));

			// getRanking();
			HashMap<String, String> categoryWordsMap = getCategoryFeatures();
			printOutput("The Category Features: \n");
			printOutput(categoryWordsMap.toString());

			writeCategoryFeaturesInFS(categoryWordsMap);

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

	/**
	 * 
	 * This function writes the category feature map in file system in
	 * serialized form
	 * 
	 * @param categoryWordsMap
	 */
	private void writeCategoryFeaturesInFS(
			HashMap<String, String> categoryWordsMap) {
		FileOutputStream fos;
		FileInputStream fis;
		try {
			fos = new FileOutputStream("C:/searchproject/list.ser");

			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(categoryWordsMap);
			oos.close();
			fos.close();
			fis = new FileInputStream("C:/searchproject/list.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			Map<String, String> anotherList = (Map<String, String>) ois
					.readObject();

			ois.close();
			fis.close();
			System.out.println(anotherList);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		CategoryFeatures cf = new CategoryFeatures(
				"C:/searchproject/yelp/index");
		cf.executeJob();

	}

	/**
	 * @author Milind
	 * @info Inner class to hold and organize the token information for
	 *       calculating the token TFIDF score.
	 */
	public class TokenDocumentScore {
		String token;
		int docID;
		int ct;
		float docLength;
		Double tfidfScore;

	}

	/**
	 * This code is used in the earlier approach which took more time to
	 * generate the category features map.
	 * 
	 * Deprecated: The function to read the businesses and reviews index
	 * separately and get feature ranks
	 */
	public void getRanking() {
		IndexReader businessIndexReader;
		IndexReader reviewIndexReader;

		try {

			businessIndexReader = DirectoryReader.open(FSDirectory.open(Paths
					.get(indexPath + "/business")));
			IndexSearcher businessSearcher = new IndexSearcher(
					businessIndexReader);
			reviewIndexReader = DirectoryReader.open(FSDirectory.open(Paths
					.get(indexPath + "/review")));
			IndexSearcher reviewSearcher = new IndexSearcher(reviewIndexReader);

			int N = businessIndexReader.maxDoc();

			// hashmap [Key=Category: Value=hashmap[word:count]]
			categoryTokensMap = new HashMap<String, HashMap<String, Integer>>();

			HashSet<String> catList = Business.getCategoriesList();

			// TODO Run the code for each query term in the queryString
			for (String category : catList) {
				// Get document frequency

				TermQuery qry = new TermQuery(new Term("CATEGORIES", category));
				TopDocs businesses = businessSearcher.search(qry,
						Integer.MAX_VALUE);
				ScoreDoc[] businessScoreDocs = businesses.scoreDocs;

				int df = businessIndexReader.docFreq(new Term("CATEGORIES",
						category));
				// printOutput("Number of businesses containing the term \""
				// + category + "\" for field \"CATEGORIES\": " + df);

				HashMap<String, Integer> reviewTokens = new HashMap<String, Integer>();
				//
				// printOutput(category);

				for (ScoreDoc businessScoreDoc : businessScoreDocs) {
					Document businessDoc = businessSearcher
							.doc(businessScoreDoc.doc);
					String businessId = businessDoc.get("BUSINESSID");
					String categories = businessDoc.get("CATEGORIES");

					QueryBuilder builder = new QueryBuilder(
							new WhitespaceAnalyzer());
					Query queryBusId = builder.createPhraseQuery("BUSINESSID",
							businessId);

					TopDocs topReviews = reviewSearcher.search(queryBusId,
							Integer.MAX_VALUE);
					ScoreDoc[] reviewScoreDocs = topReviews.scoreDocs;
					for (ScoreDoc reviewScoreDoc : reviewScoreDocs) {
						Document reviewDoc = reviewSearcher
								.doc(reviewScoreDoc.doc);
						String reviewId = reviewDoc.get("REVIEWID");
						String reviewText = reviewDoc.get("TEXT");

						tokenizeAndRemoveStopWords(reviewText, reviewTokens);

					}
				}
				HashMap sortedKeyWords = sortByValue(reviewTokens);
				categoryTokensMap.put(category, sortedKeyWords);
			}

			businessIndexReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param unsortMap
	 * @return sortedMap
	 */
	private static HashMap sortByValue(HashMap<String, Integer> unsortMap) {
		List list = new LinkedList(unsortMap.entrySet());

		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue())
						.compareTo(((Map.Entry) (o1)).getValue());
			}
		});

		HashMap sortedMap = new LinkedHashMap();
		int numberOfWords = 0;
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
			if (sortedMap.size() == 100)
				break;
		}
		return sortedMap;

	}

}
