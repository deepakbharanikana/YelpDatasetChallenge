package com.search.project.yelp.task2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * @author Sameedha 
 *
 * Constants file which holds static string for file locations, populates the selected cities to be indexed and creates a feature query map
 */
public class Constants {
	public static final String BUSINESS_ID_KEY = "business_id";

	public static final String BUSINESSES_KEY = "businesses";

	public static final String businessFile = "yelp_academic_dataset_business.json";

	public static final String CATEGORIES_KEY = "categories";

	public static final String CITY_KEY = "city";

	public static List<String> cityList = new ArrayList<String>();

	public static final String datasetLocation = System.getProperty("user.dir")+"\\src\\data";

	public static Map<String,String> featureMap = new HashMap<String,String>();

	public static final String featureMapFile = "featureMap.json";

	public static final String NAME_KEY = "name";

	public static final String restaurantBusinessFile = "restaurant_business.json";

	public static final String REVIEWS_KEY = "reviews";

	public static final String reviewsFile = "yelp_academic_dataset_review.json";

	public static final String testIndexDir = System.getProperty("user.dir")+"\\src\\index\\test";

	public static final String trainIndexDir = System.getProperty("user.dir")+"\\src\\index\\train";

	static {

		//populate city list
		cityList.add("Charlotte");
		cityList.add("Las Vegas");
		cityList.add("Edinburgh");
		cityList.add("Montreal");
		cityList.add("Pittsburgh");	
		cityList.add("Phoenix");
		cityList.add("Madison");
		cityList.add("Glendale");
		cityList.add("Gilbert");
		cityList.add("Scottsdale");
		cityList.add("Tempe");
		cityList.add("Mesa");

		//Generate feature Map json file using Word2Vec
		//getSearchQueryForAttributes();
		
		try{
			FileReader inputFile = new FileReader(Constants.datasetLocation+"//"+Constants.featureMapFile);
			BufferedReader bufferReader = new BufferedReader(inputFile);
			StringBuilder sb = new StringBuilder();
			String line;
			while((line = bufferReader.readLine()) != null){
				sb.append(line);
			}
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(sb.toString());
			JSONObject jsonObj = (JSONObject)obj;
			JSONArray featArray = (JSONArray) jsonObj.get("features");
			for (int i = 0; i < featArray.size(); i++) {
				JSONObject featObj = (JSONObject) featArray.get(i);
				featureMap.put((String)featObj.get("feature_name"),(String) featObj.get("searchQuery"));
			}
			bufferReader.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}

	}

	/**
	 * Uses Word2Vec APIs to generate related words for each attribute and save it in featureMap json
	 *//*
	public static void getSearchQueryForAttributes(){
		String filePath = "C:\\Fall2015\\Search\\Project\\yelp_dataset_challenge_academic_dataset\\yelp_dataset_challenge_academic_dataset\\PhoenixReviewTips.txt";

		System.out.println("Load & Vectorize Sentences....");
		// Strip white space before and after for each line
		SentenceIterator iter = UimaSentenceIterator.createWithPath(filePath);

		// Split on white spaces in the line to get words
		TokenizerFactory t = new DefaultTokenizerFactory();
		t.setTokenPreProcessor(new CommonPreprocessor());

		InMemoryLookupCache cache = new InMemoryLookupCache();
		WeightLookupTable table = new InMemoryLookupTable.Builder()
				.vectorLength(100)
				.useAdaGrad(false)
				.cache(cache)
				.lr(0.025f).build();

		System.out.println("Building model....");
		Word2Vec vec = new Word2Vec.Builder()
				.minWordFrequency(10)
				.iterations(1)
				.epochs(1)
				.layerSize(100)
				.lookupTable(table)
				.vocabCache(cache)
				.seed(42)
				.windowSize(5)
				.iterate(iter)
				.tokenizerFactory(t)
				.build();

		System.out.println("Fitting Word2Vec model....");
		vec.fit();

		List<String> featureWords = new ArrayList<String>();
		featureWords.add("alcohol");
		featureWords.add("service");
		featureWords.add("music");
		featureWords.add("reservation");
		featureWords.add("delivery");
		featureWords.add("ambience");
		featureWords.add("smoking");
		featureWords.add("wifi");
		featureWords.add("drive-thru");
		featureWords.add("desserts");
		featureWords.add("discounts");
		featureWords.add("credit");
		featureWords.add("wheelchair");
		featureWords.add("romantic");
		featureWords.add("affordable");
		featureWords.add("expensive");
		featureWords.add("price");
		featureWords.add("lunch");
		featureWords.add("dinner");

		File featurMapFile = new File("C:\\Fall2015\\Search\\Project\\yelp_dataset_challenge_academic_dataset\\yelp_dataset_challenge_academic_dataset\\featureMap.json");
		BufferedWriter  featureMapOutput = new BufferedWriter(new FileWriter(featurMapFile));
		JSONArray outputArray = new JSONArray();

		for(String featureWord : featureWords){
			List<String> wordsList = (List<String>) vec.wordsNearest(featureWord, 20);		
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("feature_name", featureWord);
			String searchQuery = String.join(" ", wordsList);
			jsonObj.put("searchQuery", searchQuery);
			outputArray.add(jsonObj);

			System.out.println("Closest words to "+featureWord+" on 1st run: " + wordsList);
			System.out.println("======================================================================");
		}

		JSONObject outputObject = new JSONObject();
		outputObject.put("features", outputArray);

		featureMapOutput.write(outputObject.toJSONString());
		featureMapOutput.flush();
		featureMapOutput.close();
	}*/
}
