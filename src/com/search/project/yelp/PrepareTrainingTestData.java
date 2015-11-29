package com.search.project.yelp;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.search.project.yelp.datatypes.Business;
import com.search.project.yelp.datatypes.Review;
import com.search.project.yelp.datatypes.Tip;

public class PrepareTrainingTestData {

	private MongoClient mongoClient;
	private MongoDatabase db;
	private MongoCollection<Document> trainingCollection;
	private MongoCollection<Document> testCollection;

	PrepareTrainingTestData() {
		this.mongoClient = new MongoClient("localhost", 27017);
		this.db = mongoClient.getDatabase("yelp");
		this.trainingCollection = db.getCollection("training_set");
		this.testCollection = db.getCollection("test_set");
		trainingCollection.drop();
		testCollection.drop();
	}

	public void insertData() {
		MongoCollection<Document> tableBusiness = db.getCollection("business");
		MongoCollection<Document> tableReview = db.getCollection("review");
		MongoCollection<Document> tableTip = db.getCollection("tip");
		DBObject projectionString = new BasicDBObject("_id", 0)
				.append("business_id", 1).append("categories", 1)
				.append("name", 1);
		MongoCursor<Document> businessCursor = tableBusiness.find().iterator();
		List<Document> trainingDocuments = new ArrayList<Document>();
		List<Document> testDocuments = new ArrayList<Document>();
		List trainingReviews = new ArrayList();
		List testReviews = new ArrayList();
		List trainingTips = new ArrayList();
		List testTips = new ArrayList();

		while (businessCursor.hasNext()) {
			Document businessRow = businessCursor.next();
			Business busDoc = Business.jsonStringToBusiness(businessRow
					.toJson());
			Document trainingDoc = new Document();
			trainingDoc.put("business_id", busDoc.getBusiness_id());
			Document testDoc = new Document();
			testDoc.put("business_id", busDoc.getBusiness_id());

			// get all the reviews of the current business ID
			Document querySelCrit = new Document("business_id", new Document(
					"$eq", busDoc.getBusiness_id()));
			MongoCursor<Document> reviewCursor = tableReview.find(querySelCrit)
					.iterator();
			List reviews = new ArrayList();
			int ctr = 0;
			while (reviewCursor.hasNext()) {
				ctr++;
				Document reviewRow = reviewCursor.next();
				Review reviewDoc = Review
						.jsonStringToReview(reviewRow.toJson());
				// put 33% reviews of this business in test and 66% in training
				if (ctr % 3 == 0) {
					testReviews.add((String) reviewDoc.getText());
				} else {
					trainingReviews.add((String) reviewDoc.getText());
				}
			}

			// get all the reviews of the current business ID
			MongoCursor<Document> tipCursor = tableTip.find(querySelCrit)
					.iterator();
			List tips = new ArrayList();
			ctr = 0;
			while (tipCursor.hasNext()) {
				ctr++;
				Document tipRow = tipCursor.next();
				Tip tipDoc = Tip.jsonStringToTip(tipRow.toJson());
				// put 33% tips of this business in test and 66% in training
				if (ctr % 3 == 0) {
					testTips.add((String) tipDoc.getText());
				} else {
					trainingTips.add((String) tipDoc.getText());
				}
			}

			trainingDoc.append("reviews", trainingReviews);
			testDoc.append("reviews", testReviews);
			trainingDoc.append("tips", trainingTips);
			testDoc.append("tips", testTips);
			trainingCollection.insertOne(trainingDoc);
			testCollection.insertOne(testDoc);

		}

	}

	public static void main(String[] args) {
		PrepareTrainingTestData pt = new PrepareTrainingTestData();
		pt.insertData();
	}

}
