package com.search.project.yelp.task2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;



public class CityIndexer {


	/**
	 * @param tableName
	 * Creates separate train and test indexes for all cities by reading through mongodb collection
	 */
	public static void createIndexForCity(String tableName) {
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase db = mongoClient.getDatabase("test");
		MongoCollection<org.bson.Document> table = db.getCollection(tableName);

		BasicDBObject cityQueryfield = new BasicDBObject();

		for(String cityName : Constants.cityList){

			cityQueryfield.put("city", cityName);
			MongoCursor<org.bson.Document> cursor = table.find(cityQueryfield).iterator();
			try {
				File reviewFile = new File(Constants.datasetLocation+"//"+cityName+"ReviewTips.txt");
				BufferedWriter  reviewTipOutput = new BufferedWriter(new FileWriter(reviewFile));

				Analyzer analyzer = new StandardAnalyzer();
				IndexWriterConfig trainIwc = new IndexWriterConfig(analyzer);
				trainIwc.setOpenMode(OpenMode.CREATE);
				Directory trainDir = FSDirectory.open(Paths.get(Constants.trainIndexDir + "\\" +cityName));
				IndexWriter trainWriter =  new IndexWriter(trainDir, trainIwc);

				IndexWriterConfig testIwc = new IndexWriterConfig(analyzer);
				testIwc.setOpenMode(OpenMode.CREATE);
				Directory testDir = FSDirectory.open(Paths.get(Constants.testIndexDir + "\\" +cityName));
				IndexWriter testWriter =  new IndexWriter(testDir, testIwc);

				int docCount = 1;
				while (cursor.hasNext()) {
					// Create lucene document and add to index
					org.bson.Document cityDoc = cursor.next();

					Document ldoc = new Document();// for lucene doc

					ldoc.add(new StringField("BUSINESSID", (String) cityDoc
							.get("business_id"), Field.Store.YES));

					ArrayList<org.bson.Document> reviews = (ArrayList<org.bson.Document>) cityDoc.get("reviews");


					if (reviews != null) {
						for (org.bson.Document revDoc : reviews) {
							ldoc.add(new TextField("REVIEWSTIPS", revDoc.getString("text"),
									Field.Store.YES));
							reviewTipOutput.write(revDoc.getString("text"));
						}
					}
					ArrayList<org.bson.Document> tips = (ArrayList<org.bson.Document>) cityDoc.get("tips");
					if (tips != null) {
						for (org.bson.Document tipDoc : tips) {
							ldoc.add(new TextField("REVIEWSTIPS", tipDoc.getString("text"),
									Field.Store.YES));
							reviewTipOutput.write(tipDoc.getString("text"));
						}
					}
					if(docCount % 3 != 0){
						trainWriter.addDocument(ldoc);
					}else{
						testWriter.addDocument(ldoc);
					}
					docCount++;
				}// while
				System.out.println("Total reviews and tips for city "+cityName+" : "+docCount);
				trainWriter.forceMerge(1);
				trainWriter.commit();
				trainWriter.close();

				testWriter.forceMerge(1);
				testWriter.commit();
				testWriter.close();
				reviewTipOutput.flush();
				reviewTipOutput.close();


			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		mongoClient.close();

	}
}
