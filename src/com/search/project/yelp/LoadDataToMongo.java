package com.search.project.yelp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.bson.Document;

import com.google.gson.JsonSyntaxException;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class LoadDataToMongo {
	private String corpusPath;
	private int rowCount;
	private String tableName;
	private String inputFileName;
	private String reportString;

	public LoadDataToMongo(String path) {
		setCorpusPath(path);
		reportString = "";
	}

	public String getCorpusPath() {
		return corpusPath;
	}

	public void setCorpusPath(String corpusPath) {
		this.corpusPath = corpusPath;
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getInputFileName() {
		return inputFileName;
	}

	public void setInputFileName(String inputFileName) {
		this.inputFileName = inputFileName;
	}

	public String getReportString() {
		return reportString;
	}

	public void setReportString(String reportString) {
		this.reportString = reportString;
	}

	public static void main(String[] args) {
		LoadDataToMongo ld = new LoadDataToMongo(
				"F:/Users/Milind/Documents/GitHub/Z534_Search/src/com/search/project/yelp/dataset/yelp_dataset_challenge_academic_dataset/");
		// "C:/Users/mgokhale/Documents/GitHub/Z534_Search/src/com/search/project/yelp/dataset/yelp_dataset_challenge_academic_dataset/yelp_academic_dataset_business.json"));
		long startTime = System.currentTimeMillis();
		ld.setReportString(ld.getReportString() + startTime + "\n");
		ld.loadDataToCollection("business",
				"yelp_academic_dataset_business.json");
		ld.loadDataToCollection("review", "yelp_academic_dataset_review.json");
		ld.loadDataToCollection("tip", "yelp_academic_dataset_tip.json");
		long endTime = System.currentTimeMillis();
		ld.setReportString(ld.getReportString() + endTime + "\n");
		ld.setReportString(ld.getReportString() + "It took "
				+ (endTime - startTime) + " milliseconds \n");
		ld.printReport();
	}

	public void printReport() {
		try {
			FileWriter fw = new FileWriter(
					"F:/Users/Milind/Documents/GitHub/Z534_Search/src/com/search/project/yelp/OutputFiles/"
							+ this.getClass().getName().toString()
							+ "_Report.txt");
			// Inserted 61184 rows in table business
			fw.write(reportString);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void loadDataToCollection(String tableName, String inputFileName) {
		setTableName(tableName);
		setInputFileName(inputFileName);

		MongoClient mongo = new MongoClient("localhost", 27017);
		MongoDatabase db = mongo.getDatabase("yelp");
		// System.out.println(db);
		MongoCollection<Document> table = db.getCollection(tableName);
		// truncate the tables
		table.drop();
		reportString += "dropped table " + tableName + "\n";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(getCorpusPath() + "/"
					+ inputFileName));
			String thisline = "";
			int i = 0;
			Document row;

			while ((thisline = br.readLine()) != null) {
				i++;
				row = Document.parse(thisline);
				table.insertOne(row);
			}
			br.close();
			setRowCount(i);
			reportString += "Inserted " + getRowCount() + " rows in table "
					+ getTableName() + " from file " + getInputFileName()
					+ "\n";
		} catch (JsonSyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mongo.close();
	}
}
