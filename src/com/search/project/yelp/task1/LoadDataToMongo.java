/**
 * @author Milind Gokhale
 * This code extracts data from the file and inserts into a collection in mongodb.
 * Date : November 28, 2015
 */

package com.search.project.yelp.task1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bson.Document;

import com.google.gson.JsonSyntaxException;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.search.project.yelp.task1.datatypes.Business;

/**
 * @author Milind
 *
 *         Class to load the data from json files into mongodb.
 *
 */
public class LoadDataToMongo {
	private String corpusPath; // corpus path to load the json file
	private int rowCount; // row count for the report string
	private String tableName; // table name to populate in mongodb
	private String inputFileName; // input file name
	private String reportString;

	public LoadDataToMongo(String path) {
		setCorpusPath(path);
		reportString = "";
	}

	public String getCorpusPath() {
		return corpusPath;
	}

	public void setCorpusPath(String corpusPath) {
		this.corpusPath = corpusPath;
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getInputFileName() {
		return inputFileName;
	}

	public void setInputFileName(String inputFileName) {
		this.inputFileName = inputFileName;
	}

	public String getReportString() {
		return reportString;
	}

	public void setReportString(String reportString) {
		this.reportString = reportString;
	}

	public static void main(String[] args) {
		LoadDataToMongo ld = new LoadDataToMongo(
				UtilFunctions.getMySourcePath()
						+ "/com/search/project/yelp/dataset/yelp_dataset_challenge_academic_dataset/");
		// long startTime = System.currentTimeMillis();
		// ld.setReportString(ld.getReportString() + startTime + "\n");
		ld.loadDataToCollection("business",
				"yelp_academic_dataset_business.json");
		ld.loadDataToCollection("review", "yelp_academic_dataset_review.json");
		ld.loadDataToCollection("tip", "yelp_academic_dataset_tip.json");

		// long endTime = System.currentTimeMillis();
		// ld.setReportString(ld.getReportString() + endTime + "\n");
		// ld.setReportString(ld.getReportString() + "It took "
		// + (endTime - startTime) + " milliseconds \n");
		ld.printReport();
	}

	/**
	 * This function prints the report of the execution process.
	 */
	public void printReport() {
		try {
			FileWriter fw = new FileWriter(UtilFunctions.getMySourcePath()
					+ "/com/search/project/yelp/OutputFiles/"
					+ this.getClass().getName().toString() + "_Report.txt");
			fw.write(reportString);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This function loads table containing categories and their reviews and
	 * tips in mongodb.
	 */
	public void loadCategoryReviewTipsMap() {
		MongoClient mongo = new MongoClient("localhost", 27017);
		MongoDatabase db = mongo.getDatabase("yelp");
		MongoCollection<Document> category_reviewstips_table = db
				.getCollection("category_reviewstips");
		category_reviewstips_table.drop();

		MongoCollection<Document> businesstable = db.getCollection("business");
		reportString += "dropped table " + "category_reviewtips" + "\n";
		HashSet<String> catList = Business.getCategoriesList();

		// Select business ids for the categories
		for (String category : catList) {
			System.out.println("Ongoing Category: " + category);
			String reviews_and_tips = "";
			Document businessInQuery = new Document();
			List<String> list = new ArrayList<String>();
			list.add(category);
			businessInQuery.put("categories", new Document("$in", list));
			MongoCursor<Document> businessCursor = businesstable.find(
					businessInQuery).iterator();
			while (businessCursor.hasNext()) { // iterating on each business id
				Document business = businessCursor.next();
				String businessID = (String) business.get("business_id");

				// select reviews of business id given
				Document reviewWhereQuery = new Document();
				reviewWhereQuery.put("business_id", businessID);
				MongoCursor<Document> reviewCursor = db.getCollection("review")
						.find(reviewWhereQuery).iterator();
				while (reviewCursor.hasNext()) {
					Document review = reviewCursor.next();
					String reviewText = (String) review.get("text");
					// append reviews altogether
					reviews_and_tips += " " + reviewText;
				}

				// select tips of given business id
				Document tipWhereQuery = new Document();
				reviewWhereQuery.put("business_id", businessID);
				MongoCursor<Document> tipCursor = db.getCollection("review")
						.find(tipWhereQuery).iterator();
				while (tipCursor.hasNext()) {
					Document tip = tipCursor.next();
					String tipText = (String) tip.get("text");
					// append tips altogether after reviews
					reviews_and_tips += " " + tipText;
				}

				System.out.println("Business " + businessID + " processed.");
			}

			// put it in table category_reviewstips
			Document categoryDoc = new Document();
			categoryDoc.put("category", category);
			categoryDoc.append("reviewstips", reviews_and_tips);
			category_reviewstips_table.insertOne(categoryDoc);
		}
	}

	/**
	 * 
	 * This function reads data from input file and put it in a table in mongodb
	 * 
	 * @param tableName
	 *            : target table to insert the data in mongodb
	 * @param inputFileName
	 *            : input json file from which to extract data
	 */
	private void loadDataToCollection(String tableName, String inputFileName) {
		setTableName(tableName);
		setInputFileName(inputFileName);

		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase db = mongoClient.getDatabase("yelp");
		MongoCollection<Document> table = db.getCollection(tableName);
		// truncate the table
		table.drop();
		reportString += "dropped table " + tableName + "\n";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(getCorpusPath() + "/"
					+ inputFileName));
			String thisline = "";
			int i = 0;
			Document row;

			while ((thisline = br.readLine()) != null) {
				i++;
				row = Document.parse(thisline);
				table.insertOne(row);
			}
			br.close();
			setRowCount(i);
			reportString += "Inserted " + getRowCount() + " rows in table "
					+ getTableName() + " from file " + getInputFileName()
					+ "\n";
		} catch (JsonSyntaxException | IOException e) {
			e.printStackTrace();
		}
		mongoClient.close();
	}
}
