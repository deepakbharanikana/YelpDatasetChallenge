/**
 * @author Milind Gokhale
 * This class primarily prepares a mongodb collection containing all the businesses and their assigned categories along with the scores for each category.
 * Thus the collection prepared by this class can be used to sort and pick definite number of assignments and performance evaluation in the Evaluation class.
 * 
 * Date : November 28, 2015
 * 
 */

package com.search.project.yelp.task1;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.FSDirectory;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

/**
 * @author Milind
 *
 *         This main purpose of this class is to prepare the category assignment
 *         map for all the businesses in the test set.
 *
 */
public class AssignCategories {
	private String indexPath;
	private String queryString;
	private BufferedWriter bufferedFileWriter;
	public static int BATCHSIZE = 100;
	int batchWriteCount = 0;
	String batchWriterString;
	HashMap<String, HashMap<String, Double>> businessCategoryAssignment = null;
	MongoClient mongoClient = null;
	MongoDatabase db = null;

	private AssignCategories(String indexPath) {
		batchWriterString = "";
		batchWriteCount = 0;
		setIndexPath(indexPath);
		businessCategoryAssignment = new HashMap<String, HashMap<String, Double>>();
		mongoClient = new MongoClient("localhost", 27017);
		db = mongoClient.getDatabase("yelp");

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

	public HashMap<String, HashMap<String, Double>> getBusinessCategoryAssignment() {
		return businessCategoryAssignment;
	}

	public void setBusinessCategoryAssignment(
			HashMap<String, HashMap<String, Double>> businessCategoryAssignment) {
		this.businessCategoryAssignment = businessCategoryAssignment;
	}

	/**
	 * 
	 * function to print the output to a file
	 * 
	 * @param outputString
	 */
	public void printOutput(String outputString) {
		try {
			getBufferedFileWriter().write(outputString);
		} catch (IOException e) {
			e.printStackTrace();
			printOutput(e.getClass() + " Exception Occurred: " + e.getMessage());
		}
	}

	/**
	 * Main ranking function which runs a search with given querystring as input
	 * and gets the category scores for the querystring. This function gets the
	 * category scores for each query and inserts them in a collection in
	 * mongodb.
	 * 
	 * 
	 * @param businessID
	 * @param queryStringInput
	 * @param algo
	 */
	public void getRanking(String businessID, String queryStringInput,
			String algo) {
		IndexReader reader;
		try {
			String queryString = tokenizeAndRemoveStopWords(queryStringInput);

			MongoCollection<org.bson.Document> assignmentScoreMapCollection = db
					.getCollection("New_AssignmentScoreMap");

			reader = DirectoryReader.open(FSDirectory.open(Paths
					.get(getIndexPath() + "/categoriesAndBagOfWords")));
			IndexSearcher searcher = new IndexSearcher(reader);
			Analyzer analyzer = new StandardAnalyzer();
			searcher.setSimilarity(new DefaultSimilarity());
			printOutput("Processing Business: " + businessID);
			System.out.println("Processing Business: " + businessID);
			QueryParser parser = new QueryParser("BAGOFWORDS", analyzer);
			System.setProperty("org.apache.lucene.maxClauseCount",
					Integer.toString(Integer.MAX_VALUE));
			Query query = parser.parse(QueryParser.escape(queryString));
			TopDocs results = searcher.search(query, Integer.MAX_VALUE);

			int numTotalHits = results.totalHits;

			// Print retrieved results
			org.bson.Document assignmentScoreMapRow = new org.bson.Document();
			assignmentScoreMapRow.put("business_id", businessID);
			ScoreDoc[] hits = results.scoreDocs;
			HashMap<String, Double> categoryScores = new HashMap<String, Double>();
			;
			for (int i = 0; i < hits.length; i++) {
				Document doc = searcher.doc(hits[i].doc);
				ScoreDoc categoryHit = hits[i];

				Document currCategoryDoc = searcher.doc(categoryHit.doc);
				categoryScores.put(currCategoryDoc.get("CATEGORY"),
						(double) categoryHit.score);
				// insert the only top 100 category search results
				if (i > 100)
					break;
			}
			assignmentScoreMapRow.put("categories", categoryScores);
			assignmentScoreMapCollection.insertOne(assignmentScoreMapRow);

			reader.close();
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			printOutput(e.getClass() + " Exception Occurred: " + e.getMessage());
		}

	}

	/**
	 * 
	 * Get the category-features map
	 * 
	 * @return category features HashMap
	 */
	public static Map<String, String> getCategoryWordsMap() {
		Map<String, String> category = UtilFunctions.getCategoryFeaturesMap();
		for (Entry<String, String> str : category.entrySet()) {
			System.out.println(str.getKey() + ": " + str.getValue());
		}
		CategoryFeatures cf = new CategoryFeatures(
				"C:/searchproject/yelp/index");
		return category;
	}

	/**
	 * 
	 * Tokenize the given review or tip text and remove the stop words from the
	 * text and return the text.
	 * 
	 * @param reviewText
	 * @return
	 */
	private String tokenizeAndRemoveStopWords(String reviewText) {
		String input = reviewText;
		String output = "";
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
					"fell", "fall", "psqft.", "http://", "km", "miles", "'", },
					true);
			StopFilter stopFilter = new StopFilter(lowCaseFilter, stopSet);
			CharTermAttribute charTermAttribute = tokenizer
					.addAttribute(CharTermAttribute.class);

