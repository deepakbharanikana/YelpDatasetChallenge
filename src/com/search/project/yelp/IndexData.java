/**
 * @author Milind Gokhale
 * This code primarily extracts data from the training and test sets and prepares index for each dataset.
 * The index prepared is a lucene index. Lucene index fields used in the final approach
 * [BUSINESSID, CATEGORIES, REVIEWSTIPS]
 * 
 * This class also contains code for extracting data json file and prepare index for business, review and tip json files for approach without using mongodb.
 * The index structure used in earlier approach was same as the json structure of each business, review and tip 
 * However it took more time to extract category features using this approach hence finally mongodb was used for data storage. 
 * 
 * This class also contains code to apply POS tagging and prepare the bag of words for each category.
 * 
 * TrainingSet and TestSet table structure
 * [BusinessID, Categories, Reviews, Tips]
 * 
 * Date : November 17, 2015
 */

package com.search.project.yelp.task1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.search.project.yelp.datatypes.Business;
import com.search.project.yelp.datatypes.Review;
import com.search.project.yelp.datatypes.Tip;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * @author Milind
 * @info Class to index data
 */
public class IndexData {

	private String indexPath;
	private String corpusPath;
	private String sourcePath;
	MaxentTagger tagger = new MaxentTagger(
			"H:/Users/Milind/Downloads/stanford-postagger-2015-04-20/stanford-postagger-2015-04-20/models/english-left3words-distsim.tagger");
	TokenizerFactory<CoreLabel> ptbTokenizerFactory = PTBTokenizer.factory(
			new CoreLabelTokenFactory(), "untokenizable=noneKeep");

	public IndexData() {
		setSourcePath(UtilFunctions.getMySourcePath());
	}

	public String getIndexPath() {
		return indexPath;
	}

	public void setIndexPath(String indexPath) {
		this.indexPath = indexPath;
	}

	public String getCorpusPath() {
		return corpusPath;
	}

	public void setCorpusPath(String corpusPath) {
		this.corpusPath = corpusPath;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	/**
	 * This function generates index of training and test set. Input tableName
	 * takes the name of training or test set collection in mongodb.
	 * 
	 * @param tableName
	 */
	private void generateMongoTableIndex(String tableName) {
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase db = mongoClient.getDatabase("yelp");
		MongoCollection<org.bson.Document> table = db.getCollection(tableName);
		MongoCursor<org.bson.Document> cursor = table.find().iterator();

		Directory dir;
		try {
			dir = FSDirectory.open(Paths.get(indexPath + "\\" + tableName));
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			IndexWriter writer;
			writer = new IndexWriter(dir, iwc);

			while (cursor.hasNext()) {
				// Create a lucene document and add to lucene index
				org.bson.Document businessDoc = cursor.next();
				Document ldoc = new Document();

				String businessID = (String) businessDoc.get("business_id");
				ldoc.add(new StringField("BUSINESSID", businessID,
						Field.Store.YES));
				ArrayList<String> categories = (ArrayList<String>) businessDoc
						.get("categories");
				for (String str : categories) {
					ldoc.add(new StringField("CATEGORIES", str, Field.Store.YES));
				}
				ArrayList<String> reviews = (ArrayList<String>) businessDoc
						.get("reviews");
				if (reviews != null) {
					for (String str : reviews) {
						ldoc.add(new TextField("REVIEWSTIPS", str,
								Field.Store.YES));
					}
				}
				ArrayList<String> tips = (ArrayList<String>) businessDoc
						.get("tips");
				for (String str : tips) {
					ldoc.add(new TextField("REVIEWSTIPS", str, Field.Store.YES));
				}

				writer.addDocument(ldoc);
			}

			writer.forceMerge(1);
			writer.commit();
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mongoClient.close();
	}

	/**
	 * This function POS tags the words in the category features and extracts
	 * nouns from them. It puts the new bag of words in the categoryFeatureMap
	 * and returns the map.
	 * 
	 * @param categoryFeatures
	 * @return categoryFeatuesMap
	 * @throws IOException
	 */
	public HashMap<String, String> posTagCategoryFeatures(
			HashMap<String, String> categoryFeatures) throws IOException {

		HashMap<String, String> categoryFeaturesMap = new HashMap<String, String>();

		for (Entry<String, String> categoryFeatureEntry : categoryFeatures
				.entrySet()) {
			String categoryFeatureValue = "";
			String category = categoryFeatureEntry.getKey();
			String categoryValue = categoryFeatureEntry.getValue();

			FileWriter fw = new FileWriter(
					"H:/Users/Milind/Downloads/stanford-postagger-2015-04-20/stanford-postagger-2015-04-20/categoryFeatureWordsFile.txt");
			fw.write(categoryValue);
			fw.close();
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out,
					"utf-8"));
			DocumentPreprocessor documentPreprocessor = new DocumentPreprocessor(
					"H:/Users/Milind/Downloads/stanford-postagger-2015-04-20/stanford-postagger-2015-04-20/categoryFeatureWordsFile.txt");
			documentPreprocessor.setTokenizerFactory(ptbTokenizerFactory);
			for (List<HasWord> sentence : documentPreprocessor) {
				List<TaggedWord> tSentence = tagger.tagSentence(sentence);
				for (TaggedWord tw : tSentence) {
					if (tw.tag().startsWith("NN") && tw.word().length() > 3) {
						categoryFeatureValue += tw.word() + " ";
					}
				}
			}
			categoryFeaturesMap.put(category, categoryFeatureValue);
			pw.println("category: " + category + " processed");
		}
		// pw.close();

		return categoryFeaturesMap;
	}

