package tut.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

public class MongoDBHelloWorld {

	public static void main(String[] args) {
		MongoClient mongo = new MongoClient("localhost", 27017);
		MongoDatabase db = mongo.getDatabase("mkyong");
		List<String> dbs = mongo.getDatabaseNames();
		System.out.println("Showing list of databases:");
		System.out.println(dbs);
		MongoIterable<String> databases = mongo.listDatabaseNames();
		for (String str : databases) {
			System.out.println(str);
		}
		System.out.println("Showing list of collections in db " + db.getName()
				+ ":");
		MongoIterable<String> collections = db.listCollectionNames();
		for (String str : collections) {
			System.out.println(str);
		}
		MongoCollection<Document> users = db.getCollection("users");
		// Document document = new Document();
		// document.put("name", "mkyong");
		// document.put("age", 30);
		// document.put("createdDate", new Date());
		// users.insertOne(document);
		List<Document> foundDocument = users.find().into(
				new ArrayList<Document>());
		for (Document doc : foundDocument) {
			System.out.println(doc);
		}

		// try inserting a business into the users table
		Document document = new Document("x", 1);
		// users.insertOne(document);
		document.append("y", 2).append("BID", "2234fs");
		users.insertOne(document);

		foundDocument = users.find().into(new ArrayList<Document>());
		for (Document doc : foundDocument) {
			System.out.println(doc);
		}

		Document delQuery = new Document();
		delQuery.put("x", 1);
		users.deleteMany(delQuery);
	}
}
