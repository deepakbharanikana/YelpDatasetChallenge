/**
 * @author Milind Gokhale
 * This code extracts data from the individual reviewGroup and tipGroup collections and 
 * creates a training and test set containing 
 * businessID, yelp assigned categories, all reviews and all tips.
 * 
 * TrainingSet and TestSet table structure
 * [BusinessID, Categories, Reviews, Tips]
 * 
 * Date : November 28, 2015
 */

package com.search.project.yelp.task1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.search.project.yelp.task1.datatypes.Business;

/**
 * @author Milind
 * @info Class to prepare training and test set.
 */
public class PrepareTrainingTestData {

	private MongoClient mongoClient;
	private MongoDatabase db;
	private MongoCollection<Document> trainingCollection;
	private MongoCollection<Document> testCollection;

	/**
	 * Constructor which prepares mongodb connection and tables
	 */
	PrepareTrainingTestData() {
		this.mongoClient = new MongoClient("localhost", 27017);
		this.db = mongoClient.getDatabase("yelp");
		this.trainingCollection = db.getCollection("New_training_set");
		this.testCollection = db.getCollection("New_test_set");
	}

	/**
	 * This function connects to mongodb, extracts 
	 * 		1. businessID, yelp assigned categories from business collection 
	 * 		2. reviews of business from ReviewGroupCollection table 
	 * 		3. tips of business from TipGroupCollection table 
	 * 	and then 
	 * 		inserts each businesses entry with this collected data into training and test sets. 
	 * The partitioning strategy for the preparation of training set is: 
	 * 		33% test : 66% training 
	 * 
	 */
	public void insertData() {
		MongoCollection<Document> tableBusiness = db.getCollection("business");
		MongoCollection<Document> tableReview = db
				.getCollection("ReviewGroupCollection");
		MongoCollection<Document> tableTip = db
				.getCollection("TipGroupCollection");
		DBObject projectionString = new BasicDBObject("_id", 0)
				.append("business_id", 1).append("categories", 1)
				.append("name", 1);
		MongoCursor<Document> businessCursor = tableBusiness.find().iterator();

		int ctr = 0;

		while (businessCursor.hasNext()) {
			ctr++;

			Document businessRow = businessCursor.next();
			Business busDoc = Business.jsonStringToBusiness(businessRow
					.toJson());

			// Check if the business document already exists in the training and
			// test set and insert or process only if it doesnt already exist
			// this will help in incremental insert design for running the 
			// function multiple times
			boolean insertFlag = false;
			Document querySelCrit = new Document("business_id", new Document(
					"$eq", busDoc.getBusiness_id()));
			MongoCursor<Document> existingDocInTrain = trainingCollection.find(
					querySelCrit).iterator();
			MongoCursor<Document> existingDocInTest = testCollection.find(
					querySelCrit).iterator();
			if (!existingDocInTest.hasNext() && !existingDocInTrain.hasNext()) {
				insertFlag = true;
			}

			if (insertFlag) {
				Document doc = new Document();
				doc.put("business_id", busDoc.getBusiness_id());
				doc.put("categories", Arrays.asList(busDoc.getCategories()));

				// get all the reviews of the current business ID
				querySelCrit = new Document("_id", new Document("$eq",
						busDoc.getBusiness_id()));
				MongoCursor<Document> reviewCursor = tableReview.find(
						querySelCrit).iterator();
				List<String> reviews = null;
				while (reviewCursor.hasNext()) {
					Document reviewRow = reviewCursor.next();
					reviews = (List<String>) reviewRow.get("text");
				}

				// get all the tips of the current business ID
				MongoCursor<Document> tipCursor = tableTip.find(querySelCrit)
						.iterator();
				List<String> tips = new ArrayList();
				while (tipCursor.hasNext()) {
					Document tipRow = tipCursor.next();
					tips = (List<String>) tipRow.get("text");
				}

				doc.put("reviews", reviews);
				doc.put("tips", tips);

				// put every 3rd entry in the test set for even distribution of
				// data rather than contiguous data.
				if (ctr % 3 != 0) {
					trainingCollection.insertOne(doc);
				} else {
					testCollection.insertOne(doc);
				}
			}
		}
	}

	public static void main(String[] args) {
		PrepareTrainingTestData pt = new PrepareTrainingTestData();
		pt.insertData();
		pt.mongoClient.close();
	}

}
