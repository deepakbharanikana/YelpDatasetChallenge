package com.yelp.task2;

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
private String trainIndexPath;
	
	private String corpusPath;
	private String sourcePath;

	public CityIndexer() {
		// setSourcePath(UtilFunctions.getMySourcePath());
	}

	public String getIndexPath() {
		return trainIndexPath;
	}

	public void setIndexPath(String indexPath) {
		this.trainIndexPath = indexPath;
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

	private void generateCityMongoTableIndex(String tableName) {
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase db = mongoClient.getDatabase("test");
		MongoCollection<org.bson.Document> table = db.getCollection(tableName);
		
		
		
		BasicDBObject cityQueryfield = new BasicDBObject();
		
		for(String cityName : Constants.cityList){
			//String cityName="Phoenix";
			cityQueryfield.put("city", cityName);
			MongoCursor<org.bson.Document> cursor = table.find(cityQueryfield)
					.iterator();
	        
			
			try {

				
				/*//Test collection 
				MongoCollection<org.bson.Document> testCollection = db.getCollection("BusinessReviewTipCityTest");
				*/
				
				Analyzer analyzer = new StandardAnalyzer();
				IndexWriterConfig trainIwc = new IndexWriterConfig(analyzer);
				trainIwc.setOpenMode(OpenMode.CREATE);
				Directory trainDir = FSDirectory.open(Paths.get(Constants.trainIndexDir + "\\" + tableName+"\\"+cityName));
				IndexWriter trainWriter =  new IndexWriter(trainDir, trainIwc);
				
				IndexWriterConfig testIwc = new IndexWriterConfig(analyzer);
				testIwc.setOpenMode(OpenMode.CREATE);
				Directory testDir = FSDirectory.open(Paths.get(Constants.testIndexDir + "\\" + tableName+"\\"+cityName));
				IndexWriter testWriter =  new IndexWriter(testDir, testIwc);
				
	            int docCount = 1;
				while (cursor.hasNext()) {
					// Form lucene document and add to index
					org.bson.Document cityDoc = cursor.next();

					
					
						Document ldoc = new Document();// for lucene doc

						// String businessID = (String) cityDoc.get("businesses");
						// List<org.bson.Document> businessesList =
						// (List<org.bson.Document>) cityDoc
						// .get("businesses");

						ldoc.add(new StringField("BUSINESSID", (String) cityDoc
								.get("business_id"), Field.Store.YES));

						ArrayList<org.bson.Document> reviews = (ArrayList<org.bson.Document>) cityDoc
								.get("reviews");
						

						if (reviews != null) {
							for (org.bson.Document revDoc : reviews) {
								ldoc.add(new TextField("REVIEWSTIPS", revDoc.getString("text"),
										Field.Store.YES));
							}
						}
						ArrayList<org.bson.Document> tips = (ArrayList<org.bson.Document>) cityDoc
								.get("tips");
						if (tips != null) {
							for (org.bson.Document tipDoc : tips) {
								//System.out.println(tipDoc.getString("text"));
								ldoc.add(new TextField("REVIEWSTIPS", tipDoc.getString("text"),
										Field.Store.YES));
							}
						}
		                 if(docCount % 3 != 0){
		                	 trainWriter.addDocument(ldoc);
		                 }else{
		                	 testWriter.addDocument(ldoc);
		                 }
					docCount++;
				}// while
	            System.out.println("Total doc count : "+docCount);
				trainWriter.forceMerge(1);
				trainWriter.commit();
				trainWriter.close();
				
				testWriter.forceMerge(1);
				testWriter.commit();
				testWriter.close();
				

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		mongoClient.close();
		
	}

	public static void main(String[] args) {
		CityIndexer ed = new CityIndexer();
		ed.setIndexPath("C:\\Fall2015\\Search\\Project\\Index");
		//ed.setCorpusPath("/com/search/project/yelp/dataset/yelp_dataset_challenge_academic_dataset");
		// ed.generateIndex("review");
		// ed.generateIndex("business");
		// ed.generateIndex("tip");
		ed.generateCityMongoTableIndex("BusinessReviewTipCity");
		// ed.generateMongoTableIndex("trainCollection");

	}

}
