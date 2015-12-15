package com.search.project.yelp.task2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


/**
 * @author Sameedha
 *
 * This is the entry point for Task 2 . Our objective is to find the most talked about features within a city.
 * For this, we have some pre-conditions to be met which are as follows -
 *  
 *  Create a new business file "restaurant_business.json" which has businesses which has "Restaurants" in their category.
 *  This can be done by executing TopAttributesFinder.createRestaurantBusinessFile()
 *  
 * "restaurants","reviewCollection" and "tipsCollection" should be created in MongoDB by importing their respective json files.
 * 
 *  Group these reviews and tips according to business Id by executing below command -
 *  db.reviewCollection.aggregate([{$group:{_id:"$business_id",reviews:{$push: {text:"$text"}}}},{$out:"BusinessReviewGroupCollection"}],{allowDiskUse:true})
 *  db.tipCollection.aggregate([{$group:{_id:"$business_id",tips:{$push: {text:"$text"}}}},{$out:"BusinessTipGroupCollection"}],{allowDiskUse:true})
 *  
 *  Create a new collection "BusinessReviewTipCollection" which has all the reviews and tips combined together along with their business Id.
 *  For the above requirement, execute DBManager.createReviewTipGroups(dbName).
 *  
 *  Create city indexes by executing CityIndexer.createIndexForCity("BusinessReviewTipCollection") which will divide the training and test data into 2:1 ratio by creating indexes for each city.
 *  For example, if a city 'A' has 3000(reviews + tips) , then 2000 (reviews + tips) will be indexed for training, whereas the other 1000 will be indexed for testing
 *  
 *  Finally, execute findTopAttributes() to get the top attributes for each city such that their score crosses a certain pre-set threshold value (different for each similarities).
 *  
 *  
 */
public class TopAttributesFinder {

	private final static String restaurants_category = "Restaurants";

	public static final float thresholdDefaultScore = 0.01f;

	public static final float thresholdBM25Score = 7.0f;

	public static final float thresholdLMScore = 3.0f;

	public static void main(String gg[]){

		try {
			
			//Generate indexes for all the cities
			CityIndexer.createIndexForCity("BusinessReviewTipCity");

			//For each city, find the most discussed features in reviews and tips which surpass the pre-set threshold value
			for(String city : Constants.cityList){
				findTopAttributes(city,Constants.trainIndexDir ,thresholdDefaultScore, new DefaultSimilarity());
				findTopAttributes(city,Constants.testIndexDir ,thresholdDefaultScore, new DefaultSimilarity());

				findTopAttributes(city,Constants.trainIndexDir ,thresholdBM25Score, new BM25Similarity());
				findTopAttributes(city,Constants.testIndexDir ,thresholdBM25Score, new BM25Similarity());

				findTopAttributes(city,Constants.trainIndexDir ,thresholdLMScore, new LMDirichletSimilarity());
				findTopAttributes(city,Constants.testIndexDir ,thresholdLMScore, new LMDirichletSimilarity());
			}
			 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	/**
	 * Creates a business json file that has businesses related to restaurant category
	 * @throws IOException 
	 * @throws org.json.simple.parser.ParseException 
	 */
	public static void createRestaurantBusinessFile() throws IOException, org.json.simple.parser.ParseException {
		JSONParser parser = new JSONParser();

		//Create object of FileReader
		FileReader inputFile = new FileReader(Constants.datasetLocation+"//"+Constants.businessFile);

		//Instantiate the BufferedReader Class
		BufferedReader bufferReader = new BufferedReader(inputFile);

		//Variable to hold the one line data
		String line;

		File file = new File(Constants.datasetLocation+"//"+Constants.restaurantBusinessFile);
		BufferedWriter  output = new BufferedWriter(new FileWriter(file));

		JSONObject restaurantObj = new JSONObject();
		JSONArray businessObjArray = new JSONArray(); 


		// Read file line by line and print on the console
		while ((line = bufferReader.readLine()) != null)   {

			Object obj = parser.parse(line);
			JSONObject jsonObj = (JSONObject)obj;
			JSONArray catArray = (JSONArray) jsonObj.get(Constants.CATEGORIES_KEY);

			if(catArray.contains(restaurants_category)){		    	
				businessObjArray.add(jsonObj);
			}


		}

		restaurantObj.put(restaurants_category.toLowerCase(), businessObjArray);
		output.write(restaurantObj.toJSONString());
		output.flush();
		output.close();
		bufferReader.close();





	}



	/**
	 * @param cityName
	 * @param indexDir
	 * @param thresholdScore
	 * @param sim
	 * @throws IOException
	 * @throws ParseException
	 * Finds top attributes for a given city which meet a threshold score and prints the ranked attributes on console
	 */
	private static void findTopAttributes(String cityName, String indexDir, float thresholdScore, Similarity sim) throws IOException, ParseException{

		System.out.println("Searching top features for "+cityName+ " in "+indexDir); 
		String index = indexDir+"\\"+cityName;
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));

		// Print the total number of documents in the corpus
		System.out.println("Total number of documents in the corpus: "+ reader.maxDoc());
		Map<String,Float> featureScores= new HashMap<String,Float>();

		for(Entry<String,String> queryEntry : Constants.featureMap.entrySet()){

			String queryString =queryEntry.getValue();
			System.out.println("Feature Word : "+queryEntry.getKey());


			IndexSearcher searcher = new IndexSearcher(reader);
			Analyzer analyzer = new StandardAnalyzer();
			searcher.setSimilarity(sim);
			QueryParser parser = new QueryParser("REVIEWSTIPS", analyzer);
			Query query = parser.parse(queryString);
			TopDocs results = searcher.search(query, 1000);
			int hitsAboveThreshold = 0;
			for (int i = 0; i < results.scoreDocs.length; i++) {

				if(results.scoreDocs[i].score > thresholdScore){
					hitsAboveThreshold++;
				}

			}
			float finalScore = (float)hitsAboveThreshold/reader.maxDoc();
			finalScore *= 100;

			if(finalScore > 30.0){
				featureScores.put(queryEntry.getKey(), finalScore);
			}

			System.out.println(hitsAboveThreshold + " relevant documents");
			System.out.println("=============================================");


		}

		reader.close();
		int rank = 1;
		//Sort the map and print
		Map<String, Float> sortedFeatureScores = sortByComparator(featureScores);

		for (Map.Entry<String, Float> entry: sortedFeatureScores.entrySet() ){								
			System.out.println(rank+".  "+entry.getKey()+" ====> "+entry.getValue());
			rank++;
		}			

	}


	/**
	 * @param : Map<String, Double> unsortMap : a Map which has to be sorted on keys
	 * @return: Map<String, Double> : sortedMap 
	 * 
	 * Function sorts a map on keys and returns the sorted map
	 * 
	 */	
	private static Map<String, Float> sortByComparator(Map<String, Float> unsortMap) {

		// Convert Map to List
		List<Map.Entry<String, Float>> list = 
				new LinkedList<Map.Entry<String, Float>>(unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {
			public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// Convert sorted map back to a Map
		Map<String, Float> sortedMap = new LinkedHashMap<String, Float>();
		for (Iterator<Map.Entry<String, Float>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Float> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

}

