package com.search.project.yelp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Review {
	private Votes votes;
	private String user_id;
	private String review_id;
	private double stars;
	private Date date;
	private String text;
	private String type;
	private String business_id;

	public Review(Votes votes, String userId, String reviewId, double stars,
			Date date, String text, String type, String businessId) {
		this.votes = votes;
		this.user_id = userId;
		this.review_id = reviewId;
		this.stars = stars;
		this.date = date;
		this.text = text;
		this.type = type;
		this.business_id = businessId;
	}

	public Review() {
		// TODO Auto-generated constructor stub
	}

	public Votes getVotes() {
		return votes;
	}

	public void setVotes(Votes votes) {
		this.votes = votes;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getReview_id() {
		return review_id;
	}

	public void setReview_id(String review_id) {
		this.review_id = review_id;
	}

	public double getStars() {
		return stars;
	}

	public void setStars(double stars) {
		this.stars = stars;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBusiness_id() {
		return business_id;
	}

	public void setBusiness_id(String business_id) {
		this.business_id = business_id;
	}

	@Override
	public String toString() {
		return "Review [votes=" + votes + ", user_id=" + user_id
				+ ", review_id=" + review_id + ", stars=" + stars + ", date="
				+ date.getTime() + ", text=" + text + ", type=" + type
				+ ", business_id=" + business_id + "]";
	}

	public static void readGson() {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

		String jsonreview = "{\"votes\": {\"funny\": 0, \"useful\": 2, \"cool\": 1}, \"user_id\": \"Xqd0DzHaiyRqVH3WRG7hzg\", \"review_id\": \"15SdjuK7DmYqUAj6rjGowg\", \"stars\": 5, \"date\": \"2007-05-17\", \"text\": \"dr. goldberg offers everything i look for in a general practitioner.  he's nice and easy to talk to without being patronizing; he's always on time in seeing his patients; he's affiliated with a top-notch hospital (nyu) which my parents have explained to me is very important in case something happens and you need surgery; and you can get referrals to see specialists without having to see him first.  really, what more do you need?  i'm sitting here trying to think of any complaints i have about him, but i'm really drawing a blank.\", \"type\": \"review\", \"business_id\": \"vcNAWiLM4dR7D2nwwJ7nCA\"}";

		Review review = gson.fromJson(jsonreview, Review.class);
		System.out.println(review);

		BufferedReader br;
		try {

			br = new BufferedReader(
					new FileReader(
							"F:\\Users\\Milind\\Documents\\GitHub\\Z534_Search\\src\\com\\search\\project\\yelp\\corpus\\review1.txt"));
			String thisline = "";
			int i = 0;
			while ((thisline = br.readLine()) != null) {
				i++;
				review = gson.fromJson(thisline, Review.class);
				System.out.println(review);
			}
			System.out.println("Analyzed " + i + " tip rows");

			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}

	}

	public static void readBoon() {
		final ObjectMapper mapper = JsonFactory.create();

		String jsonreview = "{\"vote\": {\"funny\": 0, \"useful\": 2, \"cool\": 1}, \"user_id\": \"Xqd0DzHaiyRqVH3WRG7hzg\", \"review_id\": \"15SdjuK7DmYqUAj6rjGowg\", \"stars\": 5, \"date\": \"2007-05-17\", \"text\": \"dr. goldberg offers everything i look for in a general practitioner.  he's nice and easy to talk to without being patronizing; he's always on time in seeing his patients; he's affiliated with a top-notch hospital (nyu) which my parents have explained to me is very important in case something happens and you need surgery; and you can get referrals to see specialists without having to see him first.  really, what more do you need?  i'm sitting here trying to think of any complaints i have about him, but i'm really drawing a blank.\", \"type\": \"review\", \"business_id\": \"vcNAWiLM4dR7D2nwwJ7nCA\"}";
		try {
			BufferedReader br = new BufferedReader(
					new FileReader(
							"F:\\Users\\Milind\\Documents\\GitHub\\Z534_Search\\src\\com\\search\\project\\yelp\\corpus\\review1.txt"));
			Review review = mapper.fromJson(jsonreview, Review.class);
			System.out.println(review);
			// String thisline = "";
			// while ((thisline = br.readLine()) != null) {
			// Review review = mapper.fromJson(thisline, Review.class);
			// System.out.println(review);
			// }

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		Review.readGson();
		// Review.readBoon();
	}
}
