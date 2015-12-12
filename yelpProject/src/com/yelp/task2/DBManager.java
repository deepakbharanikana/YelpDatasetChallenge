package com.yelp.task2;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class DBManager {

	public static void main(String[] args) {
		try{
			DB testDB = getConnectedDB();

			long startTime = System.currentTimeMillis();

		    //groupCitiesAndCategory(testDB);
			
			createReviewTipGroups(testDB);
			long stopTime = System.currentTimeMillis();
			
			long elapsedTime = stopTime - startTime;
		    System.out.println("Total time taken : "+elapsedTime);
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public static DB getConnectedDB() throws UnknownHostException{
		
		MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
		DB db = mongoClient.getDB( "test" );
		
		return db;
	}
	
	
	public static void groupCitiesAndCategory(DB db){
		
		DBCollection businessReviewCityCollection = db.getCollection("BusinessReviewTipCity");
		
		DBCollection opTrainCollection = db.getCollection("cityCategoryTrainCollection");
		DBCollection opTestCollection = db.getCollection("cityCategoryTestCollection");
		
		
		for(String city :Constants.cityList){
			//for(String category : Constants.categoryList){
				//String outputDocKey = city+"_"+category;
				//DBCollection outputCollection = db.getCollection(outputDocKey);
			
			String outputDocKey = city;
				BasicDBObject outputTrainObj = new BasicDBObject();
				BasicDBObject outputTestObj = new BasicDBObject();
				
				outputTrainObj.put("_id", outputDocKey);
				outputTestObj.put("_id", outputDocKey);
		        
		        //String[] catArray = {category};		        
		        //$match : { $and:[{city : "Carnegie"},{categories:{$in:["Pizza"]}}]}
		        List<BasicDBObject> criteria = new ArrayList<BasicDBObject>();
		        criteria.add(new BasicDBObject(Constants.CITY_KEY, city));
		        //criteria.add(new BasicDBObject(Constants.CATEGORIES_KEY, new BasicDBObject("$in", catArray)));
		        DBObject andObj = new BasicDBObject("$and",criteria);
		        DBObject match = new BasicDBObject("$match", andObj);
		     
		        
		        DBObject groupObj = new BasicDBObject( "_id", city);
		        groupObj.put("total", new BasicDBObject( "$sum", 1));
		       
		        
		        
		        //$group : { _id : "$city", total : { $sum : 1 } ,BusinessReviewCity:{$push:{business_id:"$business_id",reviews:"$reviews",name:"$name"}} }
		       
		        
		        DBObject pushObj = new BasicDBObject(Constants.BUSINESS_ID_KEY,"$business_id");
		        pushObj.put(Constants.NAME_KEY,"$name");
		        pushObj.put(Constants.REVIEWS_KEY,"$reviews");
		        pushObj.put("tips","$tips");
		        
		        
		        groupObj.put(Constants.BUSINESSES_KEY, new BasicDBObject("$push", pushObj));
		        
		        DBObject group = new BasicDBObject("$group", groupObj);
		        
		        
		        List<DBObject> dbObjects = new ArrayList<DBObject>();
		        dbObjects.add(match);
		        dbObjects.add(group);
		        
		        
		        AggregationOutput output = businessReviewCityCollection.aggregate( dbObjects);
		        
		        Iterator<DBObject> resultItr = output.results().iterator();
		        while(resultItr.hasNext()){
		          DBObject resultObj = resultItr.next();
		          //outputCollection.insert(resultObj); 
		          // System.out.println(resultObj);
		           int totalCount = (int) resultObj.get("total");
		           
		           int  counter = (int) Math.floor((0.6 * totalCount));
		           List<DBObject> resultBizList = (List<DBObject>) resultObj.get(Constants.BUSINESSES_KEY);
		          
		           
		           Object[] trainArray =  Arrays.copyOfRange(resultBizList.toArray(), 0, counter + 1);
		           Object[] testArray =  Arrays.copyOfRange(resultBizList.toArray(), counter + 1, resultBizList.size());
		           /*System.out.println("==============Train============== ");
		           System.out.println(Arrays.asList(trainArray));
		           System.out.println("==============Test============== ");
		           System.out.println(Arrays.asList(testArray));*/
		           //System.out.println(resultBizList.toString());
		          //outputTrainObj.put(outputDocKey,resultObj);
		           
		           
		           outputTrainObj.put(Constants.BUSINESSES_KEY,Arrays.asList(trainArray ));
		           outputTestObj.put(Constants.BUSINESSES_KEY,Arrays.asList(testArray ));
		           
		           opTrainCollection.insert(outputTrainObj);
		           opTestCollection.insert(outputTestObj);
		        }
	  
		        System.out.println("Training Collection created : "+opTrainCollection);
		        System.out.println("Testing Collection created : "+opTestCollection);
			//}
		}
		
	}
	
	public static void createReviewTipGroups(DB db){
		
		// get a business collection
		DBCollection businessCollection = db.getCollection("restaurants");//toallbiz
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

		//fields.put(  "_id", 0);

		List<String> visitedBiz = new ArrayList<String>();
		DBCursor businessCursor = businessCollection.find(allQuery, fields);
		while(businessCursor.hasNext()) {
			BasicDBObject newDoc = new BasicDBObject();
			//System.out.println("biz");
			// System.out.println(businessCursor.next());
			DBObject businessObject=businessCursor.next();
			String businessID = (String) businessObject.get("business_id");
			String bizName = (String) businessObject.get("name");
			String bizCity = (String) businessObject.get("city");			
			int review_count = (Integer) businessObject.get("review_count");
			List categories = (List) businessObject.get("categories");
			//List attributes = (List) businessObject.get("attributes");

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
					//System.out.println(rev);
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
				System.out.println("Biz is already present "+businessID);
			}
		}

	}

}