	/**
	 * Take the category features map and generate 
	 * 
	 * @param categoryFeaturesMap
	 */
	private void generateCategoriesIndex(
			HashMap<String, String> categoryFeaturesMap) {
		Directory dir;
		try {
			dir = FSDirectory.open(Paths.get(indexPath + "\\"
					+ "categoriesAndBagOfWords"));
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			IndexWriter writer;
			writer = new IndexWriter(dir, iwc);

			for (Entry<String, String> categoryEntry : categoryFeaturesMap
					.entrySet()) {
				// Form lucene document and add to index
				Document ldoc = new Document();

				ldoc.add(new StringField("CATEGORY", categoryEntry.getKey(),
						Field.Store.YES));
				ldoc.add(new TextField("BAGOFWORDS", categoryEntry.getValue(),
						Field.Store.YES));

				writer.addDocument(ldoc);
			}
			writer.forceMerge(1);
			writer.commit();
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Earlier approach to index business, review and tip json files and then
	 * use it to generate category features.
	 * 
	 * @param object
	 */
	public void generateIndex(String object) {
		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(".\\IndexingProgress.txt");
			System.out.println("Indexing " + object + " to directory '"
					+ getIndexPath() + "/" + object + "'...");
			fileWriter.write("Indexing to directory '" + getIndexPath() + "/"
					+ object + "'...\n");

			Directory dir = FSDirectory.open(Paths.get(indexPath + "\\"
					+ object));
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			IndexWriter writer;
			writer = new IndexWriter(dir, iwc);

			// fetch all the files from the corpus path
			File corpus = new File(getSourcePath() + getCorpusPath());
			File[] files = corpus.listFiles();

			for (File file : files) {
				// condition to neglect files other than files we want
				if (!file.getName().contains(object))
					continue;

				BufferedReader br = new BufferedReader(new FileReader(file));
				String lineInFile = "";
				Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd")
						.create();

				while ((lineInFile = br.readLine()) != null) {
					Document ldoc = new Document();
					switch (object) {
					case "review":
						Review review = null;
						review = new Review();
						prepareAndIndexReview(gson, review, ldoc, lineInFile);
						break;
					case "tip":
						Tip tip = null;
						tip = new Tip();
						prepareAndIndexTip(gson, tip, ldoc, lineInFile);
						break;
					case "business":
						Business business = null;
						business = new Business();
						prepareAndIndexBusiness(gson, business, ldoc,
								lineInFile);
						break;
					}
					writer.addDocument(ldoc);
				}

				writer.forceMerge(1);
				writer.commit();
				writer.close();
				br.close();
				System.out.println("Indexing Complete");
				fileWriter.write("Indexing Complete");
				fileWriter.close();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * prepare entry of lucene document to insert in the lucene index for
	 * reviews
	 * 
	 * @param gson
	 * @param review
	 * @param ldoc
	 * @param lineInFile
	 */
	public void prepareAndIndexReview(Gson gson, Review review, Document ldoc,
			String lineInFile) {
		review = gson.fromJson(lineInFile, Review.class);
		System.out.println("Indexing document: " + review.getReview_id());
		ldoc.add(new StringField("USERID", review.getUser_id(), Field.Store.YES));
		ldoc.add(new TextField("REVIEWID", review.getReview_id(),
				Field.Store.YES));
		ldoc.add(new DoubleField("STARS", review.getStars(), Field.Store.YES));
		ldoc.add(new LongField("Date", review.getDate().getTime(),
				Field.Store.YES));
		ldoc.add(new TextField("TEXT", review.getText(), Field.Store.YES));
		ldoc.add(new TextField("TYPE", review.getType(), Field.Store.YES));
		ldoc.add(new StringField("BUSINESSID", review.getBusiness_id(),
				Field.Store.YES));

	}

	/**
	 * 
	 * prepare entry of lucene document to insert in the lucene index for tips
	 * 
	 * @param gson
	 * @param tip
	 * @param ldoc
	 * @param lineInFile
	 */
	public void prepareAndIndexTip(Gson gson, Tip tip, Document ldoc,
			String lineInFile) {
		tip = gson.fromJson(lineInFile, Tip.class);
		System.out.println("Indexing document: " + tip.getUser_id() + " "
				+ tip.getBusiness_id());
		ldoc.add(new TextField("USERID", tip.getUser_id(), Field.Store.YES));
		ldoc.add(new TextField("TEXT", tip.getText(), Field.Store.YES));
		ldoc.add(new TextField("BUSINESSID", tip.getBusiness_id(),
				Field.Store.YES));
		ldoc.add(new IntField("LIKES", tip.getLikes(), Field.Store.YES));
		ldoc.add(new LongField("Date", tip.getDate().getTime(), Field.Store.YES));
		ldoc.add(new TextField("TYPE", tip.getType(), Field.Store.YES));

	}

	/**
	 * 
	 * prepare entry of lucene document to insert in the lucene index for
	 * businesses
	 * 
	 * @param gson
	 * @param business
	 * @param ldoc
	 * @param lineInFile
	 */
	public void prepareAndIndexBusiness(Gson gson, Business business,
			Document ldoc, String lineInFile) {
		business = gson.fromJson(lineInFile, Business.class);
		System.out.println("Indexing document: " + business.getBusiness_id());
		ldoc.add(new StringField("BUSINESSID", business.getBusiness_id(),
				Field.Store.YES));
		ldoc.add(new TextField("FULL_ADDRESS", business.getFull_address(),
				Field.Store.YES));
		FieldType HoursField = new FieldType();
		HoursField.setOmitNorms(true);
		HoursField.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
		HoursField.setStored(true);
		HoursField.setTokenized(true);
		HoursField.setStoreTermVectors(true);
		HoursField.freeze();
		ldoc.add(new Field("HOURS", business.getHours().toString(), HoursField));
		ldoc.add(new IntField("OPEN", (business.isOpen() == true ? 1 : 0),
				Field.Store.YES));
		for (String str : business.getCategories()) {
			ldoc.add(new StringField("CATEGORIES", str, Field.Store.YES));
		}
		ldoc.add(new TextField("CITY", business.getCity(), Field.Store.YES));
		ldoc.add(new IntField("REVIEW_COUNT", business.getReview_count(),
				Field.Store.YES));
		ldoc.add(new TextField("NAME", business.getName(), Field.Store.YES));
		// DID NOT INDEX NEIGHBORHOODS
		ldoc.add(new DoubleField("LONGITUDE", business.getLongitutde(),
				Field.Store.YES));
		ldoc.add(new TextField("STATE", business.getState(), Field.Store.YES));
		ldoc.add(new DoubleField("STARS", business.getStars(), Field.Store.YES));
		ldoc.add(new DoubleField("LATITUDE", business.getLatitude(),
				Field.Store.YES));
		FieldType AttributesField = new FieldType();
		AttributesField.setOmitNorms(true);
		AttributesField.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
		AttributesField.setStored(true);
		AttributesField.setTokenized(true);
		AttributesField.freeze();
		ldoc.add(new Field("ATTRIBUTES", business.getAttributes().toString(),
				AttributesField));
		ldoc.add(new TextField("TYPE", business.getType(), Field.Store.YES));

	}

	public static void main(String[] args) {
		IndexData idata = new IndexData();
		idata.setIndexPath("C:\\searchproject\\yelp\\index");
		idata.setCorpusPath("/com/search/project/yelp/dataset/yelp_dataset_challenge_academic_dataset");

		// ////////////////// Earlier approach which took more time to extract
		// category features
		idata.generateIndex("review");
		idata.generateIndex("business");
		idata.generateIndex("tip");
		idata.generateCategoriesIndex(UtilFunctions.getCategoryFeaturesMap());

		// //////////////////
		// Final approach in which large data was put in mongodb and helped
		// faster category features extraction.
		idata.generateMongoTableIndex("New_training_set");
		idata.generateMongoTableIndex("New_test_set");
		HashMap<String, String> categoryFeaturesMap = UtilFunctions
				.getCategoryFeaturesMap();

		// //////////////////
		// Do POS tagging and get the nouns here and then pass it.
		HashMap<String, String> categoryFeatures = null;
		try {
			categoryFeatures = idata.posTagCategoryFeatures(UtilFunctions
					.getCategoryFeaturesMap());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
