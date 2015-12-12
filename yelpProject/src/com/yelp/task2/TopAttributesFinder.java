package com.yelp.task2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;

public class TopAttributesFinder {
	
	private final static String restaurants_category = "Restaurants";

	public static void main(String gg[]){
	
		
		
		/* Create restaurant business file
		 * 
		 * Import restaurant business file and reviews.json in MongoDB
		 * Now, we have "restaurants and "reviewCollection" in DB
		 * 
		 * Execute below command to create "BusinessReviewGroupCollection
		 * db.reviewCollection.aggregate([{$group:{_id:"$business_id",reviews:{$push: {text:"$text"}}}},{$out:"BusinessReviewGroupCollection"}],{allowDiskUse:true})
		 * 
		 * The above command will group the reviews according to business_ids.
		 * 
		 * Join the BusinessReviewGroupCollection and restaurants collection on their business_ids.
		 * This  creates a hierarchical model that comprises of each doc having business_id,city, categories, business_name and their reviews
		 * 
		 * createBusinessReviewCity() is executed for above process.
		 * 
		 * Now, we create train and test collection on basis of city_category information.
		 * Each doc will have "city_category" as id and business reviews as their value.
		 * 
		 * groupCitiesAndCategory() is executed for above process
		 *
		 * */
		
		System.setProperty("wordnet.database.dir", "C:\\Program Files (x86)\\WordNet\\2.1\\dict\\");
		
		/*String queryFeature = "desserts";
		
		
		
		List<String> relatedWords = generateQueryForFeature(queryFeature);
		System.out.println("Displaying Related words for "+queryFeature+" :");
		for (String relWord : relatedWords) {
			System.out.println(relWord);
		}*/
		try {
			calculateAttributeScores();
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		
	}

	/**
	 * Creates a business file that has businesses related to restaurant category
	 */
	private static void createRestaurantBusinessFile() {
		JSONParser parser = new JSONParser();
		
		try{
			
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
	          
		
			
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public static List<String> generateQueryForFeature(String featureWord){
		List<String> relatedWords = new ArrayList<String>();
		
		Set<String> relWordsSet = new HashSet<String>();
		
	   //  Get the synsets containing the feature word
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		Synset[] synsets = database.getSynsets(featureWord);
		
	   //  Display the word forms and definitions for synsets retrieved
				if (synsets.length > 0)
				{
					System.out.println("The following synsets contain '" +
							featureWord + "' or a possible base form " +
							"of that text:");
					for (int i = 0; i < synsets.length; i++)
					{
						
						String[] wordForms = synsets[i].getWordForms();
						String[] usageExamples = synsets[i].getUsageExamples();
						
						
						for (int j = 0; j < usageExamples.length; j++) {
							
							usageExamples[j] = usageExamples[j].replaceAll("\"", "");
							
							relWordsSet.addAll(Arrays.asList(usageExamples[j].split(" ")));
						}
						String definition = synsets[i].getDefinition();
						
						String[] definitionWords = definition.split(" ");
						
						relWordsSet.addAll(Arrays.asList(wordForms));
						
						relWordsSet.addAll(Arrays.asList(definitionWords));
						
					}
				}
				else
				{
					System.err.println("No synsets exist that contain " +
							"the word form '" + featureWord + "'");
				}
				
	   /*Iterator<String> itr = relWordsSet.iterator();
	   while(itr.hasNext()){
		   System.out.println(itr.next());
	   }*/
				
		//return (String[]) relWordsSet.toArray();
				relatedWords.addAll(relWordsSet);
				return relatedWords;
	}
	
  private static void createFeatureWordsFile() throws IOException{
	  
	  File file = new File(Constants.datasetLocation+"//"+Constants.featureMapFile);
      BufferedWriter  output = new BufferedWriter(new FileWriter(file));
      JSONArray outputArray = new JSONArray();
      for(Entry<String, String> feature : Constants.featureMap.entrySet()){
    	  String featureKey = feature.getKey();
    	  String featureSearchWord = feature.getValue();
    	  
    	  List<String> relatedWords = generateQueryForFeature(featureSearchWord);
    	  if(!relatedWords.contains(featureKey)){
    		  relatedWords.add(featureKey);
    	  }
    	  
    	  String searchQuery = String.join(" ", relatedWords);
    			  
    	  JSONObject jsonObj = new JSONObject();
    	  jsonObj.put("feature_name", featureKey);
    	  jsonObj.put("searchQuery", searchQuery);
    	  outputArray.add(jsonObj);
    	  
      }
      
      JSONObject outputObject = new JSONObject();
      outputObject.put("features", outputArray);
      
      output.write(outputObject.toJSONString());
      output.flush();
	  output.close();
		
	}
  
  private static void calculateAttributeScores() throws IOException, ParseException{
	  
	    Map<String,String> queryMap = new HashMap<String,String>();
	    queryMap.put("Wheelchair accessible","wheelchair accessible handicap");
	    queryMap.put("Happy Hour","drinks reduced  hour decreased lower discount");
	    queryMap.put("Caters","banquets host cater conference feast");
	    queryMap.put("Smoking","tobacco cigar hookah cigarette pipe fire lighter");
	    queryMap.put("Desserts","dessert icecream sweet cake confection");
	    queryMap.put("Casual Ambience","casual natural style interesting elegant Casual ambience informal");
	    queryMap.put("Take-Out","take-out to-go take-away pickup carryout");
	    queryMap.put("Price Range","price affordable expensive cost cheap worth bucks value purchase bill fare");
	    queryMap.put("Alcohol","liquor alcohol intoxicant brew alcoholic beverage bar pubs lounge");
	    queryMap.put("Music","jazz music rock melody  pop club dance Listening Singer band Beats ");
	    queryMap.put("Delivery","delivery to-go online-order pick-up quick ontime home-delivery parcel");
	    //queryMap.put("Good For Kids","kid play baby child colorful game young son daughter boy girl");
	    queryMap.put("Waiter Service","service maintenance clean serving waiter early friendly horrible speed slow");
	    queryMap.put("Accepts credit cards","credit card debit payment cash visa mastercard");
	    queryMap.put("Wi-Fi","wifi wi-fi internet signal wireless password network distant");
	    queryMap.put("Drive-Thru","drive-through drive-thru");
	    queryMap.put("Takes Reservation","booking reserve advance appointment waiting delay vacancy house-full occupied accommodation");
	    queryMap.put("Parking", "parking vehicle car bike space lot convenient garage");
	   
	    
	    Map<String,Float> featureScores= new HashMap<String,Float>();
	    
	    for(Entry<String,String> queryEntry : queryMap.entrySet()){
	    	
	    String queryString =queryEntry.getValue();
		String cityName = "Las Vegas";
		String index = Constants.trainIndexDir+"\\BusinessReviewTipCity\\"+cityName;
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get(index)));
		// Print the total number of documents in the corpus
		//System.out.println("city Name :"+cityName);
		
		//System.out.println(queryString);
		System.out.println("Total number of documents in the corpus: "
				+ reader.maxDoc());
		//System.out.println(reader.toString());

		// Terms vocabulary = MultiFields.getTerms(reader, "reviews");
		// System.out.println(vocabulary);
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();
		searcher.setSimilarity(new DefaultSimilarity());
		QueryParser parser = new QueryParser("REVIEWSTIPS", analyzer);
		Query query = parser.parse(queryString);
		//System.out.println("Searching for: " + query.toString("REVIEWSTIPS"));
		TopDocs results = searcher.search(query, 1000);
		// Print number of hits
		int numTotalHits = results.totalHits;
		
		float finalScore = 0;
		System.out.println(numTotalHits + " total matching documents");
		int topDocsCount = 100;
		for (int i = 0; i < results.scoreDocs.length; i++) {
			if(topDocsCount != 0){
				finalScore+= (results.scoreDocs[i].score);
			}
			topDocsCount--;
			
		}
		finalScore = (float)(finalScore/reader.maxDoc()) ;
		System.out.println("Score : "+finalScore);
		float percentScore = (float)numTotalHits/reader.maxDoc();
		percentScore *= 100;
		System.out.println("Percentage Score : "+percentScore);
		System.out.println("=============================================");
		
		if(finalScore > 0.01){
		featureScores.put(queryEntry.getKey(), finalScore);
		}
		// Print retrieved results
		/*ScoreDoc[] hits = results.scoreDocs;
		for (int i = 0; i < hits.length; i++) {
			Document doc = searcher.doc(hits[i].doc);
			//System.out.println("BUSINESSID: " + doc.get("BUSINESSID"));
		}*/
		
		//System.out.println(index);
		
		reader.close();
	    }
	    
	    
	    int rank = 1;
	    
	    //Sort the map and print
	    Map<String, Float> sortedFeatureScores = sortByComparator(featureScores);
		
		for (Map.Entry<String, Float> entry: sortedFeatureScores.entrySet() ){						
				
			System.out.println(rank+".  "+entry.getKey()+" ====> "+entry.getValue());
			//System.out.println(entry.getKey());
			/*if(rank > 9){
				break;
			}*/
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

