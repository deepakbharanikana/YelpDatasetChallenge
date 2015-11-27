package com.search.project.yelp;

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
