package com.yelp.task2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {
public static final String datasetLocation = "C:\\Fall2015\\Search\\Project\\yelp_dataset_challenge_academic_dataset\\yelp_dataset_challenge_academic_dataset";
	
	public static final String businessFile = "yelp_academic_dataset_business.json";
	
	public static final String trainIndexDir = "C:\\Fall2015\\Search\\Project\\TrainIndex";
	
	public static final String testIndexDir = "C:\\Fall2015\\Search\\Project\\TestIndex";
	
	public static final String restaurantBusinessFile = "restaurant_business.json";
	
	public static final String reviewsFile = "yelp_academic_dataset_review.json";
	
	public static final String featureMapFile = "featureMap.json";
	
	public static final String CATEGORIES_KEY = "categories";
	
	public static final String CITY_KEY = "city";
	
	public static final String BUSINESS_ID_KEY = "business_id";
	
	public static final String NAME_KEY = "name";
	
	public static final String REVIEWS_KEY = "reviews";
	
	public static final String BUSINESSES_KEY = "businesses";
	
	public static List<String> cityList = new ArrayList<String>();
	
	public static List<String> categoryList = new ArrayList<String>();
	
	public static Map<String,String> featureMap = new HashMap<String,String>();
	
	static {
		
		//populate city list
		//cityList.add("Montreal");
		//cityList.add("Charlotte");
		//cityList.add("Las Vegas");
		
		/*cityList.add("Glendale");
		cityList.add("Gilbert");
		cityList.add("Montreal");*/
		
		
		/*cityList.add("Phoenix");
		cityList.add("Pittsburgh");*/
		/*cityList.add("Pittsburgh");
		cityList.add("Charlotte");
		cityList.add("Scottsdale");
		cityList.add("Tempe");
		cityList.add("Edinburgh");
		cityList.add("Mesa");
		*/
		cityList.add("Montreal");
		
		
		//populate category list
		categoryList.add("Pizza");
		categoryList.add("Cafes");
		
		
		//populate feature map
		featureMap.put("Parking","parking");
		featureMap.put("Price Range","price");
		featureMap.put("Good for Kids","kids");
		featureMap.put("Take-Out","take out");
		featureMap.put("Takes Reservation","reservation");
		featureMap.put("Accepts credit cards","credit cards");
		featureMap.put("Delivery","delivery");
		featureMap.put("Waiter Service","service");
		featureMap.put("Happy hour","happy hour");
		featureMap.put("Alcohol","alcohol");
		featureMap.put("Wi-Fi","WiFi");
		featureMap.put("Caters","cater");
		featureMap.put("Music","music");
		featureMap.put("Drive-Thru","drive thru");
		featureMap.put("Wheelchair accessible","wheelchair");
		featureMap.put("Smoking","smoking");
		featureMap.put("Good for Lunch","lunch");
		featureMap.put("Good for Desserts","desserts");
		featureMap.put("Good for Brunch","brunch");
		featureMap.put("Good for latenight","latenight");
		featureMap.put("Good for dinner","dinner");
		featureMap.put("Good for breakfast","breakfast");
		featureMap.put("Romantic Ambience","romance");
		featureMap.put("Casual Ambience","casual");
		featureMap.put("Classy Ambience","classy");
		featureMap.put("Upscale Ambience","upscale");
		
	}
}
