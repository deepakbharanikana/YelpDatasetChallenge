/**
 * @author Milind Gokhale
 * This class primarily gets the definite number of top category assignments for each of the businesses and then evaluates the precision and recall for the assignment  
 * 
 * Date : December 1, 2015
 * 
 */

package com.search.project.yelp.task1;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

/**
 * @author Milind
 * 
 *         This class is for picking definite top category assignments for
 *         businesses and evaluation.
 */
public class Evaluation {

	public static void main(String[] args) {
		int[] arrayOfCatAssignmentSize = { 3, 5, 10, 20 };
		for (int numberOfCategoryAssignments : arrayOfCatAssignmentSize)
			try {
				sortAndAssignCategories(numberOfCategoryAssignments);
				performanceEvaluation(numberOfCategoryAssignments);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private static void performanceEvaluation(int numberOfCategoryAssignments)
			throws IOException {
		// Declaring variable for MongoDB query operations
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase db = mongoClient.getDatabase("yelp");
		MongoCollection<org.bson.Document> testSetCollection = db
				.getCollection("New_test_set");
		MongoCollection<org.bson.Document> categoriesAssigned = db
				.getCollection("New_CategoriesAssigned");
		FileWriter fw = new FileWriter(
				UtilFunctions.getMySourcePath()
						+ "/com/search/project/yelp/OutputFiles/PerformanceEval_New_VSM_PreWords_"
						+ numberOfCategoryAssignments + "Cats.txt");
		FileWriter fwNonZero = new FileWriter(
				UtilFunctions.getMySourcePath()
						+ "/com/search/project/yelp/OutputFiles/PerformanceEvalNonZero_New_VSM_PreWords_"
						+ numberOfCategoryAssignments + "Cats.txt");

		Document queryString = new Document();
		MongoCursor<Document> outputCursor = categoriesAssigned.find(
				queryString).iterator();
		Document result;
		Document result1;
		MongoCursor<Document> testCursor;
		Document queryString1;
		String businessID;
		List<String> testCategories;
		List<String> outputCategories;
		ArrayList<String> generatedCategories;

		// Iterating over each document in the "categories_assigned_from_code"
		// collection that stores the programmatically assigned categories for
		// each business in test data
		int totalBusinessesWithScore = 0;
		Double totalPrecision = 0.0;
		Double totalRecall = 0.0;

		while (outputCursor.hasNext()) {
			result = outputCursor.next();
			businessID = (String) result.get("businessID");
			outputCategories = (List) result.get("categories");

			// Categories assigned by our code
			generatedCategories = (ArrayList<String>) outputCategories;

			queryString1 = new Document("business_id", businessID);
			testCursor = testSetCollection.find(queryString1).iterator();
			if (testCursor.hasNext()) {
				result1 = testCursor.next();
				// Ground truth categories
				testCategories = (List) result1.get("categories");

				// Compute the number of matched categories between the
				// programmatically assigned and ground truth categories
				int matched = 0;
				float precision = 0;
				float recall = 0;
				float fMeasure = 0;

				if (generatedCategories.size() > testCategories.size()) {
					for (String generatedCategory : generatedCategories) {
						for (String testCategory : testCategories) {
							if ((generatedCategory.trim()).equals(testCategory
									.toString().trim())) {
								matched++;
							}

						}
					}
				} else {
					for (Object testCategory : testCategories) {
						for (String generatedCategory : generatedCategories) {
							if ((generatedCategory.trim()).equals(testCategory
									.toString().trim())) {
								matched++;
							}

						}
					}

				}

				if (testCategories.size() > 0) // i.e. The business id has some
												// ground truth to measure
												// precision and recall
				{
					// Compute Precision,Recall and F2 measure
					precision = ((float) matched / (generatedCategories.size()));
					recall = ((float) matched / (testCategories.size()));
					if (precision != 0 && recall != 0)
						fMeasure = ((float) 2 * precision * recall)
								/ (precision + recall) * (5 / 4);
					else
						fMeasure = 0;

					fw.write("Business ID : " + businessID
							+ " | Ground Truth Categories : "
							+ testCategories.toString()
							+ " | Programatically Assigned Categories : "
							+ outputCategories + " | Precision : " + precision
							+ " | Recall : " + recall + " | F-Measure: "
							+ fMeasure + "\n");
					if (precision > 0.0 || recall > 0.0) {
						totalBusinessesWithScore++;
						totalPrecision += precision;
						totalRecall += recall;
						fwNonZero.write("Business ID : " + businessID
								+ " | Ground Truth Categories : "
								+ testCategories.toString()
								+ " | Programatically Assigned Categories : "
								+ outputCategories + " | Precision : "
								+ precision + " | Recall : " + recall
								+ " | F-Measure: " + fMeasure + "\n");
						System.out.println("Business ID : " + businessID
								+ " | Ground Truth Categories : "
								+ testCategories.toString()
								+ " | Programatically Assigned Categories : "
								+ outputCategories + " | Precision : "
								+ precision + " | Recall : " + recall
								+ " | F-Measure: " + fMeasure);
					}
				}
			}
		}

		fwNonZero.write("Total Documents with Score: "
				+ totalBusinessesWithScore + "\n");
		fwNonZero.write("Average Precision: "
				+ (totalPrecision / totalBusinessesWithScore * 100) + "\n");
		fwNonZero.write("Average Recall: "
				+ (totalRecall / totalBusinessesWithScore * 100) + "\n");
		System.out.println("Total Documents with Score: "
				+ totalBusinessesWithScore);
		System.out.println("Average Precision: "
				+ (totalPrecision / totalBusinessesWithScore * 100));
		System.out.println("Average Recall: "
				+ (totalRecall / totalBusinessesWithScore * 100));

		fw.close();
		fwNonZero.close();
	}

	private static void sortAndAssignCategories(int numberOfTopCategories) {
		// get the assignment rows from the AssignmentScoreMap and then sort the
		// scores of each row and put it in the new table of final
		// CategoryAssigned_byCode
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase db = mongoClient.getDatabase("yelp");
		MongoCollection<org.bson.Document> assignmentScoreMapCollection = db
				.getCollection("New_AssignmentScoreMap");
		MongoCollection<org.bson.Document> categoriesAssigned = db
				.getCollection("New_CategoriesAssigned");
		categoriesAssigned.drop();
		MongoCursor<org.bson.Document> assignmentScoreMapCursor = assignmentScoreMapCollection
				.find().iterator();
		while (assignmentScoreMapCursor.hasNext()) {
			org.bson.Document business = assignmentScoreMapCursor.next();
			String businessID = (String) business.get("business_id");
			org.bson.Document categories1 = (org.bson.Document) business
					.get("categories");
			HashMap<String, Double> categories = new HashMap<String, Double>();
			for (Entry<String, Object> category : categories1.entrySet()) {
				categories.put(category.getKey(),
						Double.valueOf(category.getValue().toString()));
			}
			List list = new LinkedList(categories.entrySet());

			Collections.sort(list, new Comparator() {
				public int compare(Object o1, Object o2) {
					return ((Comparable) ((Map.Entry) (o2)).getValue())
							.compareTo(((Map.Entry) (o1)).getValue());
				}
			});

			int i = 0;
			org.bson.Document assignedCategoriesRow = new Document();
			ArrayList<String> assignedCats = new ArrayList<String>();
			HashMap sortedCategories = (HashMap) new LinkedHashMap();
			for (Iterator it = list.iterator(); it.hasNext();) {

				Map.Entry entry = (Map.Entry) it.next();
				sortedCategories.put(entry.getKey(), entry.getValue());
				if (i < numberOfTopCategories) {
					assignedCats.add(entry.getKey().toString());
					i++;
				}
			}

			assignedCategoriesRow.put("businessID", businessID);
			assignedCategoriesRow.put("categories", assignedCats);
			categoriesAssigned.insertOne(assignedCategoriesRow);
		}
		mongoClient.close();
	}
}