			stopFilter.reset();
			HashMap<String, Integer> reviewTokens = new HashMap<String, Integer>();

			while (stopFilter.incrementToken()) {
				String token = charTermAttribute.toString().toString();
				// printOutput(token);
				if (reviewTokens.get(token) == null) {
					reviewTokens.put(token, 1);
					output += token + " ";
				} else {
					reviewTokens.put(token, reviewTokens.get(token) + 1);
				}
			}
			// System.out.println(reviewTokens);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}

	/**
	 * This function is the trigger for running the main purpose functions in
	 * this class.
	 * 
	 * @param categories
	 * @param categoryScores
	 * @param inputBusinessId
	 */
	private void executeJob(Map<String, String> categories,
			HashMap<String, Double> categoryScores, String inputBusinessId) {
		try {
			setBufferedFileWriter(new BufferedWriter(new FileWriter(
					"src/com/search/project/yelp/OutputFiles/Compare_" + "VSM"
							+ "_" + "reviews" + ".txt")));

			MongoCollection<org.bson.Document> testSetCollection = db
					.getCollection("New_test_set");
			MongoCursor<org.bson.Document> testSetCursor = testSetCollection
					.find().iterator();
			while (testSetCursor.hasNext()) {
				// form the query string containing the reviews and tips
				// together
				String queryString = "";
				org.bson.Document business = testSetCursor.next();
				String businessID = (String) business.get("business_id");
				List<String> reviews = (List<String>) business.get("reviews");
				if (reviews != null) {
					for (String review : reviews) {
						queryString += review + " ";
					}
				}
				List<String> tips = (List<String>) business.get("tips");
				if (tips != null) {
					for (String tip : tips) {
						queryString += tip + " ";
					}
				}
				getRanking(businessID, queryString, "VSM");
			}

			getBufferedFileWriter().write(batchWriterString + "\n");
			getBufferedFileWriter().close();
			mongoClient.close();
		} catch (IOException e) {
			e.printStackTrace();
			printOutput(e.getClass() + " Exception Occurred: " + e.getMessage());
		}
		batchWriterString = "";
	}

	public static void main(String[] args) {
		String inputBusinessId = "";
		HashMap<String, Double> categoryScores = new HashMap<String, Double>();
		Map<String, String> categories = null;
		AssignCategories ac = new AssignCategories(
				"C:/searchproject/yelp/index");
		ac.executeJob(categories, categoryScores, inputBusinessId);
	}

}
