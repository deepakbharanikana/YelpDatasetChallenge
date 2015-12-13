/************************************************************************
 * File: Business.java
 * Author: Milind Gokhale (mgokhale@indiana.edu)
 *
 * An implementation of Business data structures business day with open and close timings for handling Business 
 * entries of yelp dataset.
 * This class will help automatic mapping business hours on each day from the business json file in yelp dataset.
 *
 * November 26, 2015
 *
 */

package com.search.project.yelp.task1.datatypes;

public class BusinessDay {
	private String open;
	private String close;

	public String getOpen() {
		return open;
	}

	public void setOpen(String open) {
		this.open = open;
	}

	public String getClose() {
		return close;
	}

	public void setClose(String close) {
		this.close = close;
	}

	@Override
	public String toString() {
		return "BusinessDay [open=" + open + ", close=" + close + "]";
	}

}
