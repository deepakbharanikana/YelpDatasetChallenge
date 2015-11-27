package com.search.project.yelp;

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
