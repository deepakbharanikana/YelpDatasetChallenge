package com.search.project.yelp.task2;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * @author Sameedha
 * Database manager which creates tips and reviews collections to group them by business_ids
 */
public class DBManager {
	
	/**
	 * @return connectedDb
	 * @throws UnknownHostException
	 */
	public static DB getConnectedDB() throws UnknownHostException{
		
		MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
		DB db = mongoClient.getDB( "test" );	
		return db;
	}
		
	
	/**
	 * @param db
	 * Creates collection of reviews and tips grouped by their business IDs
	 */
	public static void createReviewTipGroups(DB db){
		
		// get a business collection
		DBCollection businessCollection = db.getCollection("restaurants");
		DBCollection reviewCollection = db.getCollection("BusinessReviewGroupCollection");
		DBCollection tipCollection = db.getCollection("BusinessTipGroupCollection");
		DBCollection newCollection = db.getCollection("BusinessReviewTipCity");
		BasicDBObject allQuery = new BasicDBObject();
		BasicDBObject fields = new BasicDBObject();
		fields.put( "business_id", 1);		
		fields.put("name", 1);
		fields.put("city", 1);
		fields.put("review_count", 1);
		fields.put("categories", 1);

		List<String> visitedBiz = new ArrayList<String>();
		DBCursor businessCursor = businessCollection.find(allQuery, fields);
		while(businessCursor.hasNext()) {
			BasicDBObject newDoc = new BasicDBObject();
			DBObject businessObject=businessCursor.next();
			String businessID = (String) businessObject.get("business_id");
			String bizName = (String) businessObject.get("name");
			String bizCity = (String) businessObject.get("city");			
			int review_count = (Integer) businessObject.get("review_count");
			List categories = (List) businessObject.get("categories");

			if(!visitedBiz.contains(businessID)){
				BasicDBObject whereQuery = new BasicDBObject();
				whereQuery.put( "_id", businessID);
				DBCursor cursor = reviewCollection.find(whereQuery);
				while(cursor.hasNext()) {
					newDoc.put("business_id", businessID);
					newDoc.put("name", bizName);
					newDoc.put("city", bizCity);
					newDoc.put("review_count", review_count);
					newDoc.put("categories", categories);
					DBObject rObject=cursor.next();
					List rev = (List) rObject.get("reviews");
					newDoc.put("reviews", rev);				 
				}
				DBCursor tipCursor = tipCollection.find(whereQuery);
				while(tipCursor.hasNext()) {				
					DBObject tipObject=tipCursor.next();
					List tip = (List) tipObject.get("tips");
					System.out.println(tip);
					newDoc.put("tips", tip);				 
				}
				newCollection.insert(newDoc);
				System.out.println("Added "+bizName);
				visitedBiz.add(businessID);
			}
			else
			{
				System.out.println("Business is already present "+businessID);
			}
		}

	}

}
