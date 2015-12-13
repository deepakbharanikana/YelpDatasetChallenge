/************************************************************************
 * File: Business.java
 * Author: Milind Gokhale (mgokhale@indiana.edu)
 *
 * An implementation of Business data structures attributes for handling Business 
 * entries of yelp dataset.
 * This class will help automatic mapping of attributes from the business.json file in yelp dataset.
 *
 * November 26, 2015
 *
 */

package com.search.project.yelp.task1.datatypes;

import com.google.gson.JsonObject;

public class BusinessAttributes {
	private JsonObject attributes;

	public JsonObject getAttributes() {
		return attributes;
	}

	public void setAttributes(JsonObject attributes) {
		this.attributes = attributes;
	}

	@Override
	public String toString() {
		return "BusinessAttributes [attributes=" + attributes + "]";
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
