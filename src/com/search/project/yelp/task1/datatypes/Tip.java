/************************************************************************
 * File: Business.java
 * Author: Milind Gokhale (mgokhale@indiana.edu)
 *
 * An implementation of Tip data structure for handling Tip 
 * entries of yelp dataset.
 * This class will help automatic mapping tip from the tip.json file in yelp dataset.
 *
 * November 26, 2015
 *
 */

package com.search.project.yelp.task1.datatypes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Tip {
	private String user_id;
	private String text;
	private String business_id;
	private int likes;
	private Date date;
	private String type;

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getBusiness_id() {
		return business_id;
	}

	public void setBusiness_id(String business_id) {
		this.business_id = business_id;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Tip [user_id=" + user_id + ", text=" + text + ", business_id="
				+ business_id + ", likes=" + likes + ", date=" + date
				+ ", type=" + type + "]";
	}

	public static Tip jsonStringToTip(String json) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
		String jsontip = json;
		Tip tip = gson.fromJson(jsontip, Tip.class);
		return tip;
	}

	public static void readGson() {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
		String jsontip = "{\"user_id\": \"EZ0r9dKKtEGVx2CdnowPCw\", \"text\": \"Your GPS will not allow you to find this place. Put Rankin police department in instead. They are directly across the street.\", \"business_id\": \"mVHrayjG3uZ_RLHkLj-AMg\", \"likes\": 0, \"date\": \"2013-01-06\", \"type\": \"tip\"}";

		Tip tip = gson.fromJson(jsontip, Tip.class);
		System.out.println(tip);

		BufferedReader br;
		try {
			br = new BufferedReader(
					new FileReader(
							"F:/Users/Milind/Documents/GitHub/Z534_Search/src/com/search/project/yelp/dataset/yelp_dataset_challenge_academic_dataset/yelp_academic_dataset_tip.json"));
			// C:/Users/mgokhale/Documents/GitHub/Z534_Search/src/com/search/project/yelp/dataset/yelp_dataset_challenge_academic_dataset/yelp_academic_dataset_tip.json

			String thisline = "";
			int i = 0;
			while ((thisline = br.readLine()) != null) {
				i++;
				tip = gson.fromJson(thisline, Tip.class);
				System.out.println(tip);
			}
			System.out.println("Analyzed " + i + " tip rows");
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}

	}

	public static void main(String[] args) {
		Tip.readGson();
	}

}
